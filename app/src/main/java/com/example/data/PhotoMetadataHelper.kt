package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.example.model.ReunionDateHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class ProcessedTripPhoto(
    val takenDateKey: String,
    val takenTimeLabel: String,
    val takenMillis: Long,
    val timestampSource: String,
    val thumbnailBase64: String,
    val localCacheBytes: ByteArray
)

object PhotoMetadataHelper {

    fun processPickedPhoto(context: Context, uri: Uri): ProcessedTripPhoto? {
        val resolver = context.contentResolver

        var orientation = ExifInterface.ORIENTATION_NORMAL
        var parsedMeta: Triple<String, String, Long>? = null

        resolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val rawDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            parsedMeta = ReunionDateHelper.parseExifDateTime(rawDate)
        }

        val (takenDateKey, takenTimeLabel, takenMillis, source) = if (parsedMeta != null) {
            val (dk, tl, ms) = parsedMeta!!
            Quad(dk, tl, ms, "exif")
        } else {
            val mediaDate = queryMediaStoreTimestamp(context, uri)
            if (mediaDate != null && mediaDate > 0L) {
                val (dk, tl) = ReunionDateHelper.millisToDateAndTime(mediaDate)
                Quad(dk, tl, mediaDate, "file")
            } else {
                val now = System.currentTimeMillis()
                val (dk, tl) = ReunionDateHelper.millisToDateAndTime(now)
                Quad(dk, tl, now, "manual")
            }
        }

        // Decode bounds first for memory efficiency
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOpts)
        }
        val origW = boundsOpts.outWidth
        val origH = boundsOpts.outHeight
        if (origW <= 0 || origH <= 0) return null

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = computeInSampleSize(origW, origH, 960, 960)
        }
        val rawBitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: return null

        val orientedBitmap = applyExifOrientation(rawBitmap, orientation)
        val syncBitmap = scaleDown(orientedBitmap, 520)

        // Compress JPEG cleanly so Base64 stays well under Firestore rule limit (220,000 chars)
        var quality = 74
        var jpegBytes = compressJpeg(syncBitmap, quality)
        while (jpegBytes.size > 150_000 && quality > 38) {
            quality -= 10
            jpegBytes = compressJpeg(syncBitmap, quality)
        }
        val finalBase64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)

        // Higher-res local cache bytes for snappy local display
        val localBitmap = scaleDown(orientedBitmap, 1080)
        val localBytes = compressJpeg(localBitmap, 82)

        if (syncBitmap !== orientedBitmap && !syncBitmap.isRecycled) syncBitmap.recycle()
        if (localBitmap !== orientedBitmap && !localBitmap.isRecycled) localBitmap.recycle()
        if (orientedBitmap !== rawBitmap && !rawBitmap.isRecycled) rawBitmap.recycle()
        if (!orientedBitmap.isRecycled) orientedBitmap.recycle()

        return ProcessedTripPhoto(
            takenDateKey = takenDateKey,
            takenTimeLabel = takenTimeLabel,
            takenMillis = takenMillis,
            timestampSource = source,
            thumbnailBase64 = finalBase64,
            localCacheBytes = localBytes
        )
    }

    fun saveLocalPhotoFile(context: Context, photoId: String, bytes: ByteArray) {
        runCatching {
            val dir = File(context.filesDir, "trip_photos")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "$photoId.jpg")
            FileOutputStream(file).use { it.write(bytes) }
        }
    }

    fun getLocalPhotoFileIfExists(context: Context, photoId: String): File? {
        val file = File(File(context.filesDir, "trip_photos"), "$photoId.jpg")
        return if (file.exists() && file.length() > 0L) file else null
    }

    fun decodeBase64ToBytes(base64: String): ByteArray? {
        return runCatching {
            Base64.decode(base64, Base64.DEFAULT)
        }.getOrNull()
    }

    private fun queryMediaStoreTimestamp(context: Context, uri: Uri): Long? {
        return runCatching {
            val projection = arrayOf(
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.MediaColumns.DATE_MODIFIED
            )
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val takenIdx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                if (takenIdx >= 0) {
                    val taken = cursor.getLong(takenIdx)
                    if (taken > 0L) return@use taken
                }
                val modIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                if (modIdx >= 0) {
                    val modSec = cursor.getLong(modIdx)
                    if (modSec > 0L) return@use modSec * 1000L
                }
                null
            }
        }.getOrNull()
    }

    private fun computeInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfH = height / 2
            val halfW = width / 2
            while (halfH / inSampleSize >= reqHeight && halfW / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleDown(bitmap: Bitmap, maxDim: Int): Bitmap {
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / maxSide.toFloat()
        val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun compressJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return out.toByteArray()
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
