package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.AvatarCatalog
import com.example.model.RelationshipTrackerHelper
import com.example.model.TopicDeck
import com.example.model.UiStrings
import com.example.viewmodel.DuetUiState

@Composable
fun TopicsScreen(
    state: DuetUiState.Ready,
    currentUserId: String,
    language: AppLanguage,
    showSecondary: Boolean,
    isBusy: Boolean,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleSecondary: () -> Unit,
    onShufflePrompt: () -> Unit,
    onOpenTopic: (topicId: String, questionId: String?) -> Unit,
    onAddCustomQuestion: (topicId: String, en: String, ru: String) -> Unit,
    onUpdateNextReunion: (dateKey: String, location: String) -> Unit,
    onUpdateRelationshipStartDate: (dateKey: String) -> Unit,
    onUpdateMyProfileIdentity: (displayName: String, avatarId: String) -> Unit,
    onUpdateAesthetic: (aestheticId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showReunionDialog by remember { mutableStateOf(false) }
    var showRelationshipDialog by remember { mutableStateOf(false) }
    var showMyAvatarPicker by remember { mutableStateOf(false) }
    var showAestheticPicker by remember { mutableStateOf(false) }

    val answeredQuestionIds = remember(state.answers) {
        state.answers.map { it.questionId }.toSet()
    }

    val mySlot = remember(state.coupleSpace.creatorUid, currentUserId) {
        AvatarCatalog.slotForUser(state.coupleSpace, currentUserId)
    }
    val relationshipStats = remember(state.coupleSpace.relationshipStartDateKey) {
        if (state.coupleSpace.relationshipStartDateKey.isBlank()) null
        else RelationshipTrackerHelper.calculateStats(state.coupleSpace.relationshipStartDateKey)
    }
    val partnerOne = state.coupleSpace.partnerOneName.ifBlank { "Partner 1" }
    val partnerTwo = state.coupleSpace.partnerTwoName
    val coupleHeaderNames = if (partnerTwo.isNotBlank()) "$partnerOne & $partnerTwo" else partnerOne

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = UiStrings.appTitle(language),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .then(
                                            if (mySlot == 1) {
                                                Modifier
                                                    .clickable { showMyAvatarPicker = true }
                                                    .minimumInteractiveComponentSize()
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .testTag("topics_avatar_p1")
                                ) {
                                    PartnerAvatarBadge(
                                        avatarId = state.coupleSpace.partnerOneAvatarId,
                                        displayName = partnerOne,
                                        size = 32.dp,
                                        showBorder = true
                                    )
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .then(
                                            if (mySlot == 2) {
                                                Modifier
                                                    .clickable { showMyAvatarPicker = true }
                                                    .minimumInteractiveComponentSize()
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .testTag("topics_avatar_p2")
                                ) {
                                    PartnerAvatarBadge(
                                        avatarId = state.coupleSpace.partnerTwoAvatarId,
                                        displayName = partnerTwo.ifBlank { UiStrings.partnerTwoField(language) },
                                        size = 32.dp,
                                        showBorder = true
                                    )
                                }
                            }
                            Text(
                                text = coupleHeaderNames,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { showAestheticPicker = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("topics_aesthetic_button")
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = UiStrings.aestheticSectionTitle(language),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        LanguageSwitcherPill(
                            language = language,
                            showSecondary = showSecondary,
                            onSelectLanguage = onSelectLanguage,
                            onToggleSecondary = onToggleSecondary
                        )
                    }
                }

                Surface(
                    onClick = { showRelationshipDialog = true },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.testTag("topics_relationship_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (relationshipStats != null) {
                                val daysText = UiStrings.relationshipTotalDays(relationshipStats.totalDays, language)
                                if (relationshipStats.isAnniversaryToday) "$daysText • 🎉" else daysText
                            } else {
                                UiStrings.relationshipEmptyTitle(language)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Next Reunion Countdown Card
        item {
            ReunionCountdownCard(
                couple = state.coupleSpace,
                language = language,
                onEditClick = { showReunionDialog = true }
            )
        }

        // Featured Tonight's Prompt Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = UiStrings.dailySparkLabel(language),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        BilingualPromptText(
                            text = state.dailyPrompt.text,
                            language = language,
                            showSecondary = showSecondary,
                            primaryStyle = MaterialTheme.typography.headlineSmall,
                            secondaryStyle = MaterialTheme.typography.bodyMedium,
                            primaryColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    onOpenTopic(state.dailyPrompt.topicId, state.dailyPrompt.id)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("answer_daily_prompt_button")
                            ) {
                                Text(
                                    text = UiStrings.answerNow(language),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            OutlinedButton(
                                onClick = onShufflePrompt,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("shuffle_prompt_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = UiStrings.shufflePrompt(language),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = UiStrings.shufflePrompt(language),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = UiStrings.topicsHeader(language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("open_add_question_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = UiStrings.addQuestionButton(language),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Responsive Grid of Topic Decks
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val twoColumns = maxWidth >= 560.dp
                if (twoColumns) {
                    val rows = state.decks.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        rows.forEach { rowDecks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowDecks.forEach { deck ->
                                    val answeredInDeck = deck.questions.count { it.id in answeredQuestionIds }
                                    TopicDeckCard(
                                        deck = deck,
                                        answeredCount = answeredInDeck,
                                        language = language,
                                        showSecondary = showSecondary,
                                        onClick = { onOpenTopic(deck.id, null) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowDecks.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        state.decks.forEach { deck: TopicDeck ->
                            val answeredInDeck = deck.questions.count { it.id in answeredQuestionIds }
                            TopicDeckCard(
                                deck = deck,
                                answeredCount = answeredInDeck,
                                language = language,
                                showSecondary = showSecondary,
                                onClick = { onOpenTopic(deck.id, null) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAddDialog) {
        AddCustomQuestionDialog(
            language = language,
            onDismiss = { showAddDialog = false },
            onConfirm = { en, ru ->
                onAddCustomQuestion("custom", en, ru)
                showAddDialog = false
            }
        )
    }

    if (showReunionDialog) {
        ReunionEditorDialog(
            initialDateKey = state.coupleSpace.nextMeetDateKey,
            initialLocation = state.coupleSpace.nextMeetLocation,
            language = language,
            isBusy = isBusy,
            onDismiss = { showReunionDialog = false },
            onSave = { dateKey, location ->
                onUpdateNextReunion(dateKey, location)
                showReunionDialog = false
            }
        )
    }

    if (showRelationshipDialog) {
        RelationshipDateEditorDialog(
            initialDateKey = state.coupleSpace.relationshipStartDateKey,
            language = language,
            isBusy = isBusy,
            onDismiss = { showRelationshipDialog = false },
            onSave = { dateKey ->
                onUpdateRelationshipStartDate(dateKey)
                showRelationshipDialog = false
            }
        )
    }

    if (showMyAvatarPicker) {
        val currentName = if (mySlot == 1) {
            state.coupleSpace.partnerOneName.ifBlank { state.userProfile?.displayName ?: "" }
        } else {
            state.coupleSpace.partnerTwoName.ifBlank { state.userProfile?.displayName ?: "" }
        }
        val currentAvatar = if (mySlot == 1) {
            state.coupleSpace.partnerOneAvatarId
        } else {
            state.coupleSpace.partnerTwoAvatarId
        }
        AvatarPickerDialog(
            initialName = currentName,
            currentAvatarId = currentAvatar,
            language = language,
            isFirstJoin = false,
            isBusy = isBusy,
            onDismiss = { showMyAvatarPicker = false },
            onSave = { updatedName, selectedId ->
                onUpdateMyProfileIdentity(updatedName, selectedId)
                showMyAvatarPicker = false
            }
        )
    }

    if (showAestheticPicker) {
        AestheticPickerDialog(
            currentAestheticId = state.coupleSpace.aestheticThemeId,
            language = language,
            onDismiss = { showAestheticPicker = false },
            onSelectAesthetic = { themeId ->
                onUpdateAesthetic(themeId)
                showAestheticPicker = false
            }
        )
    }
}
