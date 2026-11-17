package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CoupleRepository
import com.example.data.LanguagePreferences
import com.example.data.LanguageSettings
import com.example.data.PartnerNotificationHelper
import com.example.data.PhotoMetadataHelper
import com.example.model.AppLanguage
import com.example.model.CoupleAestheticCatalog
import com.example.model.CoupleSpace
import com.example.model.CustomQuestion
import com.example.model.DailyDrawing
import com.example.model.PromptQuestion
import com.example.model.QuestionAnswer
import com.example.model.TopicCatalog
import com.example.model.TopicDeck
import com.example.model.TripMemory
import com.example.model.TripPhoto
import com.example.model.UserProfile
import com.example.model.WidgetCoupleSnapshot
import com.example.widget.WidgetUpdateHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PartnerAnswerNotification(
    val answer: QuestionAnswer,
    val question: PromptQuestion,
    val deck: TopicDeck,
    val unseen: Boolean
)

sealed interface DuetUiState {
    data object Loading : DuetUiState
    data class Ready(
        val userProfile: UserProfile?,
        val coupleSpace: CoupleSpace,
        val answers: List<QuestionAnswer>,
        val customQuestions: List<CustomQuestion>,
        val drawings: List<DailyDrawing>,
        val trips: List<TripMemory>,
        val tripPhotos: List<TripPhoto>,
        val decks: List<TopicDeck>,
        val dailyPrompt: PromptQuestion,
        val partnerNotifications: List<PartnerAnswerNotification>
    ) : DuetUiState
    data class Error(val message: String) : DuetUiState
}

