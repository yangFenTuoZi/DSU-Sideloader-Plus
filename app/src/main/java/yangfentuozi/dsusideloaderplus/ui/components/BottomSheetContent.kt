package yangfentuozi.dsusideloaderplus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetContent(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface,
            shape = CircleShape,
            modifier = Modifier
                .alpha(0.1F)
                .height(6.dp)
                .width(32.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            content = {},
        )
        Spacer(modifier = Modifier.height(12.dp))
        Icon(
            tint = MaterialTheme.colorScheme.onSurface,
            imageVector = icon,
            contentDescription = "icon",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(12.dp),
        )
        Text(
            color = MaterialTheme.colorScheme.onSurface,
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
