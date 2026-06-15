package yangfentuozi.dsusideloaderplus.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.preferences.AppPrefs
import yangfentuozi.dsusideloaderplus.ui.components.ApplicationScreen
import yangfentuozi.dsusideloaderplus.ui.components.DialogLikeBottomSheet
import yangfentuozi.dsusideloaderplus.ui.components.M3ESwitchWidget
import yangfentuozi.dsusideloaderplus.ui.components.SettingsItem
import yangfentuozi.dsusideloaderplus.ui.components.SplicedColumnGroup
import yangfentuozi.dsusideloaderplus.ui.components.TopBar
import yangfentuozi.dsusideloaderplus.ui.screen.Destinations
import yangfentuozi.dsusideloaderplus.util.OperationMode
import yangfentuozi.dsusideloaderplus.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navigate: (String) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        settingsViewModel.checkDevOpt()
    }

    ApplicationScreen(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.settings),
                scrollBehavior = it,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        SplicedColumnGroup(title = stringResource(id = R.string.installation)) {
            item {
                M3ESwitchWidget(
                    title = stringResource(id = R.string.builtin_installer),
                    summary =
                        if (settingsViewModel.isAndroidQ()) {
                            stringResource(id = R.string.unsupported)
                        } else if (uiState.isRoot) {
                            stringResource(id = R.string.builtin_installer_description)
                        } else {
                            stringResource(R.string.requires_root)
                        },
                    enabled = uiState.isRoot && !settingsViewModel.isAndroidQ(),
                    checked = uiState.preferences[AppPrefs.USE_BUILTIN_INSTALLER]!!,
                    onCheckedChange = {
                        if (it) {
                            settingsViewModel.updateSheetDisplay(DialogSheetState.BUILT_IN_INSTALLER)
                        }
                        settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, it)
                    },
                )
            }
            item {
                M3ESwitchWidget(
                    title = stringResource(id = R.string.unmount_sd_title),
                    summary = stringResource(id = R.string.unmount_sd_description),
                    checked = uiState.preferences[AppPrefs.UMOUNT_SD]!!,
                    onCheckedChange = {
                        settingsViewModel.togglePreference(
                            AppPrefs.UMOUNT_SD,
                            it
                        )
                    },
                )
            }
            item {
                M3ESwitchWidget(
                    title = stringResource(id = R.string.keep_screen_on),
                    checked = uiState.preferences[AppPrefs.KEEP_SCREEN_ON]!!,
                    onCheckedChange = {
                        settingsViewModel.togglePreference(
                            AppPrefs.KEEP_SCREEN_ON,
                            it
                        )
                    },
                )
            }
        }

        if (uiState.isDevOptEnabled) {
            SplicedColumnGroup(title = stringResource(id = R.string.developer_options)) {
                item {
                    M3ESwitchWidget(
                        title = stringResource(id = R.string.storage_check_title),
                        summary = stringResource(id = R.string.storage_check_description),
                        checked = uiState.preferences[AppPrefs.DISABLE_STORAGE_CHECK]!!,
                        onCheckedChange = {
                            if (it) {
                                settingsViewModel.updateSheetDisplay(DialogSheetState.DISABLE_STORAGE_CHECK)
                            }
                            settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, it)
                        },
                    )
                }
                item(visible = settingsViewModel.getOperationMode() != OperationMode.ADB) {
                    M3ESwitchWidget(
                        title = stringResource(id = R.string.full_logcat_logging_title),
                        summary = stringResource(id = R.string.full_logcat_logging_description),
                        checked = uiState.preferences[AppPrefs.FULL_LOGCAT_LOGGING]!!,
                        onCheckedChange = {
                            settingsViewModel.togglePreference(
                                AppPrefs.FULL_LOGCAT_LOGGING,
                                it
                            )
                        },
                    )
                }
            }
        }

        SplicedColumnGroup(title = stringResource(id = R.string.other)) {
            item {
                SettingsItem(
                    title = stringResource(id = R.string.operation_mode),
                    summary = settingsViewModel.checkOperationMode(),
                    onClick = null
                )
            }
            item {
                SettingsItem(
                    title = stringResource(id = R.string.about),
                    summary = stringResource(id = R.string.about_description),
                    onClick = { navigate(Destinations.About) },
                )
            }
        }
    }

    when (uiState.dialogSheetDisplay) {
        DialogSheetState.BUILT_IN_INSTALLER ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.experimental_feature),
                icon = Icons.Outlined.NewReleases,
                text = stringResource(id = R.string.experimental_feature_description),
                confirmText = stringResource(id = R.string.yes),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, false)
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                },
                onClickConfirm = { settingsViewModel.updateSheetDisplay(DialogSheetState.NONE) },
            )

        DialogSheetState.DISABLE_STORAGE_CHECK ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.warning_storage_check_title),
                icon = Icons.Outlined.WarningAmber,
                text = stringResource(id = R.string.warning_storage_check_description),
                confirmText = stringResource(id = R.string.continue_anyway),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, false)
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                },
                onClickConfirm = { settingsViewModel.updateSheetDisplay(DialogSheetState.NONE) },
            )

        else -> {}
    }
}
