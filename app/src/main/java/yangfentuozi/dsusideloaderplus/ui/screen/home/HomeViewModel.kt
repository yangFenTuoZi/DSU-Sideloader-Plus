package yangfentuozi.dsusideloaderplus.ui.screen.home

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yangfentuozi.dsusideloaderplus.BuildConfig
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.core.BaseViewModel
import yangfentuozi.dsusideloaderplus.core.StorageManager
import yangfentuozi.dsusideloaderplus.installer.adb.AdbInstallationHandler
import yangfentuozi.dsusideloaderplus.installer.privileged.DsuInstallationHandler
import yangfentuozi.dsusideloaderplus.installer.privileged.LogcatDiagnostic
import yangfentuozi.dsusideloaderplus.installer.root.DSUInstaller
import yangfentuozi.dsusideloaderplus.model.DSUInstallationSource
import yangfentuozi.dsusideloaderplus.model.Session
import yangfentuozi.dsusideloaderplus.preferences.AppPrefs
import yangfentuozi.dsusideloaderplus.preparation.InstallationStep
import yangfentuozi.dsusideloaderplus.preparation.Preparation
import yangfentuozi.dsusideloaderplus.service.PrivilegedProvider
import yangfentuozi.dsusideloaderplus.util.DevicePropUtils
import yangfentuozi.dsusideloaderplus.util.FilenameUtils
import yangfentuozi.dsusideloaderplus.util.OperationMode
import yangfentuozi.dsusideloaderplus.util.OperationModeUtils
import yangfentuozi.dsusideloaderplus.util.StorageUtils

