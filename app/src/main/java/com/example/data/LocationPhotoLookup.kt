package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

object LocationPhotoLookup {
    private val urlCache = ConcurrentHashMap<String, String>()

    suspend fun resolvePhotoUrl(rawLocation: String): String? {
        val clean = rawLocation.trim()
        if (clean.isBlank()) return null

        val cacheKey = clean.lowercase()
        urlCache[cacheKey]?.let { return it }

        return withContext(Dispatchers.IO) {
            val hasCyrillic = clean.any { it in '\u0400'..'\u04FF' }
            val primaryHost = if (hasCyrillic) "ru.wikipedia.org" else "en.wikipedia.org"

            val primaryHit = fetchWikimediaImage(primaryHost, clean)
            val cityOnly = clean.substringBefore(',').trim()
            val resolved = primaryHit
                ?: if (cityOnly.isNotBlank() && cityOnly != clean) {
                    fetchWikimediaImage(primaryHost, cityOnly)
                } else {
                    null
                }
                ?: if (hasCyrillic) {
                    fetchWikimediaImage("en.wikipedia.org", clean)
                } else {
                    fetchWikimediaImage("en.wikipedia.org", "$cityOnly landmark")
                }

            if (!resolved.isNullOrBlank()) {
                urlCache[cacheKey] = resolved
            }
            resolved
        }
    }

    internal fun buildSearchUrl(host: String, query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://$host/w/api.php?action=query&generator=search&gsrsearch=$encoded" +
            "&gsrlimit=6&prop=pageimages&piprop=thumbnail&pithumbsize=1080&format=json&origin=*"
    }

    internal fun parseBestImageUrl(jsonText: String): String? {
        if (jsonText.isBlank()) return null
        val root = JSONObject(jsonText)
        val queryObj = root.optJSONObject("query") ?: return null
        val pagesObj = queryObj.optJSONObject("pages") ?: return null

        data class Candidate(val index: Int, val url: String)
        val candidates = mutableListOf<Candidate>()
        val keys = pagesObj.keys()
        while (keys.hasNext()) {
            val page = pagesObj.optJSONObject(keys.next()) ?: continue
            val index = page.optInt("index", Int.MAX_VALUE)
            val thumb = page.optJSONObject("thumbnail") ?: continue
            val src = thumb.optString("source", "").trim()
            if (src.startsWith("https://")) {
                candidates.add(Candidate(index, src))
            }
        }
        val sorted = candidates.sortedBy { it.index }
        val preferred = sorted.firstOrNull { cand ->
            val lower = cand.url.lowercase()
            val isRasterPhoto = lower.contains(".jpg") ||
                lower.contains(".jpeg") ||
                lower.contains(".webp") ||
                lower.contains(".png")
            val isGraphicSymbol = lower.contains("flag_of") ||
                lower.contains("locator") ||
                lower.contains("location_map") ||
                lower.contains("coat_of_arms") ||
                lower.contains("emblem") ||
                lower.contains(".svg")
            isRasterPhoto && !isGraphicSymbol
        }
        return preferred?.url ?: sorted.firstOrNull()?.url
    }

    private fun fetchWikimediaImage(host: String, query: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(buildSearchUrl(host, query))
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "DuetCouplesApp/1.0 (Android)")
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode in 200..299) {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseBestImageUrl(body)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
