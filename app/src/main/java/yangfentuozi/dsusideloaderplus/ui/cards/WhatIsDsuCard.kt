package yangfentuozi.dsusideloaderplus.ui.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.SettingsItem
import yangfentuozi.dsusideloaderplus.ui.components.buttons.SecondaryButton

@Composable
fun DsuInfoCard(
    modifier: Modifier = Modifier,
    onClickViewDocs: () -> Unit,
    onClickLearnMore: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsItem(
            title = stringResource(id = R.string.what_is_dsu),
            onClick = null,
            columnTrailingContent = {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.what_is_dsu_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Justify,
                    )
                    Row(modifier = Modifier.padding(top = 6.dp)) {
                        Spacer(modifier = Modifier.weight(1F))
                        SecondaryButton(
                            text = stringResource(id = R.string.view_docs),
                            onClick = onClickViewDocs,
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        SecondaryButton(
                            text = stringResource(id = R.string.learn_more),
                            onClick = onClickLearnMore,
                        )
                    }
            })
    }
}