@HiltViewModel
class HomeViewModel @Inject constructor(
    val application: Application,
    override val dataStore: DataStore<Preferences>,
    private val storageManager: StorageManager,
    var session: Session,
) : BaseViewModel(dataStore) {

    private val tag = this.javaClass.simpleName

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var checkDynamicPartitions = true
    var checkUnavaiableStorage = true
    var checkReadLogsPermission = true
    var disabledStorageCheck = false

    var installationJob: Job = Job()
    var logger: LogcatDiagnostic? = null

    private val allocPercentage = DevicePropUtils.getGsidBinaryAllowedPerc()
    val allocPercentageInt = String.format("%.0f", allocPercentage * 100).toInt()

    private val storageStats = StorageUtils.getAllocInfo(allocPercentage)
    private val hasAvailableStorage = storageStats.first
    private val maximumAllowedForAllocation = storageStats.second

    //
    // Helper methods used for controlling UI State
    //

    private fun updateAdditionalCardState(additionalCard: AdditionalCardState) =
        _uiState.update { it.copy(additionalCard = additionalCard) }

    private fun updateUserdataCard(update: (UserDataCardState) -> UserDataCardState) =
        _uiState.update { it.copy(userDataCard = update(it.userDataCard.copy())) }

    private fun updateInstallationCard(update: (InstallationCardState) -> InstallationCardState) =
        _uiState.update { it.copy(installationCard = update(it.installationCard.copy())) }

    private fun updateImageSizeCard(update: (ImageSizeCardState) -> ImageSizeCardState) =
        _uiState.update { it.copy(imageSizeCard = update(it.imageSizeCard.copy())) }

    private fun updateSheetState(sheetDisplay: SheetDisplayState) =
        _uiState.update { it.copy(sheetDisplay = sheetDisplay) }

    fun resetInstallationCard() =
        _uiState.update {
            // If DSU is installed, restore the DSU_ALREADY_INSTALLED state
            // instead of resetting to NOT_INSTALLING
            val installationStep = if (it.isDsuInstalled) {
                InstallationStep.DSU_ALREADY_INSTALLED
            } else {
                InstallationStep.NOT_INSTALLING
            }

            it.copy(
                installationCard = InstallationCardState(installationStep = installationStep),
                sheetDisplay = SheetDisplayState.NONE,
            )
        }

    fun dismissSheet() = updateSheetState(SheetDisplayState.NONE)

    //
    // Home startup and checks
    //

    init {
        // Check if a DSU is already installed
        // Root-only because MANAGE_DYNAMIC_SYSTEM is required
        if (session.isRoot()) {
            PrivilegedProvider.run {
                if (isInUse) {
                    updateInstallationCard { it.copy(installationStep = InstallationStep.DSU_ALREADY_RUNNING_DYN_OS) }
                    return@run
                }
                if (isInstalled) {
                    // Track DSU installation status and show update UI
                    // Set preserve userdata to true by default
                    _uiState.update {
                        it.copy(
                            isDsuInstalled = true,
                            userDataCard = it.userDataCard.copy(preserveSelected = true)
                        )
                    }
                    updateInstallationCard { it.copy(installationStep = InstallationStep.DSU_ALREADY_INSTALLED) }
                    return@run
                }
            }
        }
    }

    fun initialChecks() {
        if (checkDynamicPartitions && !DevicePropUtils.hasDynamicPartitions()) {
            updateAdditionalCardState(AdditionalCardState.NO_DYNAMIC_PARTITIONS)
            return
        }

        if (checkUnavaiableStorage && !hasAvailableStorage) {
            updateAdditionalCardState(AdditionalCardState.UNAVAIABLE_STORAGE)
            return
        }

        viewModelScope.launch {
            val result = readStringPref(AppPrefs.SAF_PATH)
            if (!storageManager.arePermissionsGrantedToFolder(result)) {
                updateAdditionalCardState(AdditionalCardState.SETUP_STORAGE)
                return@launch
            }

            if (session.getOperationMode() == OperationMode.SHIZUKU && checkReadLogsPermission &&
                !OperationModeUtils.isReadLogsPermissionGranted(application)
            ) {
                _uiState.update { it.copy(passedInitialChecks = false) }
                updateAdditionalCardState(AdditionalCardState.MISSING_READ_LOGS_PERMISSION)
                return@launch
            }

            val seenUnlockedBootloaderWarning = readBoolPref(AppPrefs.BOOTLOADER_UNLOCKED_WARNING)
            if (!seenUnlockedBootloaderWarning) {
                _uiState.update { it.copy(passedInitialChecks = false) }
                updateAdditionalCardState(AdditionalCardState.BOOTLOADER_UNLOCKED_WARNING)
                return@launch
            }

            updateAdditionalCardState(AdditionalCardState.NONE)
            _uiState.update { it.copy(passedInitialChecks = true) }
        }
    }

    fun setupUserPreferences() {
        viewModelScope.launch {
            val shouldKeepScreenOn = readBoolPref(AppPrefs.KEEP_SCREEN_ON)
            Log.d(tag, "shouldKeepScreenOn: $shouldKeepScreenOn")
            _uiState.update { it.copy(shouldKeepScreenOn = shouldKeepScreenOn) }

            disabledStorageCheck = readBoolPref(AppPrefs.DISABLE_STORAGE_CHECK)
            Log.d(tag, "disabledStorageCheck: $shouldKeepScreenOn")
        }
    }

    fun overrideDynamicPartitionCheck() {
        checkDynamicPartitions = false
        Log.d(tag, "checkDynamicPartitions: $checkDynamicPartitions")
        initialChecks()
    }

    fun overrideUnavaiableStorage() {
        checkUnavaiableStorage = false
        Log.d(tag, "checkUnavaiableStorage: $checkUnavaiableStorage")
        initialChecks()
    }

    fun onClickBootloaderUnlockedWarning() {
        viewModelScope.launch {
            updateBoolPref(AppPrefs.BOOTLOADER_UNLOCKED_WARNING, true)
            initialChecks()
        }
    }

    //
    // Installation
    //

    fun obtainSelectedFilename(): String = session.userSelection.selectedFileName

    fun onClickCancel() {
        if (uiState.value.isInstalling()) {
            updateSheetState(SheetDisplayState.CANCEL_INSTALLATION)
        }
    }

    fun onClickInstall() {
        session.userSelection.setUserDataSize(uiState.value.userDataCard.text)
        session.userSelection.setImageSize(uiState.value.imageSizeCard.text)
        updateSheetState(SheetDisplayState.CONFIRM_INSTALLATION)
    }

    fun onConfirmInstallationSheet() {
        dismissSheet()
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }
        installationJob = Job()
        viewModelScope.launch(Dispatchers.IO + installationJob) {
            session.preferences.isUnmountSdCard = readBoolPref(AppPrefs.UMOUNT_SD)
            session.preferences.useBuiltinInstaller = readBoolPref(AppPrefs.USE_BUILTIN_INSTALLER)
            Preparation(
                storageManager = storageManager,
                session = session,
                job = installationJob,
                onStepUpdate = this@HomeViewModel::onStepUpdate,
                onPreparationProgressUpdate = this@HomeViewModel::onPreparationProgressUpdate,
                onCanceled = this@HomeViewModel::onClickCancelInstallationButton,
                onPreparationFinished = this@HomeViewModel::onPreparationFinished,
            ).invoke()
        }
    }

    private fun onPreparationFinished(dsuInstallation: DSUInstallationSource) {
        Log.d(tag, "DSU preparation finished, result: $dsuInstallation")
        session.dsuInstallation = dsuInstallation
        startInstallation()
    }

    private fun startInstallation() {
        Log.d(tag, "startInstallation(), session: \n$session")
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }

        if (session.getOperationMode() == OperationMode.ADB) {
            setupAdbInstallation()
            return
        }

        if (session.preferences.useBuiltinInstaller && session.isRoot()) {
            startDSUInstallation()
            return
        }

        startPrivilegedInstallation()
    }

    private fun setupAdbInstallation() {
        AdbInstallationHandler(storageManager, session).generate { scriptPath ->
            Log.d(tag, "Installation script generated: $scriptPath")
            session.installationScriptPath = scriptPath
            resetInstallationCard()
            updateInstallationCard { it.copy(installationStep = InstallationStep.REQUIRES_ADB_CMD_TO_CONTINUE) }
        }
    }

    private fun startDSUInstallation() {
        DSUInstaller(
            application = application,
            userdataSize = session.userSelection.userSelectedUserdata,
            dsuInstallation = session.dsuInstallation,
            installationJob = installationJob,
            onInstallationError = this::onInstallationError,
            onInstallationProgressUpdate = this::onInstallationProgressUpdate,
            onCreatePartition = this::onCreatePartition,
            onInstallationStepUpdate = this::onStepUpdate,
            onInstallationSuccess = this::onRootInstallationSuccess,
            preserveUserdata = session.preferences.preserveUserdata,
        ).invoke()
    }

    private fun startPrivilegedInstallation() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.WAITING_USER_CONFIRMATION) }
        DsuInstallationHandler(session).startInstallation()
        if (session.isRoot() || OperationModeUtils.isReadLogsPermissionGranted(application)) {
            startLogging()
        } else {
            updateInstallationCard { it.copy(installationStep = InstallationStep.INSTALL_SUCCESS) }
        }
    }

    // Track and diagnose installation by reading logcat
    private fun startLogging() {
        if (logger == null) {
            logger = LogcatDiagnostic(
                onInstallationError = this::onInstallationError,
                onStepUpdate = this::onStepUpdate,
                onInstallationProgressUpdate = this::onInstallationProgressUpdate,
                onInstallationSuccess = this::onInstallationSuccess,
                onLogLineReceived = this::onLogLineReceived,
            )
        }
        viewModelScope.launch(Dispatchers.IO + installationJob) {
            logger!!.shouldLogEverything = readBoolPref(AppPrefs.FULL_LOGCAT_LOGGING)
            logger!!.startLogging(generateUsefulLogInfo())
        }
    }

    private fun generateUsefulLogInfo(): String {
        return "Device: ${Build.MODEL}\n" +
            "SDK: Android ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})\n" +
            "$session\n" +
            "Package: ${BuildConfig.APPLICATION_ID}\n" +
            "Version: ${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE} (${BuildConfig.BUILD_TYPE}})\n" +
            "checkDynamicPartitions: $checkDynamicPartitions\n" +
            "checkUnavaiableStorage: $checkUnavaiableStorage\n" +
            "checkReadLogsPermission: $checkReadLogsPermission\n" +
            "allocPercentage: $allocPercentage\n" +
            "hasAvailableStorage: $hasAvailableStorage\n" +
            "maximumAllowedForAllocation: $maximumAllowedForAllocation\n"
    }

    fun saveLogs(uriToSaveLogs: Uri) {
        Log.d(tag, "Writing logs to: $uriToSaveLogs")
        storageManager.writeStringToUri(uiState.value.installationLogs, uriToSaveLogs)
    }

    fun onClickCancelInstallationButton() {
        resetInstallationCard()
        if (session.getOperationMode() != OperationMode.ADB &&
            logger != null && logger!!.isLogging.get()
        ) {
            logger!!.destroy()
            // Since stopping installation requires MANAGE_DYNAMIC_SYSTEM
            // then, we stop installation using other way, not so polite, but works :)))
            PrivilegedProvider.run { forceStopPackage("com.android.dynsystem") }
        }

        if (installationJob.isActive) {
            installationJob.cancel()
        }
        session.dsuInstallation = DSUInstallationSource()
    }

    //
    // Installation Card actions
    //

    fun onClickRebootToDynOS() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }
        PrivilegedProvider.run {
            setEnable(true, true)
            Shell.cmd("reboot").exec()
        }
    }

    fun onClickDiscardGsiAndStartInstallation() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }
        PrivilegedProvider.run {
            remove()
            forceStopPackage("com.android.dynsystem")
            startDSUInstallation()
        }
    }

    fun onClickDiscardGsi() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }
        PrivilegedProvider.run {
            remove()
            forceStopPackage("com.android.dynsystem")
            dismissSheet()
            // Reset UI state after discarding DSU
            _uiState.update {
                it.copy(
                    installationCard = InstallationCardState(),
                    sheetDisplay = SheetDisplayState.NONE,
                    isDsuInstalled = false,
                )
            }
        }
    }

    fun onClickRetryInstallation() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }
        startInstallation()
    }

    fun onClickUnmountSdCardAndRetry() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }
        session.preferences.isUnmountSdCard = true
        startInstallation()
    }

    fun onClickSetSeLinuxPermissive() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.PROCESSING) }
        viewModelScope.launch {
            Shell.cmd("setenforce 0").exec()
            delay(5000)
            startInstallation()
        }
    }

    fun showDiscardSheet() = updateSheetState(SheetDisplayState.DISCARD_DSU)

    //
    // Userdata card
    //

    fun onCheckUserdataCard() =
        updateUserdataCard { it.copy(isSelected = !it.isSelected, text = "") }

    fun onCheckPreserveUserdata(checked: Boolean) {
        updateUserdataCard { it.copy(preserveSelected = checked) }
        session.preferences.preserveUserdata = checked
    }

    fun updateUserdataSize(input: String) {
        val selectedSize = FilenameUtils.getDigits(input)
        val sizeWithSuffix = FilenameUtils.appendToDigitsToString(input, "GB")
        Log.d(
            tag,
            "disabledStorageCheck: $disabledStorageCheck, selectedSize: $selectedSize, maximumAllowedForAllocation: $maximumAllowedForAllocation",
        )

        if (!disabledStorageCheck && selectedSize.isNotEmpty() && selectedSize.toInt() > maximumAllowedForAllocation) {
            val fixedSize =
                FilenameUtils.appendToDigitsToString("$maximumAllowedForAllocation", "GB")
            updateUserdataCard {
                it.copy(
                    text = fixedSize,
                    isError = true,
                    maximumAllowed = maximumAllowedForAllocation,
                )
            }
            viewModelScope.launch {
                delay(5000)
                updateUserdataCard { it.copy(isError = false) }
            }
            return
        }

        updateUserdataCard { it.copy(text = sizeWithSuffix) }
    }

    //
    // Image size card
    //

    fun onCheckImageSizeCard() {
        if (!uiState.value.imageSizeCard.isSelected) {
            updateSheetState(SheetDisplayState.IMAGESIZE_WARNING)
        } else {
            dismissSheet()
        }
        updateImageSizeCard { it.copy(isSelected = !it.isSelected, text = "") }
    }

    fun updateImageSize(input: String) {
        val inputWithSuffix = FilenameUtils.appendToDigitsToString(input, "b")
        updateImageSizeCard { it.copy(text = inputWithSuffix) }
    }

    //
    // File selection
    //

    fun takeUriPermission(uri: Uri) {
        application.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        viewModelScope.launch {
            if (storageManager.arePermissionsGrantedToFolder(uri.toString())) {
                updateStringPref(AppPrefs.SAF_PATH, uri.toString()) { initialChecks() }
            }
        }
    }

    fun onFileSelectionResult(uri: Uri) {
        val filename = FilenameUtils.queryName(application.contentResolver, uri)
        val extension = filename.substringAfterLast(".", "")
        val supportedFiles = arrayListOf("gz", "xz", "img", "gzip")

        // DSU packages (zip files), are only supported in R+
        if (Build.VERSION.SDK_INT > 29) {
            supportedFiles.add("zip")
        }
        val isFileSupported = supportedFiles.contains(extension)
        Log.d(tag, "isFileSupported: $isFileSupported, extension: $extension, filename: $filename")

        if (!isFileSupported) {
            Toast.makeText(application, R.string.file_unsupported, Toast.LENGTH_SHORT).show()
            return
        }

        session.userSelection.selectedFileName = filename
        session.userSelection.selectedFileUri = uri
        session.userSelection.setUserDataSize(uiState.value.userDataCard.text)
        session.userSelection.setImageSize(uiState.value.imageSizeCard.text)
        updateSheetState(SheetDisplayState.CONFIRM_INSTALLATION)
    }

    //
    // Read logs permission warning
    //

    fun grantReadLogs() {
        updateAdditionalCardState(AdditionalCardState.GRANTING_READ_LOGS_PERMISSION)
        val intent = Intent()
        intent.setClassName(
            BuildConfig.APPLICATION_ID,
            "${BuildConfig.APPLICATION_ID}.MainActivity",
        )
        intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK
        PrivilegedProvider.run {
            grantPermission("android.permission.READ_LOGS")
            if (Build.VERSION.SDK_INT <= 30) {
                forceStopPackage(BuildConfig.APPLICATION_ID)
            }
            startActivity(intent)
        }
    }

    fun refuseReadLogs() {
        checkReadLogsPermission = false
        Log.d(tag, "checkReadLogsPermission: $checkReadLogsPermission")
        initialChecks()
    }

    fun showLogsWarning() {
        updateSheetState(SheetDisplayState.VIEW_LOGS)
    }

    //
    // Progress tracking
    //

    private fun onRootInstallationSuccess() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.INSTALL_SUCCESS_REBOOT_DYN_OS) }
    }

    private fun onInstallationSuccess() {
        updateInstallationCard { it.copy(installationStep = InstallationStep.INSTALL_SUCCESS) }
    }

    private fun onLogLineReceived() {
        _uiState.update { it.copy(installationLogs = logger!!.logs) }
    }

    private fun onStepUpdate(step: InstallationStep) =
        updateInstallationCard { it.copy(installationStep = step) }

    private fun onPreparationProgressUpdate(progress: Float) =
        updateInstallationCard { it.copy(installationProgress = progress) }

    private fun onInstallationError(error: InstallationStep, errorContent: String) =
        updateInstallationCard {
            if (error == InstallationStep.ERROR_SELINUX && !session.isRoot()) {
                it.copy(
                    installationStep = InstallationStep.ERROR_SELINUX_ROOTLESS,
                    errorText = errorContent,
                )
            } else {
                it.copy(installationStep = error, errorText = errorContent)
            }
        }

    private fun onInstallationProgressUpdate(progress: Float, partition: String) =
        updateInstallationCard {
            it.copy(currentPartitionText = partition, installationProgress = progress)
        }

    private fun onCreatePartition(partition: String) =
        updateInstallationCard {
            it.copy(
                installationStep = InstallationStep.CREATING_PARTITION,
                currentPartitionText = partition,
            )
        }
}
