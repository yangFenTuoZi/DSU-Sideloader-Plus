package yangfentuozi.dsusideloaderplus.ui.sdialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.DialogItem
import yangfentuozi.dsusideloaderplus.ui.components.DialogLikeBottomSheet
import yangfentuozi.dsusideloaderplus.ui.components.FileSelectionBox

@Composable
fun ConfirmDSUImageAddSheet(
    prefix: String,
    imageName: String,
    filename: String,
    isImageNameError: Boolean,
    onImageNameChange: (String) -> Unit,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit,
) {
    DialogLikeBottomSheet(
        title = stringResource(id = R.string.add_dsu_image_question),
        icon = Icons.Outlined.Add,
        text = stringResource(id = R.string.add_dsu_image_warning),
        hideKeyboard = false,
        isConfirmEnabled = imageName.isNotBlank() && !isImageNameError,
        content = {
            Spacer(modifier = Modifier.padding(4.dp))
            DialogItem(
                icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
                title = "${stringResource(id = R.string.selected_file)}:",
                text = filename,
                textColor = MaterialTheme.colorScheme.onBackground,
            )
            DialogItem(
                icon = Icons.Outlined.Folder,
                title = "${stringResource(id = R.string.dsu_image_prefix)}:",
                text = prefix,
                textColor = MaterialTheme.colorScheme.onBackground,
            )
            FileSelectionBox(
                modifier = Modifier.padding(top = 10.dp),
                isEnabled = true,
                isError = isImageNameError,
                textFieldTitle = stringResource(id = R.string.dsu_image_name),
                textFieldValue = imageName,
                onValueChange = onImageNameChange,
                supportingText = {
                    if (isImageNameError) {
                        Text(
                            text = stringResource(id = R.string.dsu_image_name_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        },
        confirmText = stringResource(id = R.string.proceed),
        cancelText = stringResource(id = R.string.cancel),
        onClickConfirm = onClickConfirm,
        onClickCancel = onClickCancel,
    )
}
