package yangfentuozi.dsusideloaderplus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    title: String,
    summary: String? = null,
    valueText: String? = null,
    onClick: (() -> Unit)?,
    rowTrailingContent: @Composable (RowScope.() -> Unit)? = null,
    columnTrailingContent: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier =
            if (onClick == null) {
                Modifier
            } else {
                Modifier.clickable(onClick = onClick)
            }
                .padding(vertical = 20.dp)
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!summary.isNullOrBlank()) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (rowTrailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                rowTrailingContent(this)
            } else if (!valueText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (columnTrailingContent != null) {
            columnTrailingContent(this)
        }
    }
}

@Composable
fun ExpandableSettingsItem(
    title: String,
    summary: String? = null,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    rowTrailingContent: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffectAfterFirst(expanded) {
        haptic.performHapticFeedback(
            hapticFeedbackType = if (expanded) HapticFeedbackType.ToggleOn
            else HapticFeedbackType.ToggleOff
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .clickable(enabled = enabled) {
                    onExpandedChange(!expanded)
                }
                .fillMaxWidth()
                .heightIn(min = 72.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
                    .heightIn(min = 72.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (!summary.isNullOrBlank()) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (rowTrailingContent != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    rowTrailingContent(this)
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(36.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    enabled = enabled,
                    onClick = { onExpandedChange(!expanded) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                ) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.ExpandLess
                        } else {
                            Icons.Filled.ExpandMore
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                content = content
            )
        }
    }
}
