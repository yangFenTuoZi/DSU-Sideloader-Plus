package yangfentuozi.dsusideloaderplus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(0.dp),
    columnContent: Boolean = true,
    enableDefaultScrollBehavior: Boolean = true,
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    outsideContent: @Composable (PaddingValues) -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        state = rememberTopAppBarState(),
    )
    val layoutDirection = LocalLayoutDirection.current

    val scrollBehaviorModifier =
        if (enableDefaultScrollBehavior) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier

    val insets = WindowInsets
        .systemBars
        .only(WindowInsetsSides.Vertical)
        .asPaddingValues()

    Surface {
        Scaffold(
            modifier = scrollBehaviorModifier
                .fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = { topBar(scrollBehavior) },
            bottomBar = { bottomBar() },
            content = { innerPadding ->
                val scrollModifier =
                    if (enableDefaultScrollBehavior) Modifier.verticalScroll(rememberScrollState()) else Modifier
                val contentInsetsModifier = Modifier.padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                )
                val bottomInset = innerPadding.calculateBottomPadding()
                if (columnContent) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(modifier)
                            .then(contentInsetsModifier)
                            .then(scrollModifier),
                        verticalArrangement = verticalArrangement,
                    ) {
                        content()
                        Spacer(modifier = Modifier.height(bottomInset))
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(modifier)
                            .then(contentInsetsModifier)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            content()
                        }
                    }
                }
            },
        )
    }

    outsideContent(insets)
}
