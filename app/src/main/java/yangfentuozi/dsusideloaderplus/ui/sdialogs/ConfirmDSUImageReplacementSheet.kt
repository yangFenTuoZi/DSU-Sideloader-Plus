package yangfentuozi.dsusideloaderplus.ui.sdialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.DialogItem
import yangfentuozi.dsusideloaderplus.ui.components.DialogLikeBottomSheet

@Composable
fun ConfirmDSUImageReplacementSheet(
    imageName: String,
    filename: String,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit,
) {
    DialogLikeBottomSheet(
        title = stringResource(id = R.string.replace_dsu_image_question),
        icon = Icons.Outlined.InstallMobile,
        text = stringResource(id = R.string.replace_dsu_image_warning),
        content = {
            Spacer(modifier = Modifier.padding(4.dp))
            DialogItem(
                icon = Icons.Outlined.Storage,
                title = "${stringResource(id = R.string.installed_dsu_image)}:",
                text = imageName,
                textColor = MaterialTheme.colorScheme.onBackground,
            )
            DialogItem(
                icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
                title = "${stringResource(id = R.string.selected_file)}:",
                text = filename,
                textColor = MaterialTheme.colorScheme.onBackground,
            )
        },
        confirmText = stringResource(id = R.string.proceed),
        cancelText = stringResource(id = R.string.cancel),
        onClickConfirm = onClickConfirm,
        onClickCancel = onClickCancel,
    )
}
