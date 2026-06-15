package yangfentuozi.dsusideloaderplus.ui.screen.home

import yangfentuozi.dsusideloaderplus.preparation.InstallationStep

data class InstallationCardState(
    val installationStep: InstallationStep = InstallationStep.NOT_INSTALLING,
    val errorText: String = "",
    val isProgressBarIndeterminate: Boolean = false,
    val installationProgress: Float = 0F,
    val currentPartitionText: String = "",
)

data class UserDataCardState(
    val isSelected: Boolean = false,
    val isError: Boolean = false,
    val text: String = "",
    val maximumAllowed: Int = 0,
    val preserveSelected: Boolean = false,
)

data class ImageSizeCardState(
    val isSelected: Boolean = false,
    val text: String = "",
)

enum class AdditionalCardState {
    NONE,
    SETUP_STORAGE,
    UNAVAIABLE_STORAGE,
    NO_DYNAMIC_PARTITIONS,
    MISSING_READ_LOGS_PERMISSION,
    GRANTING_READ_LOGS_PERMISSION,
    BOOTLOADER_UNLOCKED_WARNING,
}

enum class SheetDisplayState {
    NONE,
    IMAGESIZE_WARNING,
    CONFIRM_INSTALLATION,
    CANCEL_INSTALLATION,
    DISCARD_DSU,
    VIEW_LOGS,
}

data class HomeUiState(
    val installationCard: InstallationCardState = InstallationCardState(),
    val userDataCard: UserDataCardState = UserDataCardState(),
    val imageSizeCard: ImageSizeCardState = ImageSizeCardState(),
    val additionalCard: AdditionalCardState = AdditionalCardState.NONE,
    val sheetDisplay: SheetDisplayState = SheetDisplayState.NONE,
    val installationLogs: String = "",
    val passedInitialChecks: Boolean = false,
    val shouldKeepScreenOn: Boolean = false,
    val isDsuInstalled: Boolean = false,
) {
    fun isInstalling(): Boolean {
        return installationCard.installationStep != InstallationStep.NOT_INSTALLING &&
               installationCard.installationStep != InstallationStep.DSU_ALREADY_INSTALLED
    }
}
