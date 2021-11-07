package org.briarproject.briar.desktop.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun PlaceHolderSettingsView(isDark: Boolean, setDark: (Boolean) -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Box {
            Button(onClick = { setDark(!isDark) }) {
                Text(i18n("settings.theme"))
            }
        }
    }
}
