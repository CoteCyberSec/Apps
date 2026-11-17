package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AppLanguage
import com.example.model.CoupleAestheticCatalog
import com.example.model.CoupleInvite
import com.example.model.CoupleSpace
import com.example.model.CustomQuestion
import com.example.model.DailyDrawing
import com.example.model.EmojiReactionHelper
import com.example.model.QuestionAnswer
import com.example.model.TripMemory
import com.example.model.TripPhoto
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

class CoupleRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val random = SecureRandom()

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun sanitizeIdSegment(raw: String, maxLen: Int = 40): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifBlank { "item" }.take(maxLen)
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return buildString(6) {
            repeat(6) {
                append(chars[random.nextInt(chars.length)])
            }
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user profile stream." }
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeCoupleSpace(userId: String, coupleId: String): Flow<CoupleSpace?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user couple observation." }
        val path = "couples/$coupleId"
        emitAll(
            db.collection("couples").document(coupleId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(
                        CoupleSpace::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeAnswers(userId: String, coupleId: String): Flow<List<QuestionAnswer>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user answers observation." }
        val path = "couples/$coupleId/answers"
        emitAll(
            db.collection("couples").document(coupleId)
                .collection("answers")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        QuestionAnswer::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeCustomQuestions(userId: String, coupleId: String): Flow<List<CustomQuestion>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user custom questions observation." }
        val path = "couples/$coupleId/customQuestions"
        emitAll(
            db.collection("couples").document(coupleId)
                .collection("customQuestions")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CustomQuestion::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeDrawings(userId: String, coupleId: String): Flow<List<DailyDrawing>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user drawings observation." }
        val path = "couples/$coupleId/drawings"
        emitAll(
            db.collection("couples").document(coupleId)
                .collection("drawings")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DailyDrawing::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeTrips(userId: String, coupleId: String): Flow<List<TripMemory>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user trips observation." }
        val path = "couples/$coupleId/trips"
        emitAll(
            db.collection("couples").document(coupleId)
                .collection("trips")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TripMemory::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeTripPhotos(userId: String, coupleId: String): Flow<List<TripPhoto>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user trip photos observation." }
        val path = "couples/$coupleId/tripPhotos"
        emitAll(
            db.collection("couples").document(coupleId)
                .collection("tripPhotos")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TripPhoto::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    private fun handleFlowError(error: Throwable, op: OperationType, path: String, expectedUid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }

    suspend fun bootstrapUserAndCouple(
        userId: String,
        fallbackName: String,
        language: AppLanguage,
        showSecondary: Boolean
    ): Result<String> {
        var currentPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Authenticated user mismatch." }
            val safeName = fallbackName.trim().ifBlank { "Partner 1" }.take(80)

            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            val existingProfile = if (userSnap.exists()) {
                userSnap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null

            val existingCoupleId = existingProfile?.coupleId?.takeIf { it.isNotBlank() }
            if (existingCoupleId != null) {
                return Result.success(existingCoupleId)
            }

            currentPath = "couples"
            val existingCouples = db.collection("couples")
                .whereArrayContains("memberUids", uid)
                .get()
                .await()
            val foundCoupleId = existingCouples.documents.firstOrNull()?.id
            if (foundCoupleId != null) {
                if (existingProfile == null) {
                    userRef.set(
                        mapOf(
                            "userId" to uid,
                            "displayName" to safeName,
                            "coupleId" to foundCoupleId,
                            "preferredLanguage" to language.code,
                            "showSecondaryLanguage" to showSecondary,
                            "avatarId" to "sunny_cat",
                            "nameConfirmed" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } else {
                    userRef.update(
                        mapOf(
                            "coupleId" to foundCoupleId,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
                return Result.success(foundCoupleId)
            }

            val coupleId = "couple_${sanitizeIdSegment(uid, 16)}_${System.currentTimeMillis()}"
            val inviteCode = generateInviteCode()
            currentPath = "couples/$coupleId"

            val batch = db.batch()
            batch.set(
                db.collection("couples").document(coupleId),
                mapOf(
                    "creatorUid" to uid,
                    "memberUids" to listOf(uid),
                    "partnerOneName" to (existingProfile?.displayName?.take(80) ?: safeName),
                    "partnerTwoName" to "",
                    "partnerOneAvatarId" to (existingProfile?.avatarId?.takeIf { it.isNotBlank() } ?: "sunny_cat"),
                    "partnerTwoAvatarId" to "moon_bunny",
                    "inviteCode" to inviteCode,
                    "anniversaryLabel" to "",
                    "nextMeetDateKey" to "",
                    "nextMeetLocation" to "",
                    "relationshipStartDateKey" to "",
                    "aestheticThemeId" to CoupleAestheticCatalog.DEFAULT_ID,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("coupleInvites").document(inviteCode),
                mapOf(
                    "coupleId" to coupleId,
                    "creatorUid" to uid,
                    "creatorName" to (existingProfile?.displayName?.take(80) ?: safeName),
                    "claimed" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (existingProfile == null) {
                batch.set(
                    userRef,
                    mapOf(
                        "userId" to uid,
                        "displayName" to safeName,
                        "coupleId" to coupleId,
                        "preferredLanguage" to language.code,
                        "showSecondaryLanguage" to showSecondary,
                        "avatarId" to "sunny_cat",
                        "nameConfirmed" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    userRef,
                    mapOf(
                        "coupleId" to coupleId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(coupleId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, currentPath)
            Result.failure(e)
        }
    }

    suspend fun joinCoupleByInviteCode(
        userId: String,
        partnerDisplayName: String,
        partnerAvatarId: String,
        rawCode: String
    ): Result<String> {
        val code = rawCode.trim().uppercase()
        var currentPath = "coupleInvites/$code"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Authenticated user mismatch." }
            require(code.matches(Regex("^[A-Z0-9]{6}$"))) {
                "Invite code must be 6 letters/digits."
            }

            val inviteSnap = db.collection("coupleInvites").document(code).get().await()
            if (!inviteSnap.exists()) {
                return Result.failure(IllegalArgumentException("Invite code not found."))
            }
            val invite = inviteSnap.toObject(
                CoupleInvite::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: return Result.failure(IllegalArgumentException("Invalid invite record."))

            if (invite.claimed) {
                return Result.failure(IllegalStateException("This invite code has already been used."))
            }
            if (invite.creatorUid == uid) {
                return Result.failure(IllegalStateException("You cannot join your own invite code."))
            }

            val targetCoupleId = invite.coupleId
            require(targetCoupleId.isNotBlank()) { "Invite does not reference a couple space." }

            val safeName = partnerDisplayName.trim().ifBlank { "Partner 2" }.take(80)
            val safeAvatar = partnerAvatarId.trim().take(40)
                .takeIf { it.isNotEmpty() && it.matches(Regex("^[a-z0-9_]+$")) }
                ?: "moon_bunny"
            currentPath = "couples/$targetCoupleId"

            val batch = db.batch()
            batch.update(
                db.collection("couples").document(targetCoupleId),
                mapOf(
                    "memberUids" to FieldValue.arrayUnion(uid),
                    "partnerTwoName" to safeName,
                    "partnerTwoAvatarId" to safeAvatar,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("coupleInvites").document(code),
                mapOf(
                    "claimed" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("users").document(uid),
                mapOf(
                    "coupleId" to targetCoupleId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(targetCoupleId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, currentPath)
            Result.failure(e)
        }
    }

    suspend fun syncMemberUidsAfterPartnerJoin(
        couple: CoupleSpace,
        answers: List<QuestionAnswer>,
        customQuestions: List<CustomQuestion>,
        drawings: List<DailyDrawing>,
        trips: List<TripMemory>,
        tripPhotos: List<TripPhoto>
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.success(Unit)
        if (couple.id.isBlank() || couple.memberUids.size < 2) return Result.success(Unit)

        val staleAnswers = answers.filter {
            it.authorUid == currentUid && it.memberUids != couple.memberUids
        }
        val staleQuestions = customQuestions.filter {
            it.authorUid == currentUid && it.memberUids != couple.memberUids
        }
        val staleDrawings = drawings.filter {
            it.authorUid == currentUid && it.memberUids != couple.memberUids
        }
        val staleTrips = trips.filter {
            it.authorUid == currentUid && it.memberUids != couple.memberUids
        }
        val stalePhotos = tripPhotos.filter {
            it.authorUid == currentUid && it.memberUids != couple.memberUids
        }
        if (staleAnswers.isEmpty() && staleQuestions.isEmpty() && staleDrawings.isEmpty() &&
            staleTrips.isEmpty() && stalePhotos.isEmpty()
        ) {
            return Result.success(Unit)
        }

        val path = "couples/${couple.id}"
        return try {
            val batch = db.batch()
            staleAnswers.forEach { ans ->
                batch.update(
                    db.collection("couples").document(couple.id)
                        .collection("answers").document(ans.id),
                    mapOf(
                        "memberUids" to couple.memberUids,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            staleQuestions.forEach { q ->
                batch.update(
                    db.collection("couples").document(couple.id)
                        .collection("customQuestions").document(q.id),
                    mapOf(
                        "memberUids" to couple.memberUids,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            staleDrawings.forEach { d ->
                batch.update(
                    db.collection("couples").document(couple.id)
                        .collection("drawings").document(d.id),
                    mapOf(
                        "memberUids" to couple.memberUids,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            staleTrips.forEach { t ->
                batch.update(
                    db.collection("couples").document(couple.id)
                        .collection("trips").document(t.id),
                    mapOf(
                        "memberUids" to couple.memberUids,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            stalePhotos.forEach { p ->
                batch.update(
                    db.collection("couples").document(couple.id)
                        .collection("tripPhotos").document(p.id),
                    mapOf(
                        "memberUids" to couple.memberUids,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateMyProfileIdentity(
        couple: CoupleSpace,
        displayName: String,
        avatarId: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be signed in."))
        if (uid !in couple.memberUids) {
            return Result.failure(IllegalStateException("Not a member of this couple space."))
        }
        val cleanName = displayName.trim().take(80)
        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty."))
        }
        val cleanAvatar = avatarId.trim().take(40)
        require(cleanAvatar.matches(Regex("^[a-z0-9_]*$"))) { "Invalid avatar ID." }
        val ownSlot = com.example.model.AvatarCatalog.slotForUser(couple, uid)
        val nameField = if (ownSlot == 2) "partnerTwoName" else "partnerOneName"
        val avatarField = if (ownSlot == 2) "partnerTwoAvatarId" else "partnerOneAvatarId"
        val path = "couples/${couple.id}"
        return try {
            val batch = db.batch()
            batch.update(
                db.collection("couples").document(couple.id),
                mapOf(
                    nameField to cleanName,
                    avatarField to cleanAvatar,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("users").document(uid),
                mapOf(
                    "displayName" to cleanName,
                    "avatarId" to cleanAvatar,
                    "nameConfirmed" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateNextReunion(
        coupleId: String,
        dateKey: String,
        location: String
    ): Result<Unit> {
        val path = "couples/$coupleId"
        return try {
            requireUserId()
            val cleanDate = dateKey.trim()
            require(cleanDate.isEmpty() || cleanDate.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) {
                "Invalid reunion date format."
            }
            val cleanLocation = location.trim().take(100)
            db.collection("couples").document(coupleId)
                .update(
                    mapOf(
                        "nextMeetDateKey" to cleanDate,
                        "nextMeetLocation" to cleanLocation,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateRelationshipStartDate(
        coupleId: String,
        dateKey: String
    ): Result<Unit> {
        val path = "couples/$coupleId"
        return try {
            requireUserId()
            val cleanDate = dateKey.trim()
            require(cleanDate.isEmpty() || cleanDate.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) {
                "Invalid dating start date format."
            }
            db.collection("couples").document(coupleId)
                .update(
                    mapOf(
                        "relationshipStartDateKey" to cleanDate,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCoupleAesthetic(
        coupleId: String,
        aestheticThemeId: String
    ): Result<Unit> {
        val path = "couples/$coupleId"
        return try {
            requireUserId()
            val cleanThemeId = aestheticThemeId.trim().take(40)
            require(CoupleAestheticCatalog.isValidThemeId(cleanThemeId)) {
                "Invalid couple aesthetic theme."
            }
            db.collection("couples").document(coupleId)
                .update(
                    mapOf(
                        "aestheticThemeId" to cleanThemeId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateLanguagePreference(
        userId: String,
        language: AppLanguage,
        showSecondary: Boolean
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Authenticated user mismatch." }
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "preferredLanguage" to language.code,
                        "showSecondaryLanguage" to showSecondary,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveAnswer(
        couple: CoupleSpace,
        questionId: String,
        topicId: String,
        speakerRole: String,
        authorLabel: String,
        text: String,
        existingAnswer: QuestionAnswer?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be signed in."))
        if (existingAnswer != null && (existingAnswer.authorUid != uid || existingAnswer.speakerRole != "self")) {
            return Result.failure(IllegalAccessException("Only your partner can edit their own answer."))
        }
        val safeRole = "self"
        val answerId = existingAnswer?.id?.takeIf { it.isNotBlank() }
            ?: "${sanitizeIdSegment(questionId, 48)}_${safeRole}_${sanitizeIdSegment(uid, 20)}"
        val path = "couples/${couple.id}/answers/$answerId"

        return try {
            val trimmed = text.trim()
            require(trimmed.isNotEmpty() && trimmed.length <= 1500) {
                "Answer must be between 1 and 1500 characters."
            }
            val safeLabel = authorLabel.trim().ifBlank { "Partner" }.take(80)
            val docRef = db.collection("couples").document(couple.id)
                .collection("answers").document(answerId)

            if (existingAnswer != null) {
                docRef.update(
                    mapOf(
                        "text" to trimmed,
                        "authorLabel" to safeLabel,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "coupleId" to couple.id,
                    "questionId" to questionId,
                    "topicId" to topicId,
                    "authorUid" to uid,
                    "authorLabel" to safeLabel,
                    "speakerRole" to safeRole,
                    "text" to trimmed,
                    "reaction" to "",
                    "memberUids" to couple.memberUids,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun reactToAnswer(
        coupleId: String,
        answerId: String,
        reaction: String
    ): Result<Unit> {
        val path = "couples/$coupleId/answers/$answerId"
        return try {
            requireUserId()
            require(answerId.isNotBlank())
            val safeReaction = if (reaction.isBlank()) "" else EmojiReactionHelper.extractFirstEmojiGrapheme(reaction)
            db.collection("couples").document(coupleId)
                .collection("answers").document(answerId)
                .update(
                    mapOf(
                        "reaction" to safeReaction,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveDrawing(
        couple: CoupleSpace,
        dateKey: String,
        speakerRole: String,
        authorLabel: String,
        caption: String,
        encodedStrokes: List<String>,
        existingDrawing: DailyDrawing?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be signed in."))
        if (existingDrawing != null && (existingDrawing.authorUid != uid || existingDrawing.speakerRole != "self")) {
            return Result.failure(IllegalAccessException("Only your partner can edit their own drawing."))
        }
        val safeRole = "self"
        val drawingId = existingDrawing?.id?.takeIf { it.isNotBlank() }
            ?: "draw_${sanitizeIdSegment(dateKey, 12)}_${System.currentTimeMillis()}_${sanitizeIdSegment(uid, 12)}"
        val path = "couples/${couple.id}/drawings/$drawingId"

        return try {
            require(dateKey.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) {
                "Invalid drawing date key."
            }
            val cleanStrokes = encodedStrokes.filter { it.isNotBlank() }.take(150)
            require(cleanStrokes.isNotEmpty()) {
                "Draw at least one stroke before saving."
            }
            val safeLabel = authorLabel.trim().ifBlank { "Partner" }.take(80)
            val safeCaption = caption.trim().take(140)
            val docRef = db.collection("couples").document(couple.id)
                .collection("drawings").document(drawingId)

            if (existingDrawing != null) {
                docRef.update(
                    mapOf(
                        "strokes" to cleanStrokes,
                        "caption" to safeCaption,
                        "authorLabel" to safeLabel,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "coupleId" to couple.id,
                        "dateKey" to dateKey,
                        "authorUid" to uid,
                        "authorLabel" to safeLabel,
                        "speakerRole" to safeRole,
                        "caption" to safeCaption,
                        "strokes" to cleanStrokes,
                        "reaction" to "",
                        "memberUids" to couple.memberUids,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun reactToDrawing(
        coupleId: String,
        drawingId: String,
        reaction: String
    ): Result<Unit> {
        val path = "couples/$coupleId/drawings/$drawingId"
        return try {
            requireUserId()
            require(drawingId.isNotBlank())
            val safeReaction = if (reaction.isBlank()) "" else EmojiReactionHelper.extractFirstEmojiGrapheme(reaction)
            db.collection("couples").document(coupleId)
                .collection("drawings").document(drawingId)
                .update(
                    mapOf(
                        "reaction" to safeReaction,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteDrawing(
        coupleId: String,
        drawingId: String
    ): Result<Unit> {
        val path = "couples/$coupleId/drawings/$drawingId"
        return try {
            requireUserId()
            require(drawingId.isNotBlank())
            db.collection("couples").document(coupleId)
                .collection("drawings").document(drawingId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun addCustomQuestion(
        couple: CoupleSpace,
        topicId: String,
        promptEn: String,
        promptRu: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be signed in."))
        val questionId = "cq_${System.currentTimeMillis()}_${sanitizeIdSegment(uid, 12)}"
        val path = "couples/${couple.id}/customQuestions/$questionId"

        return try {
            val cleanEn = promptEn.trim().take(300)
            val cleanRu = promptRu.trim().take(300)
            require(cleanEn.isNotEmpty() || cleanRu.isNotEmpty()) {
                "Enter at least one question text."
            }
            db.collection("couples").document(couple.id)
                .collection("customQuestions").document(questionId)
                .set(
                    mapOf(
                        "coupleId" to couple.id,
                        "topicId" to topicId,
                        "promptEn" to cleanEn,
                        "promptRu" to cleanRu,
                        "authorUid" to uid,
                        "memberUids" to couple.memberUids,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveTrip(
        couple: CoupleSpace,
        title: String,
        location: String,
        startDateKey: String,
        endDateKey: String,
        notes: String,
        existingTrip: TripMemory?
    ): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be signed in."))
        val tripId = existingTrip?.id?.takeIf { it.isNotBlank() }
            ?: "trip_${System.currentTimeMillis()}_${sanitizeIdSegment(uid, 12)}"
        val path = "couples/${couple.id}/trips/$tripId"

        return try {
            val cleanTitle = title.trim().take(100)
            require(cleanTitle.isNotEmpty()) { "Trip name cannot be empty." }
            val cleanLocation = location.trim().take(100)
            val cleanStart = startDateKey.trim()
            val cleanEnd = endDateKey.trim()
            val cleanNotes = notes.trim().take(2000)

            val docRef = db.collection("couples").document(couple.id)
                .collection("trips").document(tripId)

            if (existingTrip != null) {
                docRef.update(
                    mapOf(
                        "title" to cleanTitle,
                        "location" to cleanLocation,
                        "startDateKey" to cleanStart,
                        "endDateKey" to cleanEnd,
                        "notes" to cleanNotes,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "coupleId" to couple.id,
                        "title" to cleanTitle,
                        "location" to cleanLocation,
                        "startDateKey" to cleanStart,
                        "endDateKey" to cleanEnd,
                        "notes" to cleanNotes,
                        "authorUid" to uid,
                        "memberUids" to couple.memberUids,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(tripId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTrip(
        coupleId: String,
        tripId: String,
        photosForTrip: List<TripPhoto>
    ): Result<Unit> {
        val path = "couples/$coupleId/trips/$tripId"
        return try {
            requireUserId()
            val batch = db.batch()
            batch.delete(db.collection("couples").document(coupleId).collection("trips").document(tripId))
            photosForTrip.forEach { photo ->
                batch.delete(
                    db.collection("couples").document(coupleId)
                        .collection("tripPhotos").document(photo.id)
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun addTripPhoto(
        couple: CoupleSpace,
        tripId: String,
        photoId: String,
        processed: ProcessedTripPhoto,
        authorLabel: String,
        caption: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be signed in."))
        val path = "couples/${couple.id}/tripPhotos/$photoId"
        return try {
            val safeLabel = authorLabel.trim().ifBlank { "Partner" }.take(80)
            val safeCaption = caption.trim().take(200)
            db.collection("couples").document(couple.id)
                .collection("tripPhotos").document(photoId)
                .set(
                    mapOf(
                        "coupleId" to couple.id,
                        "tripId" to tripId,
                        "takenDateKey" to processed.takenDateKey,
                        "takenTimeLabel" to processed.takenTimeLabel.take(10),
                        "takenMillis" to processed.takenMillis.coerceAtLeast(0L),
                        "timestampSource" to processed.timestampSource,
                        "caption" to safeCaption,
                        "thumbnailBase64" to processed.thumbnailBase64,
                        "reaction" to "",
                        "authorUid" to uid,
                        "authorLabel" to safeLabel,
                        "memberUids" to couple.memberUids,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun reactToTripPhoto(
        coupleId: String,
        photoId: String,
        reaction: String
    ): Result<Unit> {
        val path = "couples/$coupleId/tripPhotos/$photoId"
        return try {
            requireUserId()
            require(photoId.isNotBlank())
            val safeReaction = if (reaction.isBlank()) "" else EmojiReactionHelper.extractFirstEmojiGrapheme(reaction)
            db.collection("couples").document(coupleId)
                .collection("tripPhotos").document(photoId)
                .update(
                    mapOf(
                        "reaction" to safeReaction,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTripPhoto(
        coupleId: String,
        photoId: String,
        caption: String,
        takenDateKey: String,
        takenTimeLabel: String,
        takenMillis: Long,
        timestampSource: String
    ): Result<Unit> {
        val path = "couples/$coupleId/tripPhotos/$photoId"
        return try {
            requireUserId()
            db.collection("couples").document(coupleId)
                .collection("tripPhotos").document(photoId)
                .update(
                    mapOf(
                        "caption" to caption.trim().take(200),
                        "takenDateKey" to takenDateKey.trim(),
                        "takenTimeLabel" to takenTimeLabel.trim().take(10),
                        "takenMillis" to takenMillis.coerceAtLeast(0L),
                        "timestampSource" to timestampSource,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTripPhoto(
        coupleId: String,
        photoId: String
    ): Result<Unit> {
        val path = "couples/$coupleId/tripPhotos/$photoId"
        return try {
            requireUserId()
            db.collection("couples").document(coupleId)
                .collection("tripPhotos").document(photoId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTripPhotos(
        coupleId: String,
        photoIds: Collection<String>
    ): Result<Unit> {
        val cleanIds = photoIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (cleanIds.isEmpty()) return Result.success(Unit)
        val path = "couples/$coupleId/tripPhotos"
        return try {
            requireUserId()
            val collectionRef = db.collection("couples").document(coupleId).collection("tripPhotos")
            cleanIds.chunked(450).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { photoId ->
                    batch.delete(collectionRef.document(photoId))
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
