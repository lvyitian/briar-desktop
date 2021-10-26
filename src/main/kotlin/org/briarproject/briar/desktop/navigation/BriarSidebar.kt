package org.briarproject.briar.desktop.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.briarproject.briar.desktop.contact.ProfileCircle
import org.briarproject.briar.desktop.theme.sidebarSurface
import org.briarproject.briar.desktop.ui.UiMode

val SIDEBAR_WIDTH = 56.dp

@Composable
fun BriarSidebar(
    viewModel: SidebarViewModel,
) {
    LaunchedEffect(true) {
        viewModel.loadAccountInfo()
    }

    Surface(modifier = Modifier.width(SIDEBAR_WIDTH).fillMaxHeight(), color = MaterialTheme.colors.sidebarSurface) {
        Column(verticalArrangement = Arrangement.Top) {
            // profile button
            IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 5.dp, bottom = 4.dp),
                onClick = {}
            ) {
                viewModel.account.value?.let { ProfileCircle(size = 45.dp, it.id.bytes) }
            }

            for (
                (uiMode, icon) in listOf(
                    Pair(UiMode.CONTACTS, Icons.Filled.Contacts),
                    Pair(UiMode.GROUPS, Icons.Filled.Group),
                    Pair(UiMode.FORUMS, Icons.Filled.Forum),
                    Pair(UiMode.BLOGS, Icons.Filled.ChromeReaderMode),
                )
            ) {
                BriarSidebarButton(
                    viewModel.uiMode.value == uiMode,
                    { viewModel.setUiMode(uiMode) },
                    icon,
                    uiMode.toString()
                )
            }
        }
        Column(verticalArrangement = Arrangement.Bottom) {
            for (
                (uiMode, icon) in listOf(
                    Pair(UiMode.TRANSPORTS, Icons.Filled.WifiTethering),
                    Pair(UiMode.SETTINGS, Icons.Filled.Settings),
                    Pair(UiMode.SIGNOUT, Icons.Filled.Logout),
                )
            ) {
                BriarSidebarButton(
                    viewModel.uiMode.value == uiMode,
                    { viewModel.setUiMode(uiMode) },
                    icon,
                    uiMode.toString()
                )
            }
        }
    }
}

@Composable
fun BriarSidebarButton(selected: Boolean, onClick: () -> Unit, icon: ImageVector, contentDescription: String?) {
    val tint = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
    IconButton(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
        onClick = onClick
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(30.dp))
    }
}
