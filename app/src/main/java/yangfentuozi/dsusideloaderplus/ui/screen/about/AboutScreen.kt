package yangfentuozi.dsusideloaderplus.ui.screen.about

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.cards.updater.UpdaterCard
import yangfentuozi.dsusideloaderplus.ui.components.ApplicationScreen
import yangfentuozi.dsusideloaderplus.ui.components.SettingsItem
import yangfentuozi.dsusideloaderplus.ui.components.SplicedColumnGroup
import yangfentuozi.dsusideloaderplus.ui.components.TopBar
import yangfentuozi.dsusideloaderplus.ui.screen.Destinations
import yangfentuozi.dsusideloaderplus.util.collectAsStateWithLifecycle

object AboutLinks {
    const val CONTRIBUTORS_URL = "https://github.com/VegaBobo/DSU-Sideloader/graphs/contributors"
    const val REPOSITORY_URL = "https://github.com/VegaBobo/DSU-Sideloader"
    const val WSTXDA_GITHUB = "https://github.com/WSTxda"
    const val VEGABOBO_GITHUB = "https://github.com/VegaBobo"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navigate: (String) -> Unit,
    aboutViewModel: AboutViewModel = hiltViewModel(),
) {
    val uiState by aboutViewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val translators = stringResource(id = R.string.translators_list)

    LaunchedEffect(Unit) {
        aboutViewModel.resetDeveloperOptionsCounter()
        uiState.toastDisplay.collectLatest {
            when (it) {
                DevOptToastDisplay.ENABLED_DEV_OPT ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.developer_options_enabled),
                        Toast.LENGTH_LONG,
                    ).show()

                DevOptToastDisplay.DISABLED_DEV_OPT ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.developer_options_disabled),
                        Toast.LENGTH_LONG,
                    ).show()

                else -> {}
            }
        }
    }

    ApplicationScreen(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.about),
                scrollBehavior = it,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        UpdaterCard(
            uiState = uiState.updaterCardState,
            isUpdaterAvailable = uiState.isUpdaterAvailable,
            onClickImage = { aboutViewModel.onClickImage() },
            onClickCheckUpdates = { aboutViewModel.onClickCheckUpdates() },
            onClickDownloadUpdate = { aboutViewModel.onClickDownloadUpdate() },
            onClickViewChangelog = { uriHandler.openUri(aboutViewModel.response.changelogUrl) },
        )
        SplicedColumnGroup(title = stringResource(id = R.string.application)) {
            item {
                SettingsItem(
                    title = stringResource(id = R.string.github_repo),
                    summary = stringResource(id = R.string.github_repo_description),
                    onClick = { uriHandler.openUri(AboutLinks.REPOSITORY_URL) },
                )
            }
            item {
                SettingsItem(
                    title = stringResource(id = R.string.libraries_title),
                    summary = stringResource(id = R.string.libraries_description),
                    onClick = { navigate(Destinations.Libraries) },
                )
            }
        }
        SplicedColumnGroup(title = stringResource(id = R.string.collaborators)) {
            item {
                SettingsItem(
                    title = "VegaBobo",
                    summary = stringResource(id = R.string.role_developer),
                    onClick = { uriHandler.openUri(AboutLinks.VEGABOBO_GITHUB) },
                )
            }
            item {
                SettingsItem(
                    title = "WSTxda",
                    summary = stringResource(id = R.string.role_design_icon),
                    onClick = { uriHandler.openUri(AboutLinks.WSTXDA_GITHUB) },
                )
            }
            item(visible = translators.isNotEmpty() && translators != "translators_list") {
                SettingsItem(
                    title = stringResource(id = R.string.translators_title),
                    summary = translators,
                    onClick = null
                )
            }
            item {
                SettingsItem(
                    title = stringResource(id = R.string.contributors_title),
                    summary = stringResource(id = R.string.contributors_text),
                    onClick = { uriHandler.openUri(AboutLinks.CONTRIBUTORS_URL) },
                )
            }
        }
    }
}
