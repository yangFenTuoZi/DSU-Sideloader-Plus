package yangfentuozi.dsusideloaderplus.ui.components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import yangfentuozi.dsusideloaderplus.ui.theme.AppShape

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    colorButton: Color? = null,
    colorText: Color? = null,
    textButton: Boolean = false,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    if (textButton) {
        TextButton(
            onClick = onClick,
            enabled = isEnabled,
            shape = AppShape.SplicedGroup.single,
        ) {
            Text(text = text)
        }
    } else {
        Button(
            modifier = modifier,
            onClick = onClick,
            enabled = isEnabled,
            shape = AppShape.SplicedGroup.single,
            colors =
            if (colorButton != null) {
                ButtonDefaults.buttonColors(
                    containerColor = colorButton,
                    contentColor = colorText ?: MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors()
            },
        ) {
            Text(text = text)
            content()
        }
    }
}
