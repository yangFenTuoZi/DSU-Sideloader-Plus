package yangfentuozi.dsusideloaderplus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
fun M3ESwitchWidget(
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffectAfterFirst(checked) {
        haptic.performHapticFeedback(
            hapticFeedbackType = if (checked) HapticFeedbackType.ToggleOn
            else HapticFeedbackType.ToggleOff
        )
    }

    Row(
        modifier = Modifier
            .clickable(enabled = enabled) {
                onCheckedChange(!checked)
            }
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .padding(horizontal = 36.dp),
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
        Switch(
            enabled = enabled,
            checked = checked,
            thumbContent = {
                Icon(
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            },
            onCheckedChange = null
        )
    }
}

@Composable
fun ExpandableSettingsSwitchItem(
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffectAfterFirst(checked) {
        haptic.performHapticFeedback(
            hapticFeedbackType = if (checked) HapticFeedbackType.ToggleOn
            else HapticFeedbackType.ToggleOff
        )
    }
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
                Switch(
                    enabled = enabled,
                    checked = checked,
                    thumbContent = {
                        Icon(
                            imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    },
                    onCheckedChange = onCheckedChange
                )
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
