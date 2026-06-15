package yangfentuozi.dsusideloaderplus.ui.cards.installation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.SettingsItem

@Composable
fun NotInstallingCardContent(
    onClickInstall: () -> Unit,
) {
    SettingsItem(
        title = stringResource(R.string.installation),
        summary = stringResource(R.string.select_gsi_info),
        onClick = onClickInstall
    )
}
