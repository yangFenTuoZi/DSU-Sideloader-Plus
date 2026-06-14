package vegabobo.dsusideloader.ui.cards

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton

@Composable
fun CopyableTextCard(
    text: String,
    showToast: Boolean = true,
) {
    val context = LocalContext.current
    val clipboard: Clipboard = LocalClipboard.current
    val copiedText = stringResource(id = R.string.copied)
    val scope = rememberCoroutineScope()

    SimpleCard(
        text = text,
        content = {
            Row {
                Spacer(modifier = Modifier.weight(1F))
                PrimaryButton(
                    text = stringResource(id = R.string.copy_text),
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("", AnnotatedString(text))))
                            if (showToast) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                )
            }
        },
    )
}
