package yangfentuozi.dsusideloaderplus.ui.screen.images

data class DsuImageState(
    val prefix: String,
    val name: String,
)

enum class ImagesOperationState {
    IDLE,
    LOADING,
    ADDING,
    EXPORTING,
    REPLACING,
    DELETING,
    ERROR,
}

enum class ImagesSheetDisplayState {
    NONE,
    CONFIRM_ADD_DSU_IMAGE,
    CONFIRM_REPLACE_DSU_IMAGE,
    DELETE_DSU_IMAGE,
}

data class ImagesUiState(
    val images: List<DsuImageState> = emptyList(),
    val availablePrefixes: List<String> = emptyList(),
    val currentImageName: String = "",
    val errorText: String = "",
    val pendingImage: DsuImageState? = null,
    val pendingPrefix: String = "",
    val newImageName: String = "",
    val newImageNameError: Boolean = false,
    val replacementFileName: String = "",
    val operationState: ImagesOperationState = ImagesOperationState.LOADING,
    val sheetDisplay: ImagesSheetDisplayState = ImagesSheetDisplayState.NONE,
)
