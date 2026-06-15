package yangfentuozi.dsusideloaderplus.ui.cards.warnings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import yangfentuozi.dsusideloaderplus.R

@Composable
fun GrantingPermissionCard() {
    WarningCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(id = R.string.missing_permission),
        text = stringResource(id = R.string.granting_permission),
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
        )
    }
}
