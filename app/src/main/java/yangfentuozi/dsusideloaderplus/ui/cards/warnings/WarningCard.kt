package yangfentuozi.dsusideloaderplus.ui.cards.warnings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.ui.components.SettingsItem
import yangfentuozi.dsusideloaderplus.ui.theme.AppShape

@Composable
internal fun WarningCard(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceBright,
        shape = AppShape.SplicedGroup.single,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsItem(
                title = title,
                onClick = null,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                content()
            }
        }
    }
}
