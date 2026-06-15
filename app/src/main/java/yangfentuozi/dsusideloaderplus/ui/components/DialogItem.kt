package yangfentuozi.dsusideloaderplus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DialogItem(
    icon: ImageVector,
    title: String,
    text: String,
    textColor: Color = Color.Unspecified,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .size(32.dp)
                .padding(end = 6.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            imageVector = icon,
            contentDescription = "Icon",
        )
        Column(Modifier.padding(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = if (textColor == Color.Unspecified) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    textColor
                },
            )
            Text(
                color = textColor,
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
