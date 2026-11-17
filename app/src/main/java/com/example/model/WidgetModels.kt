package com.example.model

import java.util.Locale

data class WidgetCoupleSnapshot(
    val partnerOneName: String = "",
    val partnerTwoName: String = "",
    val relationshipStartDateKey: String = "",
    val nextMeetDateKey: String = "",
    val nextMeetLocation: String = "",
    val aestheticThemeId: String = CoupleAestheticCatalog.DEFAULT_ID,
    val language: AppLanguage = AppLanguage.EN
)

data class WidgetCountdownDisplayData(
    val headerTitle: String,
    val togetherEyebrow: String,
    val togetherValue: String,
    val togetherUnit: String,
    val togetherSubtitle: String,
    val reunionEyebrow: String,
    val reunionValue: String,
    val reunionUnit: String,
    val reunionSubtitle: String,
    val hasTogetherDate: Boolean,
    val hasReunionDate: Boolean,
    val isReunionToday: Boolean,
    val isAnniversaryToday: Boolean
)

object WidgetSummaryFormatter {
    fun buildDisplayData(
        snapshot: WidgetCoupleSnapshot,
        todayDateKey: String = ReunionDateHelper.todayDateKey()
    ): WidgetCountdownDisplayData {
        val lang = snapshot.language
        val p1 = snapshot.partnerOneName.trim()
        val p2 = snapshot.partnerTwoName.trim()
        val headerTitle = when {
            p1.isNotBlank() && p2.isNotBlank() -> "$p1 & $p2"
            p1.isNotBlank() -> p1
            p2.isNotBlank() -> p2
            else -> UiStrings.appTitle(lang)
        }

        val stats = if (snapshot.relationshipStartDateKey.isNotBlank()) {
            RelationshipTrackerHelper.calculateStats(
                startDateKey = snapshot.relationshipStartDateKey,
                todayDateKey = todayDateKey
            )
        } else null

        val togetherEyebrow = if (lang == AppLanguage.RU) "ВМЕСТЕ" else "TOGETHER"
        val togetherValue: String
        val togetherUnit: String
        val togetherSubtitle: String

        if (stats != null) {
            togetherValue = String.format(Locale.US, "%,d", stats.totalDays)
            togetherUnit = formatDaysWord(stats.totalDays, lang)
            togetherSubtitle = if (stats.isAnniversaryToday) {
                UiStrings.nextAnniversaryTitle(stats.nextAnniversaryNumber, isToday = true, lang = lang)
            } else {
                UiStrings.relationshipDurationHeadline(
                    years = stats.years,
                    months = stats.months,
                    days = stats.days,
                    totalDays = stats.totalDays,
                    lang = lang
                )
            }
        } else {
            togetherValue = if (lang == AppLanguage.RU) "Дата" else "Set date"
            togetherUnit = ""
            togetherSubtitle = if (lang == AppLanguage.RU) "Начало отношений" else "Tap to add start date"
        }

        val reunionDaysLeft = if (snapshot.nextMeetDateKey.isNotBlank()) {
            ReunionDateHelper.daysUntil(snapshot.nextMeetDateKey, todayDateKey)
        } else null

        val reunionEyebrow = if (lang == AppLanguage.RU) "ДО ВСТРЕЧИ" else "REUNION"
        val reunionValue: String
        val reunionUnit: String
        val reunionSubtitle: String

        if (reunionDaysLeft != null) {
            when {
                reunionDaysLeft > 0 -> {
                    reunionValue = String.format(Locale.US, "%,d", reunionDaysLeft)
                    reunionUnit = if (lang == AppLanguage.RU) {
                        formatDaysWord(reunionDaysLeft.toLong(), lang)
                    } else {
                        if (reunionDaysLeft == 1) "day left" else "days left"
                    }
                }
                reunionDaysLeft == 0 -> {
                    reunionValue = if (lang == AppLanguage.RU) "Сегодня!" else "Today!"
                    reunionUnit = ""
                }
                else -> {
                    reunionValue = if (lang == AppLanguage.RU) "Прошла" else "Passed"
                    reunionUnit = ""
                }
            }
            val dateText = ReunionDateHelper.formatDateDisplay(snapshot.nextMeetDateKey, lang)
            val loc = snapshot.nextMeetLocation.trim()
            reunionSubtitle = if (loc.isNotBlank()) "$loc • $dateText" else dateText
        } else {
            reunionValue = if (lang == AppLanguage.RU) "Дата" else "Plan date"
            reunionUnit = ""
            reunionSubtitle = if (lang == AppLanguage.RU) "Укажите встречу" else "Tap to set reunion"
        }

        return WidgetCountdownDisplayData(
            headerTitle = headerTitle,
            togetherEyebrow = togetherEyebrow,
            togetherValue = togetherValue,
            togetherUnit = togetherUnit,
            togetherSubtitle = togetherSubtitle,
            reunionEyebrow = reunionEyebrow,
            reunionValue = reunionValue,
            reunionUnit = reunionUnit,
            reunionSubtitle = reunionSubtitle,
            hasTogetherDate = stats != null,
            hasReunionDate = reunionDaysLeft != null,
            isReunionToday = reunionDaysLeft == 0,
            isAnniversaryToday = stats?.isAnniversaryToday == true
        )
    }

    private fun formatDaysWord(days: Long, lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> if (days == 1L) "day" else "days"
        AppLanguage.RU -> {
            val mod100 = (days % 100L).toInt()
            val mod10 = (days % 10L).toInt()
            when {
                mod100 in 11..14 -> "дней"
                mod10 == 1 -> "день"
                mod10 in 2..4 -> "дня"
                else -> "дней"
            }
        }
    }
}
