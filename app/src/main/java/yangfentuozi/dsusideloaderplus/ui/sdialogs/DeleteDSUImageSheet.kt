package yangfentuozi.dsusideloaderplus.ui.sdialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.DialogLikeBottomSheet

@Composable
fun DeleteDSUImageSheet(
    imageName: String,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit,
) {
    DialogLikeBottomSheet(
        title = stringResource(id = R.string.delete_dsu_image_question),
        icon = Icons.Outlined.DeleteForever,
        text = stringResource(id = R.string.delete_dsu_image_warning, imageName),
        confirmText = stringResource(id = R.string.yes),
        cancelText = stringResource(id = R.string.cancel),
        onClickConfirm = onClickConfirm,
        onClickCancel = onClickCancel,
    )
}
