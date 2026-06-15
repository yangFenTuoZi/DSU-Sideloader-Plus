package yangfentuozi.dsusideloaderplus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.ui.theme.AppShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.(hideSheet: suspend () -> Unit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val shouldCallOnDismiss = remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = {
            if (shouldCallOnDismiss.value) onDismiss()
        },
        sheetState = sheetState,
        modifier = modifier,
        shape = AppShape.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        BottomSheetContent(
            title = title,
            icon = icon,
        ) {
            val insets = WindowInsets
                .systemBars
                .only(WindowInsetsSides.Vertical)
                .asPaddingValues()
            Column(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(
                        end = 16.dp,
                        start = 16.dp,
                        bottom = insets.calculateBottomPadding() + 14.dp,
                        top = 14.dp,
                    ),
            ) {
                // Shortcut used to hide sheet by event
                content {
                    shouldCallOnDismiss.value = false
                    sheetState.hide()
                }
            }
        }
    }
}
