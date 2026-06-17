package com.linkanaizer.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linkanaizer.app.ui.components.GradientButton
import com.linkanaizer.app.ui.components.TopBar
import com.linkanaizer.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "Settings", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Linkanaizer",
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(48.dp))

            GradientButton(
                text = "Log Out",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.8f),
            )
        }
    }
}
