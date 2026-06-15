package yangfentuozi.dsusideloaderplus.ui.cards

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.buttons.PrimaryButton
import yangfentuozi.dsusideloaderplus.ui.theme.AppShape

@Composable
fun CopyableTextCard(
    text: String,
    showToast: Boolean = true,
) {
    val context = LocalContext.current
    val clipboard: Clipboard = LocalClipboard.current
    val copiedText = stringResource(id = R.string.copied)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShape.SplicedGroup.single)
            .background(MaterialTheme.colorScheme.surfaceBright)
            .padding(16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    }
}
