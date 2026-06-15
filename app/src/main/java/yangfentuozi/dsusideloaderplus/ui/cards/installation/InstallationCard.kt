package yangfentuozi.dsusideloaderplus.ui.cards.installation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.preparation.InstallationStep
import yangfentuozi.dsusideloaderplus.ui.cards.installation.content.DsuInstalledCardContent
import yangfentuozi.dsusideloaderplus.ui.cards.installation.content.NotInstallingCardContent
import yangfentuozi.dsusideloaderplus.ui.cards.installation.content.ProgressableCardContent
import yangfentuozi.dsusideloaderplus.ui.screen.home.InstallationCardState
import yangfentuozi.dsusideloaderplus.ui.util.launcherAcResult

@Composable
fun InstallationCard(
    uiState: InstallationCardState,
    modifier: Modifier = Modifier,
    minPercentageOfFreeStorage: String,
    onClickClear: () -> Unit,
    onClickRetryInstallation: () -> Unit,
    onClickUnmountSdCardAndRetry: () -> Unit,
    onClickSetSeLinuxPermissive: () -> Unit,
    onClickCancelInstallation: () -> Unit,
    onClickDiscardInstalledGsiAndInstall: () -> Unit,
    onClickDiscardDsu: () -> Unit,
    onClickRebootToDynOS: () -> Unit,
    onClickManageImages: () -> Unit,
    onSelectFileSuccess: (Uri) -> Unit,
    onClickViewLogs: () -> Unit,
    onClickViewCommands: () -> Unit,
) {
    var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
    chooseFile.type = "*/*"
    val mimetypes = arrayOf(
        "application/gzip",
        "application/x-gzip",
        "application/x-xz",
        "application/zip",
        "application/octet-stream",
    )
    chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
    chooseFile = Intent.createChooser(chooseFile, "")

    val launcherSelectFile = launcherAcResult {
        onSelectFileSuccess(it)
    }
    val onClickInstall = {
        launcherSelectFile.launch(chooseFile)
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        when (uiState.installationStep) {
            InstallationStep.NOT_INSTALLING ->
                NotInstallingCardContent(
                    onClickInstall = onClickInstall,
                )

            InstallationStep.DSU_ALREADY_INSTALLED ->
                DsuInstalledCardContent(
                    onClickInstall = onClickInstall,
                    onClickManageImages = onClickManageImages,
                    onClickRebootToDynOS = onClickRebootToDynOS,
                    onClickDiscardDsu = onClickDiscardDsu,
                )

            InstallationStep.DSU_ALREADY_RUNNING_DYN_OS ->
                ProgressableCardContent(
                    text = stringResource(R.string.already_running_dsu),
                )

            InstallationStep.PROCESSING ->
                ProgressableCardContent(
                    text = stringResource(R.string.processing),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    isIndeterminate = true,
                )

            InstallationStep.COPYING_FILE ->
                ProgressableCardContent(
                    text = stringResource(R.string.copying_file),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )

            InstallationStep.DECOMPRESSING_XZ ->
                ProgressableCardContent(
                    text = stringResource(R.string.decompressing_xz),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )

            InstallationStep.COMPRESSING_TO_GZ ->
                ProgressableCardContent(
                    text = stringResource(R.string.compressing_to_gz),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )

            InstallationStep.DECOMPRESSING_GZIP ->
                ProgressableCardContent(
                    text = stringResource(R.string.extracting_file),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )

            InstallationStep.EXTRACTING_FILE ->
                ProgressableCardContent(
                    text = stringResource(R.string.extracting_file),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )

            InstallationStep.DISCARD_CURRENT_GSI -> {
                ProgressableCardContent(
                    text = stringResource(R.string.discard_dsu_otg),
                    textFirstButton = stringResource(id = R.string.discard_dsu),
                    onClickFirstButton = onClickDiscardInstalledGsiAndInstall,
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    progress = uiState.installationProgress,
                )
            }

            InstallationStep.WAITING_USER_CONFIRMATION -> {
                ProgressableCardContent(
                    text = stringResource(R.string.installation_prompt),
                    textFirstButton = stringResource(id = R.string.try_again),
                    onClickFirstButton = onClickRetryInstallation,
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                )
            }

            InstallationStep.PROCESSING_LOG_READABLE ->
                ProgressableCardContent(
                    text = stringResource(R.string.installing),
                    textFirstButton = stringResource(id = R.string.cancel),
                    onClickFirstButton = onClickCancelInstallation,
                    textSecondButton = stringResource(id = R.string.view_logs),
                    onClickSecondButton = onClickViewLogs,
                    showProgressBar = true,
                    isIndeterminate = true,
                )

            InstallationStep.INSTALLING -> {
                ProgressableCardContent(
                    text = stringResource(
                        R.string.installing_partition,
                        uiState.currentPartitionText
                    ),
                    textFirstButton = stringResource(id = R.string.cancel),
                    onClickFirstButton = onClickCancelInstallation,
                    textSecondButton = stringResource(id = R.string.view_logs),
                    onClickSecondButton = onClickViewLogs,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )
            }

            InstallationStep.INSTALLING_ROOTED -> {
                ProgressableCardContent(
                    text = stringResource(
                        R.string.installing_partition,
                        uiState.currentPartitionText
                    ),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )
            }

            InstallationStep.CREATING_PARTITION ->
                ProgressableCardContent(
                    text = stringResource(
                        R.string.creating_partition,
                        uiState.currentPartitionText
                    ),
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                    showProgressBar = true,
                    progress = uiState.installationProgress,
                )

            InstallationStep.ERROR ->
                ProgressableCardContent(
                    text = stringResource(R.string.unknown_error, uiState.errorText),
                    textFirstButton = stringResource(id = R.string.view_logs),
                    onClickFirstButton = onClickViewLogs,
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.ERROR_CANCELED ->
                ProgressableCardContent(
                    text = stringResource(R.string.installation_canceled),
                    textFirstButton = stringResource(id = R.string.view_logs),
                    onClickFirstButton = onClickViewLogs,
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.ERROR_REQUIRES_DISCARD_DSU ->
                ProgressableCardContent(
                    text = stringResource(R.string.discard_dsu_otg),
                    textFirstButton = stringResource(id = R.string.discard),
                    onClickFirstButton = onClickDiscardInstalledGsiAndInstall,
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                )

            InstallationStep.ERROR_ALREADY_RUNNING_DYN_OS ->
                ProgressableCardContent(
                    text = stringResource(R.string.already_running_dsu),
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.ERROR_CREATE_PARTITION ->
                ProgressableCardContent(
                    text = stringResource(R.string.failed_create_partition),
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.ERROR_EXTERNAL_SDCARD_ALLOC ->
                ProgressableCardContent(
                    text = stringResource(
                        R.string.allocation_error_description,
                        uiState.errorText,
                    ),
                    textFirstButton = stringResource(id = R.string.allocation_error_action),
                    onClickFirstButton = onClickUnmountSdCardAndRetry,
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                )

            InstallationStep.ERROR_NO_AVAIL_STORAGE ->
                ProgressableCardContent(
                    text = stringResource(
                        R.string.storage_error_description,
                        minPercentageOfFreeStorage
                    ),
                    textFirstButton = stringResource(id = R.string.try_again),
                    onClickFirstButton = onClickRetryInstallation,
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                )

            InstallationStep.ERROR_F2FS_WRONG_PATH ->
                ProgressableCardContent(
                    text = stringResource(
                        R.string.fs_features_error_description,
                        uiState.errorText,
                    ),
                    textFirstButton = stringResource(id = R.string.view_logs),
                    onClickFirstButton = onClickViewLogs,
                    textSecondButton = stringResource(id = R.string.clear),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.ERROR_EXTENTS ->
                ProgressableCardContent(
                    text = stringResource(R.string.extents_error_description),
                    textFirstButton = stringResource(id = R.string.view_logs),
                    onClickFirstButton = onClickViewLogs,
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.ERROR_SELINUX ->
                ProgressableCardContent(
                    text = stringResource(R.string.selinux_error_description),
                    textFirstButton = stringResource(id = R.string.selinux_error_action),
                    onClickFirstButton = onClickSetSeLinuxPermissive,
                    textSecondButton = stringResource(id = R.string.cancel),
                    onClickSecondButton = onClickCancelInstallation,
                )

            InstallationStep.ERROR_SELINUX_ROOTLESS ->
                ProgressableCardContent(
                    text = stringResource(R.string.selinux_error_description),
                    textFirstButton = stringResource(id = R.string.view_logs),
                    onClickFirstButton = onClickViewLogs,
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.INSTALL_SUCCESS ->
                ProgressableCardContent(
                    text = stringResource(R.string.installation_finished_rootless),
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )

            InstallationStep.INSTALL_SUCCESS_REBOOT_DYN_OS ->
                ProgressableCardContent(
                    text = stringResource(R.string.installation_finished),
                    textFirstButton = stringResource(id = R.string.reboot_into_dsu),
                    onClickFirstButton = onClickRebootToDynOS,
                    textSecondButton = stringResource(id = R.string.discard),
                    onClickSecondButton = onClickDiscardDsu,
                )

            InstallationStep.REQUIRES_ADB_CMD_TO_CONTINUE ->
                ProgressableCardContent(
                    text = stringResource(R.string.require_adb_cmd_to_continue),
                    textFirstButton = stringResource(id = R.string.see_commands),
                    onClickFirstButton = onClickViewCommands,
                    textSecondButton = stringResource(id = R.string.mreturn),
                    onClickSecondButton = onClickClear,
                )
        }
    }
}
