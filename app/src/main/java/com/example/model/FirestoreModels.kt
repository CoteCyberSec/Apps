package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.BreakIterator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val coupleId: String? = null,
    val preferredLanguage: String = "en",
    val showSecondaryLanguage: Boolean = true,
    val avatarId: String = "",
    val nameConfirmed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CoupleInvite(
    @DocumentId val id: String = "",
    val coupleId: String = "",
    val creatorUid: String = "",
    val creatorName: String = "",
    val claimed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CoupleSpace(
    @DocumentId val id: String = "",
    val creatorUid: String = "",
    val memberUids: List<String> = emptyList(),
    val partnerOneName: String = "",
    val partnerTwoName: String = "",
    val partnerOneAvatarId: String = "",
    val partnerTwoAvatarId: String = "",
    val inviteCode: String = "",
    val anniversaryLabel: String = "",
    val nextMeetDateKey: String = "",
    val nextMeetLocation: String = "",
    val relationshipStartDateKey: String = "",
    val aestheticThemeId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class QuestionAnswer(
    @DocumentId val id: String = "",
    val coupleId: String = "",
    val questionId: String = "",
    val topicId: String = "",
    val authorUid: String = "",
    val authorLabel: String = "",
    val speakerRole: String = "self",
    val text: String = "",
    val reaction: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CustomQuestion(
    @DocumentId val id: String = "",
    val coupleId: String = "",
    val topicId: String = "custom",
    val promptEn: String = "",
    val promptRu: String = "",
    val authorUid: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toPromptQuestion(): PromptQuestion {
        val en = promptEn.ifBlank { promptRu }
        val ru = promptRu.ifBlank { promptEn }
        return PromptQuestion(
            id = id,
            topicId = topicId,
            text = BilingualText(en = en, ru = ru),
            isCustom = true
        )
    }
}

@Keep
@IgnoreExtraProperties
data class DailyDrawing(
    @DocumentId val id: String = "",
    val coupleId: String = "",
    val dateKey: String = "",
    val authorUid: String = "",
    val authorLabel: String = "",
    val speakerRole: String = "self",
    val caption: String = "",
    val strokes: List<String> = emptyList(),
    val reaction: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class NormalizedPoint(val x: Float, val y: Float)

data class DrawingStroke(
    val colorIndex: Int,
    val widthDp: Float,
    val points: List<NormalizedPoint>
)

object DrawingStrokeCodec {
    fun encode(stroke: DrawingStroke): String {
        if (stroke.points.isEmpty()) return ""
        val pts = stroke.points.take(180).joinToString(";") { p ->
            val xi = (p.x.coerceIn(0f, 1f) * 1000f).roundToInt()
            val yi = (p.y.coerceIn(0f, 1f) * 1000f).roundToInt()
            "$xi,$yi"
        }
        val widthInt = stroke.widthDp.roundToInt().coerceIn(2, 40)
        return "${stroke.colorIndex}|$widthInt|$pts"
    }

    fun decode(encoded: List<String>): List<DrawingStroke> {
        return encoded.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size != 3) return@mapNotNull null
            val colorIdx = parts[0].toIntOrNull() ?: 0
            val width = (parts[1].toFloatOrNull() ?: 8f).coerceIn(2f, 40f)
            val points = parts[2].split(";").mapNotNull { pair ->
                val xy = pair.split(",")
                if (xy.size != 2) return@mapNotNull null
                val x = (xy[0].toFloatOrNull() ?: return@mapNotNull null) / 1000f
                val y = (xy[1].toFloatOrNull() ?: return@mapNotNull null) / 1000f
                NormalizedPoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
            }
            if (points.isEmpty()) null else DrawingStroke(colorIdx, width, points)
        }
    }
}

object ReunionDateHelper {
    private val keyFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }

    fun todayDateKey(): String {
        return keyFormat.format(Calendar.getInstance().time)
    }

    fun utcMillisToDateKey(utcMillis: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = utcMillis
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    fun dateKeyToUtcMillis(dateKey: String): Long? {
        if (!dateKey.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) return null
        val parts = dateKey.split("-")
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(y, m - 1, d, 0, 0, 0)
        return cal.timeInMillis
    }

    fun daysUntil(dateKey: String, todayDateKey: String = todayDateKey()): Int? {
        val targetMillis = dateKeyToUtcMillis(dateKey) ?: return null
        val todayMillis = dateKeyToUtcMillis(todayDateKey) ?: return null
        val diffMillis = targetMillis - todayMillis
        return (diffMillis / 86_400_000L).toInt()
    }

    fun formatDateDisplay(dateKey: String, language: AppLanguage): String {
        val parts = dateKey.split("-")
        if (parts.size != 3) return dateKey
        val y = parts[0].toIntOrNull() ?: return dateKey
        val m = parts[1].toIntOrNull() ?: return dateKey
        val d = parts[2].toIntOrNull() ?: return dateKey
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, d, 12, 0, 0)
        val locale = if (language == AppLanguage.RU) Locale("ru") else Locale.ENGLISH
        val fmt = SimpleDateFormat("d MMM yyyy", locale)
        return fmt.format(cal.time)
    }

    fun parseExifDateTime(rawExif: String?): Triple<String, String, Long>? {
        val clean = rawExif?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val normalized = clean.replace("T", " ").take(19)
        val patterns = listOf("yyyy:MM:dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy:MM:dd", "yyyy-MM-dd")
        for (pattern in patterns) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
                val parsed = fmt.parse(normalized) ?: continue
                val cal = Calendar.getInstance().apply { time = parsed }
                val dateKey = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                val timeLabel = if (pattern.contains("HH:mm")) {
                    String.format(
                        Locale.US,
                        "%02d:%02d",
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE)
                    )
                } else ""
                return Triple(dateKey, timeLabel, parsed.time.coerceAtLeast(0L))
            } catch (_: Exception) {
                // Try next format
            }
        }
        return null
    }

    fun millisToDateAndTime(millis: Long): Pair<String, String> {
        val safeMillis = if (millis > 0L) millis else System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = safeMillis }
        val dateKey = String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val timeLabel = String.format(
            Locale.US,
            "%02d:%02d",
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
        return dateKey to timeLabel
    }
}

