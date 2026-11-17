package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.BilingualText
import com.example.model.QuestionAnswer
import com.example.model.TopicAccent
import com.example.model.TopicDeck
import com.example.model.UiStrings
import com.example.ui.theme.LocalDuetGoldColors

@Composable
fun accentContainerColor(accent: TopicAccent): Color = when (accent) {
    TopicAccent.BURGUNDY -> MaterialTheme.colorScheme.primaryContainer
    TopicAccent.TERRACOTTA -> MaterialTheme.colorScheme.secondaryContainer
    TopicAccent.SAGE -> MaterialTheme.colorScheme.tertiaryContainer
    TopicAccent.GOLD -> LocalDuetGoldColors.current.container.copy(alpha = 0.85f)
}

@Composable
fun accentColor(accent: TopicAccent): Color = when (accent) {
    TopicAccent.BURGUNDY -> MaterialTheme.colorScheme.primary
    TopicAccent.TERRACOTTA -> MaterialTheme.colorScheme.secondary
    TopicAccent.SAGE -> MaterialTheme.colorScheme.tertiary
    TopicAccent.GOLD -> LocalDuetGoldColors.current.accent
}

@Composable
fun LanguageSwitcherPill(
    language: AppLanguage,
    showSecondary: Boolean,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleSecondary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppLanguage.entries.forEach { option ->
                    val selected = option == language
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectLanguage(option) }
                            .testTag("lang_toggle_${option.code}")
                    ) {
                        Text(
                            text = option.badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = if (showSecondary) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                1.dp,
                if (showSecondary) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onToggleSecondary() }
                .testTag("dual_language_toggle")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = UiStrings.dualLanguageLabel(language),
                    tint = if (showSecondary) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = UiStrings.dualToggleShort(language),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (showSecondary) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BilingualPromptText(
    text: BilingualText,
    language: AppLanguage,
    showSecondary: Boolean,
    primaryStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    secondaryStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    primaryColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = text.primary(language),
            style = primaryStyle,
            color = primaryColor
        )
        AnimatedVisibility(visible = showSecondary && text.secondary(language) != text.primary(language)) {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = text.secondary(language),
                    style = secondaryStyle.copy(fontStyle = FontStyle.Italic),
                    color = secondaryColor
                )
            }
        }
    }
}

@Composable
fun TopicDeckCard(
    deck: TopicDeck,
    answeredCount: Int,
    language: AppLanguage,
    showSecondary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = deck.questions.size
    val progress = if (total > 0) (answeredCount.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val tint = accentColor(deck.accent)
    val badgeBg = accentContainerColor(deck.accent)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("topic_card_${deck.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = deck.emojiBadge,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeBg.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = if (total > 0) "$answeredCount / $total" else "0",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = deck.title.primary(language),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (showSecondary) {
                    Text(
                        text = deck.title.secondary(language),
                        style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = deck.subtitle.primary(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = tint,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun PartnerAnswerBubble(
    answer: QuestionAnswer,
    isSelfBubble: Boolean,
    language: AppLanguage,
    onReact: (String) -> Unit,
    avatarId: String = "",
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelfBubble) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.62f)
    }
    val borderColor = if (isSelfBubble) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("answer_bubble_${answer.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PartnerAvatarBadge(
                        avatarId = avatarId,
                        displayName = answer.authorLabel,
                        size = 28.dp,
                        showBorder = true
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelfBubble) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary
                    ) {
                        Text(
                            text = if (isSelfBubble) UiStrings.yourAnswerSection(answer.authorLabel, language)
                            else answer.authorLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelfBubble) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (!isSelfBubble) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = UiStrings.partnerReadOnlyBadge(language),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = UiStrings.partnerReadOnlyBadge(language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (answer.reaction.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = answer.reaction,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = answer.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                EmojiReactionBar(
                    currentReaction = answer.reaction,
                    language = language,
                    targetTagPrefix = "answer_${answer.id}",
                    onReact = onReact
                )
            }
        }
    }
}

@Composable
fun AddCustomQuestionDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (promptEn: String, promptRu: String) -> Unit
) {
    var enText by remember { mutableStateOf("") }
    var ruText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = UiStrings.addQuestionTitle(language),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = UiStrings.addQuestionTitle(language),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = enText,
                    onValueChange = { if (it.length <= 300) enText = it },
                    label = { Text(UiStrings.promptEnLabel(language)) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_question_en_input")
                )
                OutlinedTextField(
                    value = ruText,
                    onValueChange = { if (it.length <= 300) ruText = it },
                    label = { Text(UiStrings.promptRuLabel(language)) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_question_ru_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enText.isNotBlank() || ruText.isNotBlank()) {
                        onConfirm(enText, ruText)
                    }
                },
                enabled = enText.isNotBlank() || ruText.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_question_button")
            ) {
                Text(UiStrings.createLabel(language), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiStrings.cancelLabel(language))
            }
        }
    )
}
