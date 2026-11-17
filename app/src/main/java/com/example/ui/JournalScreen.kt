package com.example.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.AvatarCatalog
import com.example.model.PromptQuestion
import com.example.model.QuestionAnswer
import com.example.model.TopicDeck
import com.example.model.UiStrings
import com.example.viewmodel.DuetUiState
import com.google.firebase.Timestamp

internal data class JournalEntry(
    val question: PromptQuestion,
    val deck: TopicDeck,
    val answers: List<QuestionAnswer>,
    val latestCreatedAt: Timestamp
)

internal fun buildJournalEntries(
    answers: List<QuestionAnswer>,
    decks: List<TopicDeck>
): List<JournalEntry> {
    val questionLookup = decks.flatMap { deck ->
        deck.questions.map { q -> q.id to (q to deck) }
    }.toMap()

    return answers
        .groupBy { it.questionId }
        .mapNotNull { (qId, list) ->
            val pair = questionLookup[qId] ?: return@mapNotNull null
            val latestCreated = list.maxOfOrNull { it.createdAt ?: Timestamp(0, 0) }
                ?: Timestamp(0, 0)
            JournalEntry(
                question = pair.first,
                deck = pair.second,
                answers = list.sortedWith(
                    compareBy<QuestionAnswer> { it.createdAt ?: Timestamp(0, 0) }
                        .thenBy { it.id }
                ),
                latestCreatedAt = latestCreated
            )
        }
        .sortedWith(
            compareByDescending<JournalEntry> { it.latestCreatedAt }
                .thenBy { it.question.id }
        )
}

@Composable
fun JournalScreen(
    state: DuetUiState.Ready,
    currentUserId: String,
    language: AppLanguage,
    showSecondary: Boolean,
    onOpenQuestion: (topicId: String, questionId: String) -> Unit,
    onReactToAnswer: (answerId: String, reaction: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val entries = remember(state.answers, state.decks) {
        buildJournalEntries(state.answers, state.decks)
    }

    val filteredEntries = remember(entries, selectedFilter) {
        when (selectedFilter) {
            "all" -> entries
            "both" -> entries.filter { it.answers.size >= 2 }
            else -> entries.filter { it.deck.id == selectedFilter }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = UiStrings.journalTitle(language),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text(UiStrings.filterAll(language)) },
                        modifier = Modifier.testTag("journal_filter_all")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "both",
                        onClick = { selectedFilter = "both" },
                        label = { Text(UiStrings.filterBothAnswered(language)) },
                        modifier = Modifier.testTag("journal_filter_both")
                    )
                }
                items(state.decks) { deck ->
                    FilterChip(
                        selected = selectedFilter == deck.id,
                        onClick = { selectedFilter = deck.id },
                        label = { Text(deck.emojiBadge + " " + deck.title.primary(language)) },
                        modifier = Modifier.testTag("journal_filter_${deck.id}")
                    )
                }
            }
        }

        if (filteredEntries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_duet_cards),
                    contentDescription = UiStrings.journalEmptyTitle(language),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(26.dp))
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = UiStrings.journalEmptyTitle(language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = UiStrings.journalEmptySubtitle(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredEntries, key = { it.question.id }) { entry ->
                    Surface(
                        onClick = { onOpenQuestion(entry.deck.id, entry.question.id) },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_entry_${entry.question.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.deck.emojiBadge + " " + entry.deck.title.primary(language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accentColor(entry.deck.accent)
                                )
                                if (entry.answers.size >= 2) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = UiStrings.bothAnsweredBadge(language),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            BilingualPromptText(
                                text = entry.question.text,
                                language = language,
                                showSecondary = showSecondary,
                                primaryStyle = MaterialTheme.typography.titleLarge,
                                secondaryStyle = MaterialTheme.typography.bodySmall
                            )

                            entry.answers.forEach { ans ->
                                val isSelf = ans.authorUid == currentUserId && ans.speakerRole == "self"
                                val avatarId = AvatarCatalog.resolveForPartner(
                                    couple = state.coupleSpace,
                                    authorUid = ans.authorUid,
                                    speakerRole = ans.speakerRole
                                )
                                PartnerAnswerBubble(
                                    answer = ans,
                                    isSelfBubble = isSelf,
                                    language = language,
                                    onReact = { emoji -> onReactToAnswer(ans.id, emoji) },
                                    avatarId = avatarId
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
