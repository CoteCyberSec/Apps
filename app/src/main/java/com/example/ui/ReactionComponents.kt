package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddReaction
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.EmojiReactionHelper
import com.example.model.UiStrings

@Composable
fun EmojiReactionBar(
    currentReaction: String,
    language: AppLanguage,
    targetTagPrefix: String,
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showChooser by remember { mutableStateOf(false) }
    val hasReaction = currentReaction.isNotBlank()

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(50),
            color = if (hasReaction) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            border = BorderStroke(
                width = 1.dp,
                color = if (hasReaction) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { menuExpanded = true }
                .minimumInteractiveComponentSize()
                .testTag("reaction_dropdown_$targetTagPrefix")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (hasReaction) {
                    Text(
                        text = currentReaction,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AddReaction,
                        contentDescription = UiStrings.reactLabel(language),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = UiStrings.reactLabel(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = UiStrings.moreEmojisDesc(language),
                    tint = if (hasReaction) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EmojiReactionHelper.quickEmojis.forEach { emoji ->
                    val active = currentReaction == emoji
                    Surface(
                        shape = CircleShape,
                        color = if (active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (active) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                menuExpanded = false
                                onReact(if (active) "" else emoji)
                            }
                            .testTag("quick_emoji_${targetTagPrefix}_$emoji")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            DropdownMenuItem(
                text = {
                    Text(
                        text = UiStrings.moreEmojisOption(language),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.AddReaction,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    menuExpanded = false
                    showChooser = true
                },
                modifier = Modifier.testTag("pick_emoji_$targetTagPrefix")
            )

            if (hasReaction) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = UiStrings.clearReactionAction(language),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onReact("")
                    },
                    modifier = Modifier.testTag("clear_reaction_$targetTagPrefix")
                )
            }
        }
    }

    if (showChooser) {
        EmojiChooserDialog(
            currentReaction = currentReaction,
            language = language,
            onDismiss = { showChooser = false },
            onSelectEmoji = { chosen ->
                onReact(chosen)
                showChooser = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmojiChooserDialog(
    currentReaction: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSelectEmoji: (String) -> Unit
) {
    var customInput by remember(currentReaction) { mutableStateOf("") }
    val extractedCustom = remember(customInput) {
        EmojiReactionHelper.extractFirstEmojiGrapheme(customInput)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = UiStrings.chooseEmojiTitle(language),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { if (it.length <= 16) customInput = it },
                        label = { Text(UiStrings.customEmojiLabel(language)) },
                        placeholder = { Text(UiStrings.customEmojiHint(language)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_emoji_input")
                    )
                    Button(
                        onClick = {
                            if (extractedCustom.isNotBlank()) {
                                onSelectEmoji(extractedCustom)
                            }
                        },
                        enabled = extractedCustom.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_custom_emoji_button")
                    ) {
                        Text(UiStrings.useEmojiAction(language))
                    }
                }

                EmojiReactionHelper.categories.forEach { category ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (language == AppLanguage.RU) category.titleRu else category.titleEn,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            category.emojis.forEach { emoji ->
                                val selected = currentReaction == emoji
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelectEmoji(if (selected) "" else emoji) }
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = emoji, fontSize = 22.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(UiStrings.cancelLabel(language))
            }
        },
        dismissButton = {
            if (currentReaction.isNotBlank()) {
                TextButton(
                    onClick = { onSelectEmoji("") },
                    modifier = Modifier.testTag("clear_reaction_button")
                ) {
                    Text(
                        text = UiStrings.clearReactionAction(language),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
        }
    )
}
