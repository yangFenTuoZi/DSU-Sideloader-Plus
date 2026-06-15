package yangfentuozi.dsusideloaderplus.ui.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.R
import yangfentuozi.dsusideloaderplus.ui.components.ExpandableSettingsSwitchItem
import yangfentuozi.dsusideloaderplus.ui.components.FileSelectionBox
import yangfentuozi.dsusideloaderplus.ui.screen.home.ImageSizeCardState

@Composable
fun ImageSizeCard(
    isEnabled: Boolean,
    uiState: ImageSizeCardState,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    ExpandableSettingsSwitchItem(
        title = stringResource(id = R.string.image_size),
        checked = uiState.isSelected,
        onCheckedChange = onCheckedChange,
        expanded = uiState.isSelected,
        onExpandedChange = { expanded ->
            if (expanded != uiState.isSelected) {
                onCheckedChange(expanded)
            }
        },
        enabled = !isEnabled,
    ) {
        Column(modifier = Modifier.padding(horizontal = 36.dp)) {
            FileSelectionBox(
                modifier = Modifier.padding(bottom = 20.dp),
                isEnabled = !isEnabled,
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textFieldValue = uiState.text,
                textFieldTitle = stringResource(id = R.string.image_size_info),
                onValueChange = onValueChange,
                supportingText = {
                    Text(
                        text = stringResource(id = R.string.not_recommended_option),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            )
        }
    }
}
