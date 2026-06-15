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
import yangfentuozi.dsusideloaderplus.ui.screen.home.UserDataCardState

@Composable
fun UserdataCard(
    isEnabled: Boolean,
    uiState: UserDataCardState,
    isDsuInstalled: Boolean,
    onValueChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit = {},
    onPreserveCheckedChange: (Boolean) -> Unit = {},
) {
    // When DSU is installed and not currently installing, show simplified preserve UI
    // Note: isEnabled parameter indicates installation is in progress (when true)
    // So !isEnabled means installation is NOT in progress
    if (isDsuInstalled && !isEnabled) {
        // When DSU is installed, show a simpler card with preserve option
        ExpandableSettingsSwitchItem(
            title = stringResource(id = R.string.preserve_userdata),
            summary = stringResource(id = R.string.preserve_userdata_desc),
            checked = uiState.preserveSelected,
            onCheckedChange = onPreserveCheckedChange,
            expanded = !uiState.preserveSelected,
            onExpandedChange = { expanded -> onPreserveCheckedChange(!expanded) },
        ) {
            Column(modifier = Modifier.padding(horizontal = 36.dp)) {
                UserdataSizeInput(
                    isEnabled = !isEnabled,
                    uiState = uiState,
                    onValueChange = onValueChange,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        }
    } else {
        // Normal userdata card when DSU is not installed
        ExpandableSettingsSwitchItem(
            title = stringResource(id = R.string.userdata_size),
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
                UserdataSizeInput(
                    isEnabled = !isEnabled,
                    uiState = uiState,
                    onValueChange = onValueChange,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun UserdataSizeInput(
    isEnabled: Boolean,
    uiState: UserDataCardState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FileSelectionBox(
        modifier = modifier,
        isEnabled = isEnabled,
        isError = uiState.isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textFieldValue = uiState.text,
        textFieldTitle = stringResource(id = R.string.userdata_size_info),
        onValueChange = onValueChange,
    )
    if (uiState.isError) {
        Text(
            modifier = Modifier.padding(start = 1.dp),
            text = stringResource(
                id = R.string.allowed_userdata_allocation,
                uiState.maximumAllowed,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
