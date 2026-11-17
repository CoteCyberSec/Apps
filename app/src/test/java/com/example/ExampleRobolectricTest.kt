package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppLanguage
import com.example.model.DrawingStroke
import com.example.model.DrawingStrokeCodec
import com.example.model.NormalizedPoint
import com.example.model.ReunionDateHelper
import com.example.model.TopicCatalog
import com.example.model.UiStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Duet", appName)
    }

    @Test
    fun `all topic decks and questions have English and Russian translations`() {
        assertTrue(TopicCatalog.decks.size >= 8)
        assertTrue(TopicCatalog.decks.any { it.id == "ldr_miles" && it.questions.size >= 8 })
        assertTrue(TopicCatalog.decks.any { it.id == "ldr_reunions" && it.questions.size >= 8 })
        TopicCatalog.decks.forEach { deck ->
            assertTrue(deck.title.en.isNotBlank())
            assertTrue(deck.title.ru.isNotBlank())
            assertTrue(deck.subtitle.en.isNotBlank())
            assertTrue(deck.subtitle.ru.isNotBlank())
            deck.questions.forEach { q ->
                assertTrue(q.text.en.isNotBlank())
                assertTrue(q.text.ru.isNotBlank())
            }
        }
        assertEquals("Duet", UiStrings.appTitle(AppLanguage.EN))
        assertEquals("Дуэт", UiStrings.appTitle(AppLanguage.RU))
    }

    @Test
    fun `drawing stroke codec round-trips strokes cleanly`() {
        val stroke = DrawingStroke(
            colorIndex = 2,
            widthDp = 9f,
            points = listOf(NormalizedPoint(0.25f, 0.5f), NormalizedPoint(0.75f, 0.9f))
        )
        val encoded = DrawingStrokeCodec.encode(stroke)
        val decoded = DrawingStrokeCodec.decode(listOf(encoded))
        assertEquals(1, decoded.size)
        assertEquals(2, decoded[0].colorIndex)
        assertEquals(9f, decoded[0].widthDp, 0.01f)
        assertEquals(2, decoded[0].points.size)
        assertEquals(0.25f, decoded[0].points[0].x, 0.002f)
        assertEquals(0.5f, decoded[0].points[0].y, 0.002f)
    }

    @Test
    fun `reunion date helper converts and formats dates consistently`() {
        val today = ReunionDateHelper.todayDateKey()
        assertEquals(0, ReunionDateHelper.daysUntil(today))
        val millis = ReunionDateHelper.dateKeyToUtcMillis("2027-02-14")
        assertTrue(millis != null)
        assertEquals("2027-02-14", ReunionDateHelper.utcMillisToDateKey(millis!!))
        assertTrue(ReunionDateHelper.formatDateDisplay("2027-02-14", AppLanguage.EN).contains("2027"))
        assertTrue(ReunionDateHelper.formatDateDisplay("2027-02-14", AppLanguage.RU).contains("2027"))
    }

    @Test
    fun `exif timestamp parser extracts photo date and time`() {
        val parsed = ReunionDateHelper.parseExifDateTime("2025:10:13 18:45:22")
        assertTrue(parsed != null)
        assertEquals("2025-10-13", parsed!!.first)
        assertEquals("18:45", parsed.second)
        val fromMillis = ReunionDateHelper.millisToDateAndTime(parsed.third)
        assertEquals("2025-10-13", fromMillis.first)
        assertEquals("18:45", fromMillis.second)
    }

    @Test
    fun `avatar catalog presets are bilingual and resolve per partner`() {
        assertTrue(com.example.model.AvatarCatalog.presets.size >= 8)
        com.example.model.AvatarCatalog.presets.forEach { preset ->
            assertTrue(preset.id.matches(Regex("^[a-z0-9_]+$")))
            assertTrue(preset.name.en.isNotBlank())
            assertTrue(preset.name.ru.isNotBlank())
        }
        val couple = com.example.model.CoupleSpace(
            id = "c1",
            creatorUid = "uid_1",
            partnerOneAvatarId = "sunny_cat",
            partnerTwoAvatarId = "moon_bunny"
        )
        assertEquals(
            "sunny_cat",
            com.example.model.AvatarCatalog.resolveForPartner(couple, "uid_1", "self")
        )
        assertEquals(
            "moon_bunny",
            com.example.model.AvatarCatalog.resolveForPartner(couple, "uid_1", "partner_local")
        )
        assertEquals(
            "moon_bunny",
            com.example.model.AvatarCatalog.resolveForPartner(couple, "uid_2", "self")
        )
        assertEquals(1, com.example.model.AvatarCatalog.slotForUser(couple, "uid_1"))
        assertEquals(2, com.example.model.AvatarCatalog.slotForUser(couple, "uid_2"))
    }

    @Test
    fun `emoji reaction helper extracts intact grapheme clusters and provides categories`() {
        assertTrue(com.example.model.EmojiReactionHelper.categories.isNotEmpty())
        assertTrue(com.example.model.EmojiReactionHelper.quickEmojis.contains("❤️"))
        assertEquals("🫶", com.example.model.EmojiReactionHelper.extractFirstEmojiGrapheme("🫶🌻"))
        assertEquals("❤️", com.example.model.EmojiReactionHelper.extractFirstEmojiGrapheme("  ❤️  "))
        assertEquals("", com.example.model.EmojiReactionHelper.extractFirstEmojiGrapheme("   "))
    }

    @Test
    fun `location photo lookup prefers real location photo over map or svg`() {
        val searchUrl = com.example.data.LocationPhotoLookup.buildSearchUrl("en.wikipedia.org", "Dubai")
        assertTrue(searchUrl.contains("gsrsearch=Dubai"))
        val sampleJson = """
            {
              "query": {
                "pages": {
                  "1": {
                    "index": 1,
                    "thumbnail": { "source": "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/Dubai_location_map.svg.png" }
                  },
                  "2": {
                    "index": 2,
                    "thumbnail": { "source": "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Dubai_Skyline.jpg" }
                  }
                }
              }
            }
        """.trimIndent()
        assertEquals(
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Dubai_Skyline.jpg",
            com.example.data.LocationPhotoLookup.parseBestImageUrl(sampleJson)
        )
    }

    @Test
    fun `couple aesthetic catalog validates presets custom hex and generates light and dark themes`() {
        val catalog = com.example.model.CoupleAestheticCatalog
        assertTrue(catalog.presets.size >= 8)
        catalog.presets.forEach { preset ->
            assertTrue(catalog.isValidThemeId(preset.id))
            assertTrue(preset.name.en.isNotBlank())
            assertTrue(preset.name.ru.isNotBlank())
            val (lightScheme, lightGold) = com.example.ui.theme.resolveThemePalette(preset.id, darkTheme = false)
            val (darkScheme, darkGold) = com.example.ui.theme.resolveThemePalette(preset.id, darkTheme = true)
            assertTrue(lightScheme.primary != darkScheme.primary)
            assertTrue(lightGold.accent != darkGold.accent)
        }

        val customId = catalog.formatCustomThemeId(0xFFD96B43.toInt())
        assertEquals("custom_D96B43", customId)
        assertTrue(catalog.isValidThemeId(customId))
        assertEquals(0xFFD96B43.toInt(), catalog.parseCustomRgb(customId))
        val (customLight, _) = com.example.ui.theme.resolveThemePalette(customId, darkTheme = false)
        val (customDark, _) = com.example.ui.theme.resolveThemePalette(customId, darkTheme = true)
        assertTrue(customLight.background != customDark.background)

        assertTrue(!catalog.isValidThemeId("custom_ZZZZZZ"))
        assertTrue(!catalog.isValidThemeId("bad/theme!"))
    }

    @Test
    fun `relationship tracker calculates duration, anniversaries, and leap day accurately`() {
        val helper = com.example.model.RelationshipTrackerHelper

        // Multi-year & upcoming anniversary
        val stats1 = helper.calculateStats(startDateKey = "2023-06-15", todayDateKey = "2026-05-15")!!
        assertEquals(2, stats1.years)
        assertEquals(11, stats1.months)
        assertEquals(0, stats1.days)
        assertEquals(1065L, stats1.totalDays)
        assertEquals("2026-06-15", stats1.nextAnniversaryDateKey)
        assertEquals(3, stats1.nextAnniversaryNumber)
        assertEquals(31, stats1.daysUntilNextAnniversary)
        assertEquals(false, stats1.isAnniversaryToday)

        // Anniversary today
        val todayStats = helper.calculateStats(startDateKey = "2024-05-01", todayDateKey = "2026-05-01")!!
        assertEquals(2, todayStats.years)
        assertEquals(0, todayStats.months)
        assertEquals(0, todayStats.days)
        assertEquals("2026-05-01", todayStats.nextAnniversaryDateKey)
        assertEquals(2, todayStats.nextAnniversaryNumber)
        assertEquals(0, todayStats.daysUntilNextAnniversary)
        assertEquals(true, todayStats.isAnniversaryToday)

        // Leap day clamped to Feb 28 on non-leap year
        val leapStats = helper.calculateStats(startDateKey = "2024-02-29", todayDateKey = "2026-01-10")!!
        assertEquals("2026-02-28", leapStats.nextAnniversaryDateKey)
        assertEquals(2, leapStats.nextAnniversaryNumber)

        // Invalid date returns null
        assertEquals(null, helper.calculateStats(startDateKey = "not-a-date", todayDateKey = "2026-05-01"))

        // Bilingual headline & ordinal check
        assertTrue(UiStrings.relationshipDurationHeadline(2, 3, 4, 825, AppLanguage.EN).contains("2 yrs"))
        assertTrue(UiStrings.relationshipDurationHeadline(2, 3, 4, 825, AppLanguage.RU).contains("2 года"))
        assertEquals("3rd Anniversary", UiStrings.anniversaryOrdinal(3, AppLanguage.EN))
        assertEquals("3-я годовщина", UiStrings.anniversaryOrdinal(3, AppLanguage.RU))
    }

    @Test
    fun `widget summary formatter formats days together and next reunion across states and languages`() {
        val formatter = com.example.model.WidgetSummaryFormatter

        val activeEn = formatter.buildDisplayData(
            snapshot = com.example.model.WidgetCoupleSnapshot(
                partnerOneName = "Alex",
                partnerTwoName = "Maria",
                relationshipStartDateKey = "2023-06-15",
                nextMeetDateKey = "2026-05-29",
                nextMeetLocation = "Paris",
                language = AppLanguage.EN
            ),
            todayDateKey = "2026-05-15"
        )
        assertEquals("Alex & Maria", activeEn.headerTitle)
        assertEquals("1,065", activeEn.togetherValue)
        assertEquals("days", activeEn.togetherUnit)
        assertEquals("14", activeEn.reunionValue)
        assertEquals("days left", activeEn.reunionUnit)
        assertTrue(activeEn.reunionSubtitle.contains("Paris"))
        assertTrue(activeEn.hasTogetherDate)
        assertTrue(activeEn.hasReunionDate)

        val todayRu = formatter.buildDisplayData(
            snapshot = com.example.model.WidgetCoupleSnapshot(
                partnerOneName = "Саша",
                partnerTwoName = "Маша",
                relationshipStartDateKey = "2024-05-15",
                nextMeetDateKey = "2026-05-15",
                nextMeetLocation = "Москва",
                language = AppLanguage.RU
            ),
            todayDateKey = "2026-05-15"
        )
        assertEquals("Сегодня!", todayRu.reunionValue)
        assertTrue(todayRu.isReunionToday)
        assertTrue(todayRu.isAnniversaryToday)

        val emptyEn = formatter.buildDisplayData(
            snapshot = com.example.model.WidgetCoupleSnapshot(language = AppLanguage.EN),
            todayDateKey = "2026-05-15"
        )
        assertEquals("Duet", emptyEn.headerTitle)
        assertEquals("Set date", emptyEn.togetherValue)
        assertEquals("Plan date", emptyEn.reunionValue)
        assertTrue(!emptyEn.hasTogetherDate)
        assertTrue(!emptyEn.hasReunionDate)
    }

    @Test
    fun `journal entries stay in creation order when adding or removing emoji reactions`() {
        val decks = TopicCatalog.decks
        val qOlder = decks[0].questions[0]
        val qNewer = decks[0].questions[1]

        val olderAnswer = com.example.model.QuestionAnswer(
            id = "ans_old",
            questionId = qOlder.id,
            topicId = decks[0].id,
            authorUid = "u1",
            text = "Older answer",
            reaction = "",
            createdAt = com.google.firebase.Timestamp(1000L, 0),
            updatedAt = com.google.firebase.Timestamp(1000L, 0)
        )
        val newerAnswer = com.example.model.QuestionAnswer(
            id = "ans_new",
            questionId = qNewer.id,
            topicId = decks[0].id,
            authorUid = "u2",
            text = "Newer answer",
            reaction = "",
            createdAt = com.google.firebase.Timestamp(2000L, 0),
            updatedAt = com.google.firebase.Timestamp(2000L, 0)
        )

        val initialOrder = com.example.ui.buildJournalEntries(listOf(olderAnswer, newerAnswer), decks)
        assertEquals(listOf(qNewer.id, qOlder.id), initialOrder.map { it.question.id })

        // Reacting bumps updatedAt to a newer timestamp; order must not change
        val reactedOlder = olderAnswer.copy(
            reaction = "❤️",
            updatedAt = com.google.firebase.Timestamp(5000L, 0)
        )
        val afterReactOrder = com.example.ui.buildJournalEntries(listOf(reactedOlder, newerAnswer), decks)
        assertEquals(listOf(qNewer.id, qOlder.id), afterReactOrder.map { it.question.id })

        // Removing emoji also bumps updatedAt; order must still remain unchanged
        val clearedOlder = reactedOlder.copy(
            reaction = "",
            updatedAt = com.google.firebase.Timestamp(6000L, 0)
        )
        val afterClearOrder = com.example.ui.buildJournalEntries(listOf(clearedOlder, newerAnswer), decks)
        assertEquals(listOf(qNewer.id, qOlder.id), afterClearOrder.map { it.question.id })
    }
}
