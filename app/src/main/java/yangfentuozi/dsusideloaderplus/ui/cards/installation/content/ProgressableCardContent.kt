package yangfentuozi.dsusideloaderplus.ui.cards.installation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.SettingsItem
import yangfentuozi.dsusideloaderplus.ui.components.buttons.PrimaryButton
import yangfentuozi.dsusideloaderplus.ui.components.buttons.SecondaryButton

@Composable
fun ProgressableCardContent(
    text: String,
    showProgressBar: Boolean = false,
    isIndeterminate: Boolean = false,
    progress: Float = 0F,
    textFirstButton: String = "",
    textSecondButton: String = "",
    onClickFirstButton: (() -> Unit)? = null,
    onClickSecondButton: (() -> Unit)? = null,
) {
    val hasActions = onClickFirstButton != null || onClickSecondButton != null

    SettingsItem(
        title = stringResource(R.string.installation),
        summary = text,
        onClick = null,
        columnTrailingContent = {
            AnimatedVisibility(visible = showProgressBar) {
                val progressBarModifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                if (isIndeterminate) {
                    LinearProgressIndicator(modifier = progressBarModifier)
                } else {
                    LinearProgressIndicator(
                        modifier = progressBarModifier,
                        progress = { progress },
                    )
                }
            }
            if (hasActions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                ) {
                    Spacer(modifier = Modifier.weight(1F))
                    if (onClickSecondButton != null) {
                        SecondaryButton(
                            text = textSecondButton,
                            onClick = onClickSecondButton,
                        )
                    }
                    if (onClickFirstButton != null && onClickSecondButton != null) {
                        Spacer(modifier = Modifier.padding(end = 6.dp))
                    }
                    if (onClickFirstButton != null) {
                        PrimaryButton(
                            text = textFirstButton,
                            onClick = onClickFirstButton,
                        )
                    }
                }
            }
        }
    )
}
