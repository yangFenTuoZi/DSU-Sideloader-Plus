package yangfentuozi.dsusideloaderplus.ui.screen.libraries

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.ApplicationScreen
import yangfentuozi.dsusideloaderplus.ui.components.LazySplicedColumnGroup
import yangfentuozi.dsusideloaderplus.ui.components.SettingsItem
import yangfentuozi.dsusideloaderplus.ui.components.TopBar
import yangfentuozi.dsusideloaderplus.ui.screen.Destinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    navigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val libraries = remember(context) {
        Libs.Builder().withContext(context).build().libraries
    }
    val bottomPadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 26.dp

    ApplicationScreen(
        enableDefaultScrollBehavior = false,
        columnContent = false,
        modifier = Modifier.padding(horizontal = 16.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.libraries_title),
                scrollBehavior = it,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        LazySplicedColumnGroup(
            items = libraries,
            modifier = Modifier.fillMaxSize(),
            key = { it.uniqueId },
            contentPadding = PaddingValues(bottom = bottomPadding),
        ) { library ->
            val urlToOpen = library.website.orEmpty()
            SettingsItem(
                title = library.name,
                summary = library.licenses.joinToString { it.name },
                onClick = if (urlToOpen.isNotEmpty()) {
                    { uriHandler.openUri(urlToOpen) }
                } else {
                    null
                },
            )
        }
    }
}