class DuetViewModel(
    private val appContext: Context,
    private val repository: CoupleRepository,
    private val languagePreferences: LanguagePreferences,
    private val currentUserId: String,
    private val fallbackDisplayName: String
) : ViewModel() {

    private val _bootstrapError = MutableStateFlow<String?>(null)
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _dailyPromptIndex = MutableStateFlow(0)

    val languageSettings: StateFlow<LanguageSettings> = languagePreferences.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = LanguageSettings()
        )

    private val activeCoupleId = MutableStateFlow<String?>(null)
    private var lastSyncedWidgetSnapshot: WidgetCoupleSnapshot? = null

    init {
        bootstrap()
    }

    fun bootstrap() {
        viewModelScope.launch {
            _bootstrapError.value = null
            val lang = languageSettings.value
            val result = repository.bootstrapUserAndCouple(
                userId = currentUserId,
                fallbackName = fallbackDisplayName,
                language = lang.primaryLanguage,
                showSecondary = lang.showSecondaryLanguage
            )
            result.onSuccess { coupleId ->
                activeCoupleId.value = coupleId
            }.onFailure { err ->
                _bootstrapError.value = err.localizedMessage ?: "Failed to initialize couple space"
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DuetUiState> = combine(
        _bootstrapError,
        activeCoupleId,
        _dailyPromptIndex,
        languageSettings
    ) { bootErr, coupleId, promptIdx, langPrefs ->
        Quad(bootErr, coupleId, promptIdx, langPrefs)
    }.flatMapLatest { (bootErr, coupleId, promptIdx, langPrefs) ->
        if (bootErr != null) {
            flowOf(DuetUiState.Error(bootErr))
        } else if (coupleId.isNullOrBlank()) {
            flowOf(DuetUiState.Loading)
        } else {
            val profileFlow = repository.observeUserProfile(currentUserId)
            val coupleFlow = repository.observeCoupleSpace(currentUserId, coupleId)
            val answersFlow = repository.observeAnswers(currentUserId, coupleId)
            val customFlow = repository.observeCustomQuestions(currentUserId, coupleId)
            val drawingsFlow = repository.observeDrawings(currentUserId, coupleId)
            val tripsBundleFlow = combine(
                repository.observeTrips(currentUserId, coupleId),
                repository.observeTripPhotos(currentUserId, coupleId)
            ) { trips, photos -> trips to photos }

            val primaryDataFlow = combine(
                profileFlow,
                coupleFlow,
                answersFlow,
                customFlow,
                drawingsFlow
            ) { profile, couple, answers, customQuestions, drawings ->
                CoreCoupleData(profile, couple, answers, customQuestions, drawings)
            }

            combine(primaryDataFlow, tripsBundleFlow) { core, (trips, tripPhotos) ->
                val profile = core.profile
                val couple = core.couple
                val answers = core.answers
                val customQuestions = core.customQuestions
                val drawings = core.drawings

                val linkedId = profile?.coupleId
                if (!linkedId.isNullOrBlank() && linkedId != activeCoupleId.value) {
                    activeCoupleId.value = linkedId
                }

                if (couple == null) {
                    DuetUiState.Loading
                } else {
                    if (couple.memberUids.size == 2) {
                        val needsSync = answers.any { it.authorUid == currentUserId && it.memberUids != couple.memberUids } ||
                            customQuestions.any { it.authorUid == currentUserId && it.memberUids != couple.memberUids } ||
                            drawings.any { it.authorUid == currentUserId && it.memberUids != couple.memberUids } ||
                            trips.any { it.authorUid == currentUserId && it.memberUids != couple.memberUids } ||
                            tripPhotos.any { it.authorUid == currentUserId && it.memberUids != couple.memberUids }
                        if (needsSync) {
                            viewModelScope.launch {
                                repository.syncMemberUidsAfterPartnerJoin(
                                    couple = couple,
                                    answers = answers,
                                    customQuestions = customQuestions,
                                    drawings = drawings,
                                    trips = trips,
                                    tripPhotos = tripPhotos
                                )
                            }
                        }
                    }

                    val mergedDecks = TopicCatalog.decks.map { deck ->
                        val extra = customQuestions
                            .filter { it.topicId == deck.id }
                            .sortedBy { it.createdAt?.seconds ?: 0L }
                            .map { it.toPromptQuestion() }
                        deck.copy(questions = deck.questions + extra)
                    }
                    val allQuestions = mergedDecks.flatMap { it.questions }
                    val safePrompt = if (allQuestions.isNotEmpty()) {
                        allQuestions[promptIdx.mod(allQuestions.size)]
                    } else {
                        TopicCatalog.allBuiltInQuestions.first()
                    }

                    val questionLookup = mergedDecks.flatMap { d ->
                        d.questions.map { q -> q.id to (q to d) }
                    }.toMap()

                    val partnerAnswers = answers
                        .filter { it.authorUid != currentUserId || it.speakerRole == "partner_local" }
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }

                    val partnerNotifications = partnerAnswers.mapNotNull { ans ->
                        val pair = questionLookup[ans.questionId] ?: return@mapNotNull null
                        PartnerAnswerNotification(
                            answer = ans,
                            question = pair.first,
                            deck = pair.second,
                            unseen = ans.id !in langPrefs.seenAnswerIds
                        )
                    }

                    // Trigger OS system notifications for newly synced remote partner answers
                    val toNotify = partnerNotifications.filter {
                        it.answer.authorUid != currentUserId &&
                            it.answer.id !in langPrefs.notifiedAnswerIds
                    }
                    if (toNotify.isNotEmpty()) {
                        toNotify.forEach { item ->
                            PartnerNotificationHelper.notifyPartnerAnswer(
                                context = appContext,
                                answerId = item.answer.id,
                                partnerName = item.answer.authorLabel,
                                questionText = item.question.text.primary(langPrefs.primaryLanguage),
                                language = langPrefs.primaryLanguage
                            )
                        }
                        viewModelScope.launch {
                            languagePreferences.markAnswersNotified(toNotify.map { it.answer.id })
                        }
                    }

                    val widgetSnapshot = WidgetCoupleSnapshot(
                        partnerOneName = couple.partnerOneName,
                        partnerTwoName = couple.partnerTwoName,
                        relationshipStartDateKey = couple.relationshipStartDateKey,
                        nextMeetDateKey = couple.nextMeetDateKey,
                        nextMeetLocation = couple.nextMeetLocation,
                        aestheticThemeId = couple.aestheticThemeId.ifBlank { CoupleAestheticCatalog.DEFAULT_ID },
                        language = langPrefs.primaryLanguage
                    )
                    if (widgetSnapshot != lastSyncedWidgetSnapshot) {
                        lastSyncedWidgetSnapshot = widgetSnapshot
                        viewModelScope.launch {
                            WidgetUpdateHelper.syncSnapshotAndUpdateWidgets(
                                context = appContext,
                                preferences = languagePreferences,
                                snapshot = widgetSnapshot
                            )
                        }
                    }

                    DuetUiState.Ready(
                        userProfile = profile,
                        coupleSpace = couple,
                        answers = answers,
                        customQuestions = customQuestions,
                        drawings = drawings.sortedWith(
                            compareByDescending<DailyDrawing> { it.dateKey }
                                .thenByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
                        ),
                        trips = trips.sortedWith(
                            compareByDescending<TripMemory> { it.startDateKey.ifBlank { "0000-00-00" } }
                                .thenByDescending { it.createdAt?.seconds ?: 0L }
                        ),
                        tripPhotos = tripPhotos.sortedWith(
                            compareByDescending<TripPhoto> { it.takenDateKey }
                                .thenBy { it.takenMillis }
                        ),
                        decks = mergedDecks,
                        dailyPrompt = safePrompt,
                        partnerNotifications = partnerNotifications
                    )
                }
            }.catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing couple space", rootError)
                emit(DuetUiState.Error(rootError.localizedMessage ?: "Unable to load couple space"))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DuetUiState.Loading
    )

    fun shuffleDailyPrompt() {
        _dailyPromptIndex.value += 1
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun setPrimaryLanguage(language: AppLanguage) {
        viewModelScope.launch {
            languagePreferences.setPrimaryLanguage(language)
            repository.updateLanguagePreference(
                userId = currentUserId,
                language = language,
                showSecondary = languageSettings.value.showSecondaryLanguage
            )
        }
    }

    fun toggleSecondaryLanguage() {
        viewModelScope.launch {
            val next = !languageSettings.value.showSecondaryLanguage
            languagePreferences.setShowSecondaryLanguage(next)
            repository.updateLanguagePreference(
                userId = currentUserId,
                language = languageSettings.value.primaryLanguage,
                showSecondary = next
            )
        }
    }

    fun markAnswerSeen(answerId: String) {
        viewModelScope.launch {
            languagePreferences.markAnswersSeen(listOf(answerId))
        }
    }

    fun markAllAnswersSeen() {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        val ids = ready.partnerNotifications.filter { it.unseen }.map { it.answer.id }
        if (ids.isNotEmpty()) {
            viewModelScope.launch {
                languagePreferences.markAnswersSeen(ids)
            }
        }
    }

    fun saveAnswer(
        question: PromptQuestion,
        speakerRole: String,
        authorLabel: String,
        text: String,
        existingAnswer: QuestionAnswer?,
        successMsg: String
    ) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveAnswer(
                couple = ready.coupleSpace,
                questionId = question.id,
                topicId = question.topicId,
                speakerRole = speakerRole,
                authorLabel = authorLabel,
                text = text,
                existingAnswer = existingAnswer
            )
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = successMsg
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to save answer"
            }
        }
    }

    fun reactToAnswer(answerId: String, reaction: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            val result = repository.reactToAnswer(ready.coupleSpace.id, answerId, reaction)
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to add reaction"
            }
        }
    }

    fun saveDrawing(
        dateKey: String,
        speakerRole: String,
        authorLabel: String,
        caption: String,
        encodedStrokes: List<String>,
        existingDrawing: DailyDrawing?,
        successMsg: String
    ) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveDrawing(
                couple = ready.coupleSpace,
                dateKey = dateKey,
                speakerRole = speakerRole,
                authorLabel = authorLabel,
                caption = caption,
                encodedStrokes = encodedStrokes,
                existingDrawing = existingDrawing
            )
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = successMsg
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to save drawing"
            }
        }
    }

    fun reactToDrawing(drawingId: String, reaction: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            val result = repository.reactToDrawing(ready.coupleSpace.id, drawingId, reaction)
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to react to drawing"
            }
        }
    }

    fun deleteDrawing(drawingId: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            val result = repository.deleteDrawing(ready.coupleSpace.id, drawingId)
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to delete drawing"
            }
        }
    }

    fun addCustomQuestion(topicId: String, promptEn: String, promptRu: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.addCustomQuestion(
                couple = ready.coupleSpace,
                topicId = topicId,
                promptEn = promptEn,
                promptRu = promptRu
            )
            _isBusy.value = false
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to create question"
            }
        }
    }

    fun updateMyProfileIdentity(displayName: String, avatarId: String, savedText: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateMyProfileIdentity(
                couple = ready.coupleSpace,
                displayName = displayName,
                avatarId = avatarId
            )
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = savedText
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to update profile"
            }
        }
    }

    fun updateNextReunion(dateKey: String, location: String, savedText: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateNextReunion(
                coupleId = ready.coupleSpace.id,
                dateKey = dateKey,
                location = location
            )
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = savedText
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to save reunion date"
            }
        }
    }

    fun updateRelationshipStartDate(dateKey: String, savedText: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateRelationshipStartDate(
                coupleId = ready.coupleSpace.id,
                dateKey = dateKey
            )
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = savedText
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to save relationship start date"
            }
        }
    }

    fun updateCoupleAesthetic(aestheticThemeId: String, savedText: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            languagePreferences.setLastAestheticId(aestheticThemeId)
            val result = repository.updateCoupleAesthetic(
                coupleId = ready.coupleSpace.id,
                aestheticThemeId = aestheticThemeId
            )
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = savedText
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to update aesthetic"
            }
        }
    }

    fun joinPartnerSpace(inviteCode: String, partnerName: String) {
        val ready = uiState.value as? DuetUiState.Ready
        val myName = ready?.userProfile?.displayName?.takeIf { it.isNotBlank() }
            ?: partnerName.ifBlank { fallbackDisplayName }
        val myAvatar = ready?.userProfile?.avatarId?.takeIf { it.isNotBlank() } ?: "moon_bunny"
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.joinCoupleByInviteCode(
                userId = currentUserId,
                partnerDisplayName = myName,
                partnerAvatarId = myAvatar,
                rawCode = inviteCode
            )
            _isBusy.value = false
            result.onSuccess { newCoupleId ->
                activeCoupleId.value = newCoupleId
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not join couple space"
            }
        }
    }

    fun saveTrip(
        title: String,
        location: String,
        startDateKey: String,
        endDateKey: String,
        notes: String,
        existingTrip: TripMemory?,
        savedText: String,
        onSavedId: (String) -> Unit = {}
    ) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveTrip(
                couple = ready.coupleSpace,
                title = title,
                location = location,
                startDateKey = startDateKey,
                endDateKey = endDateKey,
                notes = notes,
                existingTrip = existingTrip
            )
            _isBusy.value = false
            result.onSuccess { id ->
                _actionMessage.value = savedText
                onSavedId(id)
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to save trip"
            }
        }
    }

    fun deleteTrip(tripId: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        val photosForTrip = ready.tripPhotos.filter { it.tripId == tripId }
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteTrip(ready.coupleSpace.id, tripId, photosForTrip)
            _isBusy.value = false
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to delete trip"
            }
        }
    }

    fun addTripPhotos(
        tripId: String,
        uris: List<Uri>,
        authorLabel: String,
        successMessageBuilder: (Int) -> String
    ) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isBusy.value = true
            var uploadedCount = 0
            var lastError: String? = null
            uris.forEachIndexed { idx, uri ->
                val photoId = "tp_${System.currentTimeMillis()}_${idx}_${currentUserId.take(8)}"
                val processed = withContext(Dispatchers.IO) {
                    PhotoMetadataHelper.processPickedPhoto(appContext, uri)
                }
                if (processed != null) {
                    withContext(Dispatchers.IO) {
                        PhotoMetadataHelper.saveLocalPhotoFile(appContext, photoId, processed.localCacheBytes)
                    }
                    val res = repository.addTripPhoto(
                        couple = ready.coupleSpace,
                        tripId = tripId,
                        photoId = photoId,
                        processed = processed,
                        authorLabel = authorLabel,
                        caption = ""
                    )
                    if (res.isSuccess) {
                        uploadedCount++
                    } else {
                        lastError = res.exceptionOrNull()?.localizedMessage
                    }
                }
            }
            _isBusy.value = false
            if (uploadedCount > 0) {
                _actionMessage.value = successMessageBuilder(uploadedCount)
            } else if (lastError != null) {
                _actionMessage.value = lastError
            }
        }
    }

    fun updateTripPhoto(
        photoId: String,
        caption: String,
        takenDateKey: String,
        takenTimeLabel: String,
        takenMillis: Long,
        timestampSource: String,
        savedMsg: String
    ) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateTripPhoto(
                coupleId = ready.coupleSpace.id,
                photoId = photoId,
                caption = caption,
                takenDateKey = takenDateKey,
                takenTimeLabel = takenTimeLabel,
                takenMillis = takenMillis,
                timestampSource = timestampSource
            )
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = savedMsg
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to update photo"
            }
        }
    }

    fun reactToTripPhoto(photoId: String, reaction: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            val result = repository.reactToTripPhoto(ready.coupleSpace.id, photoId, reaction)
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to react to photo"
            }
        }
    }

    fun deleteTripPhoto(photoId: String) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        viewModelScope.launch {
            val result = repository.deleteTripPhoto(ready.coupleSpace.id, photoId)
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to delete photo"
            }
        }
    }

    fun deleteTripPhotos(
        photoIds: Collection<String>,
        successMessageBuilder: (Int) -> String
    ) {
        val ready = uiState.value as? DuetUiState.Ready ?: return
        val cleanIds = photoIds.distinct()
        if (cleanIds.isEmpty()) return
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteTripPhotos(ready.coupleSpace.id, cleanIds)
            _isBusy.value = false
            result.onSuccess {
                _actionMessage.value = successMessageBuilder(cleanIds.size)
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to delete selected photos"
            }
        }
    }

    private data class CoreCoupleData(
        val profile: UserProfile?,
        val couple: CoupleSpace?,
        val answers: List<QuestionAnswer>,
        val customQuestions: List<CustomQuestion>,
        val drawings: List<DailyDrawing>
    )

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    private companion object {
        const val TAG = "DuetViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