data class RelationshipStats(
    val totalDays: Long,
    val years: Int,
    val months: Int,
    val days: Int,
    val nextAnniversaryDateKey: String,
    val nextAnniversaryNumber: Int,
    val daysUntilNextAnniversary: Int,
    val isAnniversaryToday: Boolean
)

object RelationshipTrackerHelper {
    private fun parseYmd(dateKey: String): Triple<Int, Int, Int>? {
        if (!dateKey.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) return null
        val parts = dateKey.split("-")
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        if (m !in 1..12 || d !in 1..31) return null
        return Triple(y, m, d)
    }

    private fun daysInMonth(year: Int, monthOneBased: Int): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(year, monthOneBased - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun anniversaryDateKeyForYear(year: Int, startMonth: Int, startDay: Int): String {
        val clampedDay = startDay.coerceAtMost(daysInMonth(year, startMonth))
        return String.format(Locale.US, "%04d-%02d-%02d", year, startMonth, clampedDay)
    }

    fun calculateStats(
        startDateKey: String,
        todayDateKey: String = ReunionDateHelper.todayDateKey()
    ): RelationshipStats? {
        val (sY, sM, sD) = parseYmd(startDateKey) ?: return null
        val (tY, tM, tD) = parseYmd(todayDateKey) ?: return null
        val startMillis = ReunionDateHelper.dateKeyToUtcMillis(startDateKey) ?: return null
        val todayMillis = ReunionDateHelper.dateKeyToUtcMillis(todayDateKey) ?: return null

        val totalDays = ((todayMillis - startMillis) / 86_400_000L).coerceAtLeast(0L)

        var years: Int
        var months: Int
        var days: Int
        if (todayMillis <= startMillis) {
            years = 0
            months = 0
            days = 0
        } else {
            years = tY - sY
            months = tM - sM
            days = tD - sD
            if (days < 0) {
                months -= 1
                val prevMonthYear = if (tM == 1) tY - 1 else tY
                val prevMonth = if (tM == 1) 12 else tM - 1
                days += daysInMonth(prevMonthYear, prevMonth)
            }
            if (months < 0) {
                years -= 1
                months += 12
            }
        }

        val nextAnnivYear: Int
        val isTodayAnniv: Boolean
        if (tY <= sY) {
            nextAnnivYear = sY + 1
            isTodayAnniv = false
        } else {
            val candidateThisYear = anniversaryDateKeyForYear(tY, sM, sD)
            when {
                candidateThisYear == todayDateKey -> {
                    nextAnnivYear = tY
                    isTodayAnniv = true
                }
                candidateThisYear > todayDateKey -> {
                    nextAnnivYear = tY
                    isTodayAnniv = false
                }
                else -> {
                    nextAnnivYear = tY + 1
                    isTodayAnniv = false
                }
            }
        }

        val nextAnnivKey = anniversaryDateKeyForYear(nextAnnivYear, sM, sD)
        val nextAnnivMillis = ReunionDateHelper.dateKeyToUtcMillis(nextAnnivKey) ?: todayMillis
        val daysUntilAnniv = if (isTodayAnniv) {
            0
        } else {
            ((nextAnnivMillis - todayMillis) / 86_400_000L).toInt().coerceAtLeast(0)
        }

        return RelationshipStats(
            totalDays = totalDays,
            years = years.coerceAtLeast(0),
            months = months.coerceAtLeast(0),
            days = days.coerceAtLeast(0),
            nextAnniversaryDateKey = nextAnnivKey,
            nextAnniversaryNumber = (nextAnnivYear - sY).coerceAtLeast(1),
            daysUntilNextAnniversary = daysUntilAnniv,
            isAnniversaryToday = isTodayAnniv
        )
    }
}

@Keep
@IgnoreExtraProperties
data class TripMemory(
    @DocumentId val id: String = "",
    val coupleId: String = "",
    val title: String = "",
    val location: String = "",
    val startDateKey: String = "",
    val endDateKey: String = "",
    val notes: String = "",
    val authorUid: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TripPhoto(
    @DocumentId val id: String = "",
    val coupleId: String = "",
    val tripId: String = "",
    val takenDateKey: String = "",
    val takenTimeLabel: String = "",
    val takenMillis: Long = 0L,
    val timestampSource: String = "exif",
    val caption: String = "",
    val thumbnailBase64: String = "",
    val reaction: String = "",
    val authorUid: String = "",
    val authorLabel: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object EmojiReactionHelper {
    val quickEmojis: List<String> = listOf("❤️", "🥰", "😂", "✨", "🥹", "🔥")

    data class EmojiCategory(
        val titleEn: String,
        val titleRu: String,
        val emojis: List<String>
    )

    val categories: List<EmojiCategory> = listOf(
        EmojiCategory(
            titleEn = "Love & Warmth",
            titleRu = "Любовь и тепло",
            emojis = listOf("❤️", "🩷", "🧡", "💛", "💜", "🤍", "🫶", "🥰", "😍", "😘", "🤗", "💌")
        ),
        EmojiCategory(
            titleEn = "Joy & Feels",
            titleRu = "Радость и чувства",
            emojis = listOf("😂", "🤣", "😊", "🥹", "🥺", "😭", "🤩", "🤭", "🫠", "👏", "🙌", "💯")
        ),
        EmojiCategory(
            titleEn = "Moments & Places",
            titleRu = "Моменты и путешествия",
            emojis = listOf("✨", "🌟", "💫", "🔥", "🎉", "🥂", "✈️", "🌅", "🏔️", "🌊", "📸", "🎨")
        ),
        EmojiCategory(
            titleEn = "Cozy & Sweet",
            titleRu = "Уют и нежность",
            emojis = listOf("🌸", "🌻", "🌹", "🦋", "🧸", "☕", "🍓", "🍰", "🎶", "🌙", "🕊️", "👀")
        )
    )

    fun extractFirstEmojiGrapheme(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        val iterator = BreakIterator.getCharacterInstance(Locale.US)
        iterator.setText(trimmed)
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            val grapheme = trimmed.substring(start, end)
            if (grapheme.isNotBlank()) {
                return if (grapheme.length <= 16) grapheme else ""
            }
            start = end
            end = iterator.next()
        }
        return ""
    }
}
