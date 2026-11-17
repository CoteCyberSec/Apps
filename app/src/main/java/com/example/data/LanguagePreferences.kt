package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.CoupleAestheticCatalog
import com.example.model.WidgetCoupleSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.duetDataStore by preferencesDataStore(name = "duet_preferences")

data class LanguageSettings(
    val primaryLanguage: AppLanguage = AppLanguage.EN,
    val showSecondaryLanguage: Boolean = true,
    val seenAnswerIds: Set<String> = emptySet(),
    val notifiedAnswerIds: Set<String> = emptySet(),
    val lastAestheticId: String = CoupleAestheticCatalog.DEFAULT_ID
)

class LanguagePreferences(private val context: Context) {
    private object Keys {
        val PRIMARY_LANGUAGE = stringPreferencesKey("primary_language")
        val SHOW_SECONDARY = booleanPreferencesKey("show_secondary_language")
        val SEEN_ANSWER_IDS = stringSetPreferencesKey("seen_answer_ids")
        val NOTIFIED_ANSWER_IDS = stringSetPreferencesKey("notified_answer_ids")
        val LAST_AESTHETIC_ID = stringPreferencesKey("last_aesthetic_id")
        val WIDGET_PARTNER_ONE_NAME = stringPreferencesKey("widget_partner_one_name")
        val WIDGET_PARTNER_TWO_NAME = stringPreferencesKey("widget_partner_two_name")
        val WIDGET_RELATIONSHIP_START_DATE = stringPreferencesKey("widget_relationship_start_date")
        val WIDGET_NEXT_MEET_DATE = stringPreferencesKey("widget_next_meet_date")
        val WIDGET_NEXT_MEET_LOCATION = stringPreferencesKey("widget_next_meet_location")
    }

    val settingsFlow: Flow<LanguageSettings> = context.applicationContext.duetDataStore.data.map { prefs ->
        LanguageSettings(
            primaryLanguage = AppLanguage.fromCode(prefs[Keys.PRIMARY_LANGUAGE]),
            showSecondaryLanguage = prefs[Keys.SHOW_SECONDARY] ?: true,
            seenAnswerIds = prefs[Keys.SEEN_ANSWER_IDS] ?: emptySet(),
            notifiedAnswerIds = prefs[Keys.NOTIFIED_ANSWER_IDS] ?: emptySet(),
            lastAestheticId = prefs[Keys.LAST_AESTHETIC_ID]?.takeIf {
                CoupleAestheticCatalog.isValidThemeId(it)
            } ?: CoupleAestheticCatalog.DEFAULT_ID
        )
    }

    val widgetSnapshotFlow: Flow<WidgetCoupleSnapshot> =
        context.applicationContext.duetDataStore.data.map { prefs ->
            snapshotFromPrefs(prefs)
        }

    suspend fun readWidgetSnapshot(): WidgetCoupleSnapshot {
        val prefs = context.applicationContext.duetDataStore.data.first()
        return snapshotFromPrefs(prefs)
    }

    suspend fun saveWidgetSnapshotIfChanged(snapshot: WidgetCoupleSnapshot): Boolean {
        var changed = false
        context.applicationContext.duetDataStore.edit { prefs ->
            val current = snapshotFromPrefs(prefs)
            if (current != snapshot) {
                changed = true
                prefs[Keys.WIDGET_PARTNER_ONE_NAME] = snapshot.partnerOneName
                prefs[Keys.WIDGET_PARTNER_TWO_NAME] = snapshot.partnerTwoName
                prefs[Keys.WIDGET_RELATIONSHIP_START_DATE] = snapshot.relationshipStartDateKey
                prefs[Keys.WIDGET_NEXT_MEET_DATE] = snapshot.nextMeetDateKey
                prefs[Keys.WIDGET_NEXT_MEET_LOCATION] = snapshot.nextMeetLocation
                if (CoupleAestheticCatalog.isValidThemeId(snapshot.aestheticThemeId)) {
                    prefs[Keys.LAST_AESTHETIC_ID] = snapshot.aestheticThemeId
                }
                prefs[Keys.PRIMARY_LANGUAGE] = snapshot.language.code
            }
        }
        return changed
    }

    private fun snapshotFromPrefs(prefs: Preferences): WidgetCoupleSnapshot {
        val safeAesthetic = prefs[Keys.LAST_AESTHETIC_ID]?.takeIf {
            CoupleAestheticCatalog.isValidThemeId(it)
        } ?: CoupleAestheticCatalog.DEFAULT_ID
        return WidgetCoupleSnapshot(
            partnerOneName = prefs[Keys.WIDGET_PARTNER_ONE_NAME] ?: "",
            partnerTwoName = prefs[Keys.WIDGET_PARTNER_TWO_NAME] ?: "",
            relationshipStartDateKey = prefs[Keys.WIDGET_RELATIONSHIP_START_DATE] ?: "",
            nextMeetDateKey = prefs[Keys.WIDGET_NEXT_MEET_DATE] ?: "",
            nextMeetLocation = prefs[Keys.WIDGET_NEXT_MEET_LOCATION] ?: "",
            aestheticThemeId = safeAesthetic,
            language = AppLanguage.fromCode(prefs[Keys.PRIMARY_LANGUAGE])
        )
    }

    suspend fun setPrimaryLanguage(language: AppLanguage) {
        context.applicationContext.duetDataStore.edit { prefs ->
            prefs[Keys.PRIMARY_LANGUAGE] = language.code
        }
    }

    suspend fun setShowSecondaryLanguage(show: Boolean) {
        context.applicationContext.duetDataStore.edit { prefs ->
            prefs[Keys.SHOW_SECONDARY] = show
        }
    }

    suspend fun setLastAestheticId(aestheticId: String) {
        if (!CoupleAestheticCatalog.isValidThemeId(aestheticId)) return
        context.applicationContext.duetDataStore.edit { prefs ->
            prefs[Keys.LAST_AESTHETIC_ID] = aestheticId
        }
    }

    suspend fun markAnswersSeen(answerIds: Collection<String>) {
        if (answerIds.isEmpty()) return
        context.applicationContext.duetDataStore.edit { prefs ->
            val current = prefs[Keys.SEEN_ANSWER_IDS] ?: emptySet()
            prefs[Keys.SEEN_ANSWER_IDS] = current + answerIds
        }
    }

    suspend fun markAnswersNotified(answerIds: Collection<String>) {
        if (answerIds.isEmpty()) return
        context.applicationContext.duetDataStore.edit { prefs ->
            val current = prefs[Keys.NOTIFIED_ANSWER_IDS] ?: emptySet()
            prefs[Keys.NOTIFIED_ANSWER_IDS] = current + answerIds
        }
    }
}
