package yangfentuozi.dsusideloaderplus.ui.screen.adb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.cards.CopyableTextCard
import yangfentuozi.dsusideloaderplus.ui.components.ApplicationScreen
import yangfentuozi.dsusideloaderplus.ui.components.TopBar
import yangfentuozi.dsusideloaderplus.ui.screen.Destinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbScreen(
    navigate: (String) -> Unit,
    adbViewModel: AdbViewModel = hiltViewModel(),
) {
    val scriptPath = adbViewModel.obtainScriptPath()

    val startInstallationCommand = "sh \"$scriptPath\""
    val startInstallationCommandAdb = "adb shell $startInstallationCommand"
    ApplicationScreen(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.installation),
                scrollBehavior = it,
                onClickIcon = { navigate(Destinations.Preferences) },
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
        content = {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.adb_how_to_adb_shell),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CopyableTextCard(text = startInstallationCommandAdb)
                Text(
                    text = stringResource(id = R.string.adb_how_to_shell),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CopyableTextCard(text = startInstallationCommand)
                Text(
                    text = stringResource(id = R.string.adb_how_to_done),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
