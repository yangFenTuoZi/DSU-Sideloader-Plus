package yangfentuozi.dsusideloaderplus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun <T> LaunchedEffectAfterFirst(
    key: T,
    block: suspend (T) -> Unit
) {
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        if (!initialized) {
            initialized = true
            return@LaunchedEffect
        }
        block(key)
    }
}
