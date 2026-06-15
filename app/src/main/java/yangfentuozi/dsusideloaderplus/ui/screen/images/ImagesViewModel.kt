package yangfentuozi.dsusideloaderplus.ui.screen.images

import android.app.Application
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yangfentuozi.dsusideloaderplus.IPrivilegedService
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.core.BaseViewModel
import yangfentuozi.dsusideloaderplus.core.StorageManager
import yangfentuozi.dsusideloaderplus.model.Session
import yangfentuozi.dsusideloaderplus.service.PrivilegedProvider
import yangfentuozi.dsusideloaderplus.util.FilenameUtils

@HiltViewModel
class ImagesViewModel @Inject constructor(
    val application: Application,
    override val dataStore: DataStore<Preferences>,
    private val storageManager: StorageManager,
    var session: Session,
) : BaseViewModel(dataStore) {

    private val _uiState = MutableStateFlow(ImagesUiState())
    val uiState: StateFlow<ImagesUiState> = _uiState.asStateFlow()

    private var pendingNewImageUri: Uri = Uri.EMPTY
    private var pendingReplacementUri: Uri = Uri.EMPTY

    init {
        refreshImages()
    }

    fun refreshImages() {
        if (!session.isRoot()) {
            _uiState.update {
                it.copy(
                    operationState = ImagesOperationState.ERROR,
                    errorText = "Root or system mode is required.",
                )
            }
            return
        }

        _uiState.update { it.copy(operationState = ImagesOperationState.LOADING, errorText = "") }
        PrivilegedProvider.run(
            onFail = {
                _uiState.update {
                    it.copy(
                        operationState = ImagesOperationState.ERROR,
                        errorText = "Privileged service unavailable.",
                    )
                }
            },
        ) {
            val prefixes = getInstalledDsuPrefixes()
            val images = prefixes.flatMap { prefix ->
                runCatching {
                    getDsuBackingImages(prefix).map { imageName ->
                        DsuImageState(prefix = prefix, name = imageName)
                    }
                }.getOrDefault(emptyList())
            }.distinctBy { "${it.prefix}/${it.name}" }

            _uiState.update {
                it.copy(
                    images = images,
                    availablePrefixes = prefixes,
                    operationState = ImagesOperationState.IDLE,
                    errorText = "",
                )
            }
        }
    }

    fun onClickAddImage(): Boolean {
        val prefix = getDefaultImagePrefix()
        if (prefix.isBlank()) {
            onImageOperationError("No installed DSU prefix found.")
            return false
        }
        _uiState.update {
            it.copy(
                pendingPrefix = prefix,
                newImageName = "",
                newImageNameError = false,
                errorText = "",
            )
        }
        return true
    }

    fun onNewImageFileSelectionResult(uri: Uri) {
        val filename = FilenameUtils.queryName(application.contentResolver, uri)
        val extension = filename.substringAfterLast(".", "").lowercase()
        if (extension != "img") {
            Toast.makeText(application, R.string.file_unsupported, Toast.LENGTH_SHORT).show()
            clearPendingSelection()
            return
        }

        val imageName = filename.substringBeforeLast(".").trim() + "_gsi"
        pendingNewImageUri = uri
        _uiState.update {
            it.copy(
                newImageName = imageName,
                newImageNameError = !isNewImageNameValid(imageName, it.pendingPrefix),
                replacementFileName = filename,
                sheetDisplay = ImagesSheetDisplayState.CONFIRM_ADD_DSU_IMAGE,
                errorText = "",
            )
        }
    }

    fun onNewImageNameChange(imageName: String) {
        _uiState.update {
            it.copy(
                newImageName = imageName,
                newImageNameError = !isNewImageNameValid(imageName, it.pendingPrefix),
            )
        }
    }

    fun confirmAddImage() {
        val prefix = uiState.value.pendingPrefix
        val imageName = uiState.value.newImageName.trim()
        if (!isNewImageNameValid(imageName, prefix)) {
            _uiState.update { it.copy(newImageNameError = true) }
            return
        }
        val uri = pendingNewImageUri
        if (uri == Uri.EMPTY) return

        dismissSheet(clearPendingSelection = false)
        _uiState.update {
            it.copy(
                operationState = ImagesOperationState.ADDING,
                currentImageName = imageName,
                errorText = "",
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val imageSize = storageManager.getFilesizeFromUri(uri)
            val fd = application.contentResolver.openFileDescriptor(uri, "r")
            if (fd == null) {
                onImageOperationError("Unable to open image file.")
                return@launch
            }
            addImage(prefix, imageName, fd, imageSize)
        }
    }

    fun onClickReplaceImage(image: DsuImageState) {
        _uiState.update {
            it.copy(
                pendingImage = image,
                errorText = "",
            )
        }
    }

    fun onReplacementFileSelectionResult(uri: Uri) {
        val filename = FilenameUtils.queryName(application.contentResolver, uri)
        val extension = filename.substringAfterLast(".", "").lowercase()
        if (extension != "img") {
            Toast.makeText(application, R.string.file_unsupported, Toast.LENGTH_SHORT).show()
            clearPendingSelection()
            return
        }

        pendingReplacementUri = uri
        _uiState.update {
            it.copy(
                replacementFileName = filename,
                sheetDisplay = ImagesSheetDisplayState.CONFIRM_REPLACE_DSU_IMAGE,
                errorText = "",
            )
        }
    }

    fun onClickExportImage(image: DsuImageState) {
        _uiState.update {
            it.copy(
                pendingImage = image,
                errorText = "",
            )
        }
    }

    fun onExportFileSelectionResult(uri: Uri) {
        val image = uiState.value.pendingImage ?: return
        _uiState.update {
            it.copy(
                operationState = ImagesOperationState.EXPORTING,
                currentImageName = image.name,
                errorText = "",
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val fd = application.contentResolver.openFileDescriptor(uri, "wt")
            if (fd == null) {
                onImageOperationError("Unable to open output image file.")
                return@launch
            }
            exportImage(image, fd)
        }
    }

    fun confirmReplaceImage() {
        val image = uiState.value.pendingImage ?: return
        val uri = pendingReplacementUri
        if (uri == Uri.EMPTY) return

        dismissSheet(clearPendingSelection = false)
        _uiState.update {
            it.copy(
                operationState = ImagesOperationState.REPLACING,
                currentImageName = image.name,
                errorText = "",
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val imageSize = storageManager.getFilesizeFromUri(uri)
            val fd = application.contentResolver.openFileDescriptor(uri, "r")
            if (fd == null) {
                onImageOperationError("Unable to open image file.")
                return@launch
            }
            replaceImage(image, fd, imageSize)
        }
    }

    private fun exportImage(
        image: DsuImageState,
        fd: ParcelFileDescriptor,
    ) {
        PrivilegedProvider.run(
            onFail = {
                fd.close()
                onImageOperationError("Privileged service unavailable.")
            },
        ) {
            val error = try {
                exportDsuBackingImage(image.prefix, image.name, fd)
            } finally {
                fd.close()
            }
            if (error.isEmpty()) {
                clearPendingSelection()
                _uiState.update {
                    it.copy(
                        operationState = ImagesOperationState.IDLE,
                        errorText = "",
                    )
                }
            } else {
                onImageOperationError(error)
            }
        }
    }

    private fun addImage(
        prefix: String,
        imageName: String,
        fd: ParcelFileDescriptor,
        imageSize: Long,
    ) {
        PrivilegedProvider.run(
            onFail = {
                fd.close()
                onImageOperationError("Privileged service unavailable.")
            },
        ) {
            val error = try {
                addDsuBackingImage(prefix, imageName, fd, imageSize, true)
            } finally {
                fd.close()
            }
            if (error.isEmpty()) {
                clearPendingSelection()
                refreshImages()
            } else {
                onImageOperationError(error)
            }
        }
    }

    private fun replaceImage(
        image: DsuImageState,
        fd: ParcelFileDescriptor,
        imageSize: Long,
    ) {
        PrivilegedProvider.run(
            onFail = {
                fd.close()
                onImageOperationError("Privileged service unavailable.")
            },
        ) {
            val error = try {
                replaceDsuBackingImage(image.prefix, image.name, fd, imageSize, true)
            } finally {
                fd.close()
            }
            if (error.isEmpty()) {
                clearPendingSelection()
                refreshImages()
            } else {
                onImageOperationError(error)
            }
        }
    }

    fun showDeleteImageSheet(image: DsuImageState) {
        _uiState.update {
            it.copy(
                pendingImage = image,
                sheetDisplay = ImagesSheetDisplayState.DELETE_DSU_IMAGE,
            )
        }
    }

    fun dismissSheet() = dismissSheet(clearPendingSelection = true)

    private fun dismissSheet(clearPendingSelection: Boolean) {
        if (clearPendingSelection) {
            clearPendingSelection()
        }
        _uiState.update {
            it.copy(
                sheetDisplay = ImagesSheetDisplayState.NONE,
            )
        }
    }

    fun confirmDeleteImage() {
        val image = uiState.value.pendingImage ?: return
        dismissSheet()
        _uiState.update {
            it.copy(
                operationState = ImagesOperationState.DELETING,
                currentImageName = image.name,
                errorText = "",
            )
        }
        PrivilegedProvider.run(
            onFail = { onImageOperationError("Privileged service unavailable.") },
        ) {
            val error = deleteDsuBackingImage(image.prefix, image.name)
            if (error.isEmpty()) {
                refreshImages()
            } else {
                onImageOperationError(error)
            }
        }
    }

    private fun onImageOperationError(error: String) {
        clearPendingSelection()
        _uiState.update {
            it.copy(
                operationState = ImagesOperationState.ERROR,
                errorText = error,
                sheetDisplay = ImagesSheetDisplayState.NONE,
            )
        }
    }

    private fun clearPendingSelection() {
        pendingNewImageUri = Uri.EMPTY
        pendingReplacementUri = Uri.EMPTY
        _uiState.update {
            it.copy(
                pendingImage = null,
                pendingPrefix = "",
                newImageName = "",
                newImageNameError = false,
                replacementFileName = "",
            )
        }
    }

    private fun getDefaultImagePrefix(): String {
        return uiState.value.images.firstOrNull()?.prefix
            ?: uiState.value.availablePrefixes.firstOrNull()
            ?: ""
    }

    private fun isNewImageNameValid(imageName: String, prefix: String): Boolean {
        val trimmedImageName = imageName.trim()
        return trimmedImageName.isNotBlank() &&
            IMAGE_NAME_REGEX.matches(trimmedImageName) &&
            uiState.value.images.none {
                it.prefix == prefix && it.name == trimmedImageName
            }
    }

    private fun IPrivilegedService.getInstalledDsuPrefixes(): List<String> {
        val slots = runCatching { getInstalledDsuSlots() }.getOrDefault(emptyList())
        val activeSlot = runCatching { getActiveDsuSlot() }.getOrDefault("")
        val imageDirPrefix = runCatching {
            getInstalledGsiImageDir().toDsuImagePrefix()
        }.getOrNull()

        return buildList {
            if (!imageDirPrefix.isNullOrEmpty()) {
                add(imageDirPrefix)
            }
            slots.forEach { slot ->
                add("$slot/$slot/")
            }
            if (activeSlot.isNotEmpty()) {
                add("$activeSlot/$activeSlot/")
            }
            add("dsu/dsu/")
        }.distinct()
    }

    private fun String.toDsuImagePrefix(): String {
        val prefix = removePrefix("/metadata/gsi/")
            .removePrefix("/data/gsi/")
            .trim('/')
        return if (prefix.isEmpty()) "" else "$prefix/"
    }

    private companion object {
        val IMAGE_NAME_REGEX = Regex("[A-Za-z0-9_.-]+")
    }
}
