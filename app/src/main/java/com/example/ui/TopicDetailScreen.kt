package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.AvatarCatalog
import com.example.model.CoupleSpace
import com.example.model.PromptQuestion
import com.example.model.QuestionAnswer
import com.example.model.TopicDeck
import com.example.model.UiStrings
import com.example.viewmodel.DuetUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    state: DuetUiState.Ready,
    topicId: String,
    initialQuestionId: String?,
    currentUserId: String,
    language: AppLanguage,
    showSecondary: Boolean,
    isBusy: Boolean,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleSecondary: () -> Unit,
    onBack: () -> Unit,
    onSaveAnswer: (
        question: PromptQuestion,
        speakerRole: String,
        authorLabel: String,
        text: String,
        existing: QuestionAnswer?
    ) -> Unit,
    onReactToAnswer: (answerId: String, reaction: String) -> Unit,
    onAddCustomQuestion: (topicId: String, en: String, ru: String) -> Unit,
    onMarkAnswerSeen: (answerId: String) -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val deck = state.decks.firstOrNull { it.id == topicId } ?: state.decks.first()
    val questions = deck.questions
    val listState = rememberLazyListState()

    var expandedQuestionId by remember(topicId, initialQuestionId) {
        mutableStateOf(initialQuestionId ?: questions.firstOrNull()?.id)
    }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(topicId, initialQuestionId, questions.size) {
        if (initialQuestionId != null) {
            val targetIdx = questions.indexOfFirst { it.id == initialQuestionId }
            if (targetIdx >= 0) {
                expandedQuestionId = initialQuestionId
                // +1 accounts for the deck header item at index 0
                listState.animateScrollToItem(targetIdx + 1)
            }
        }
    }

    val isTwoDeviceCouple = state.coupleSpace.memberUids.size >= 2
    val currentUserDisplayName = remember(state.coupleSpace, state.userProfile, currentUserId) {
        if (state.coupleSpace.creatorUid == currentUserId) {
            state.coupleSpace.partnerOneName.ifBlank { state.userProfile?.displayName ?: "You" }
        } else {
            state.coupleSpace.partnerTwoName.ifBlank { state.userProfile?.displayName ?: "You" }
        }
    }
    val localPartnerName = remember(state.coupleSpace, currentUserId) {
        if (state.coupleSpace.creatorUid == currentUserId) {
            state.coupleSpace.partnerTwoName.ifBlank { UiStrings.partnerAnswer("", language) }
        } else {
            state.coupleSpace.partnerOneName.ifBlank { UiStrings.partnerAnswer("", language) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${deck.emojiBadge} ${deck.title.primary(language)}",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (showSecondary) {
                            Text(
                                text = deck.title.secondary(language),
                                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("topic_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = UiStrings.prevQuestion(language)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("topic_add_question_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = UiStrings.addQuestionButton(language)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (questions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_duet_cards),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = UiStrings.addQuestionTitle(language),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = deck.subtitle.primary(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("empty_custom_add_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(UiStrings.addQuestionButton(language))
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("scrollable_questions_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "deck_header") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = UiStrings.scrollPromptsHint(questions.size, language),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LanguageSwitcherPill(
                                language = language,
                                showSecondary = showSecondary,
                                onSelectLanguage = onSelectLanguage,
                                onToggleSecondary = onToggleSecondary
                            )
                        }

                        Text(
                            text = deck.subtitle.primary(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(questions, key = { it.id }) { question ->
                    val questionAnswers = remember(state.answers, question.id) {
                        state.answers.filter { it.questionId == question.id }
                    }
                    val isExpanded = expandedQuestionId == question.id

                    LaunchedEffect(isExpanded, questionAnswers) {
                        if (isExpanded) {
                            questionAnswers
                                .filter { it.authorUid != currentUserId || it.speakerRole == "partner_local" }
                                .forEach { onMarkAnswerSeen(it.id) }
                        }
                    }

                    ScrollableQuestionCard(
                        deck = deck,
                        coupleSpace = state.coupleSpace,
                        question = question,
                        questionAnswers = questionAnswers,
                        isExpanded = isExpanded,
                        isTwoDeviceCouple = isTwoDeviceCouple,
                        currentUserId = currentUserId,
                        currentUserDisplayName = currentUserDisplayName,
                        localPartnerName = localPartnerName,
                        language = language,
                        showSecondary = showSecondary,
                        isBusy = isBusy,
                        onToggleExpand = {
                            expandedQuestionId = if (isExpanded) null else question.id
                        },
                        onSaveAnswer = onSaveAnswer,
                        onReactToAnswer = onReactToAnswer
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomQuestionDialog(
            language = language,
            onDismiss = { showAddDialog = false },
            onConfirm = { en, ru ->
                onAddCustomQuestion(topicId, en, ru)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ScrollableQuestionCard(
    deck: TopicDeck,
    coupleSpace: CoupleSpace,
    question: PromptQuestion,
    questionAnswers: List<QuestionAnswer>,
    isExpanded: Boolean,
    isTwoDeviceCouple: Boolean,
    currentUserId: String,
    currentUserDisplayName: String,
    localPartnerName: String,
    language: AppLanguage,
    showSecondary: Boolean,
    isBusy: Boolean,
    onToggleExpand: () -> Unit,
    onSaveAnswer: (
        question: PromptQuestion,
        speakerRole: String,
        authorLabel: String,
        text: String,
        existing: QuestionAnswer?
    ) -> Unit,
    onReactToAnswer: (answerId: String, reaction: String) -> Unit
) {
    val myExistingAnswer = remember(questionAnswers, currentUserId) {
        questionAnswers.firstOrNull {
            it.authorUid == currentUserId && it.speakerRole == "self"
        }
    }
    val partnerAnswers = remember(questionAnswers, currentUserId) {
        questionAnswers.filter {
            it.authorUid != currentUserId || it.speakerRole == "partner_local"
        }
    }
    var draftText by remember(question.id, myExistingAnswer?.id) {
        mutableStateOf(myExistingAnswer?.text ?: "")
    }
    LaunchedEffect(myExistingAnswer?.text) {
        if (draftText.isBlank() && !myExistingAnswer?.text.isNullOrBlank()) {
            draftText = myExistingAnswer?.text ?: ""
        }
    }

    val bothDone = myExistingAnswer != null && partnerAnswers.isNotEmpty()
    val answeredCount = (if (myExistingAnswer != null) 1 else 0) + (if (partnerAnswers.isNotEmpty()) 1 else 0)
    val myAvatarId = remember(coupleSpace, currentUserId) {
        AvatarCatalog.resolveForPartner(coupleSpace, currentUserId, "self")
    }

    Surface(
        onClick = onToggleExpand,
        shape = RoundedCornerShape(26.dp),
        color = accentContainerColor(deck.accent).copy(alpha = if (isExpanded) 0.58f else 0.42f),
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = accentColor(deck.accent).copy(alpha = if (isExpanded) 0.65f else 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${deck.emojiBadge} ${deck.title.primary(language)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor(deck.accent),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (bothDone) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (bothDone) Icons.Rounded.CheckCircle
                            else Icons.Rounded.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (bothDone) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (bothDone) UiStrings.bothAnsweredBadge(language)
                            else UiStrings.answeredProgress(answeredCount, 2, language),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            BilingualPromptText(
                text = question.text,
                language = language,
                showSecondary = showSecondary,
                primaryStyle = MaterialTheme.typography.headlineSmall,
                secondaryStyle = MaterialTheme.typography.bodyMedium
            )

            // Your separate answer
            if (myExistingAnswer != null) {
                PartnerAnswerBubble(
                    answer = myExistingAnswer,
                    isSelfBubble = true,
                    language = language,
                    onReact = { emoji -> onReactToAnswer(myExistingAnswer.id, emoji) },
                    avatarId = myAvatarId
                )
            }

            // Partner's separate read-only answer section
            if (partnerAnswers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    partnerAnswers.forEach { ans ->
                        val partnerAvatarId = AvatarCatalog.resolveForPartner(
                            couple = coupleSpace,
                            authorUid = ans.authorUid,
                            speakerRole = ans.speakerRole
                        )
                        PartnerAnswerBubble(
                            answer = ans,
                            isSelfBubble = false,
                            language = language,
                            onReact = { emoji -> onReactToAnswer(ans.id, emoji) },
                            avatarId = partnerAvatarId
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_readonly_placeholder_${question.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = UiStrings.partnerReadOnlyBadge(language),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = UiStrings.partnerAnswerSection(localPartnerName, language),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isTwoDeviceCouple || coupleSpace.inviteCode.isBlank()) {
                                    UiStrings.partnerOnlyAnswerHint(localPartnerName, language)
                                } else {
                                    UiStrings.partnerInviteJoinHint(localPartnerName, coupleSpace.inviteCode, language)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isExpanded) {
                    OutlinedButton(
                        onClick = onToggleExpand,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("collapse_question_${question.id}")
                    ) {
                        Icon(Icons.Rounded.ExpandLess, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(UiStrings.closeComposerAction(language), style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onToggleExpand,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("toggle_answer_${question.id}")
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (myExistingAnswer != null) UiStrings.editAnswerAction(language)
                            else UiStrings.writeAnswerAction(language),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PartnerAvatarBadge(
                                avatarId = myAvatarId,
                                displayName = currentUserDisplayName,
                                size = 28.dp,
                                showBorder = true
                            )
                            Text(
                                text = UiStrings.yourAnswerSection(currentUserDisplayName, language),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = draftText,
                            onValueChange = { if (it.length <= 1500) draftText = it },
                            placeholder = { Text(UiStrings.answerPlaceholder(language)) },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("answer_input_${question.id}")
                        )

                        Button(
                            onClick = {
                                onSaveAnswer(
                                    question,
                                    "self",
                                    currentUserDisplayName,
                                    draftText,
                                    myExistingAnswer
                                )
                            },
                            enabled = draftText.isNotBlank() && !isBusy,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_answer_${question.id}")
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (myExistingAnswer != null) {
                                        UiStrings.updateAnswer(language)
                                    } else {
                                        UiStrings.saveAnswer(language)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
