package com.linkanaizer.app.ui.insert

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linkanaizer.app.R
import com.linkanaizer.app.ui.components.*
import com.linkanaizer.app.ui.theme.*

@Composable
fun InsertLinkScreen(
    onBackClick: () -> Unit,
    viewModel: InsertLinkViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "Insert Link", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var urlText by remember { mutableStateOf("") }

            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Paste your link here") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(24.dp))

            if (state.isLoading) {
                LoadingAnimation()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "AI is analyzing your link...",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                GradientButton(
                    text = "Save Link",
                    onClick = {
                        if (urlText.isNotBlank()) {
                            viewModel.processUrl(urlText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }

            if (state.successMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.successMessage!!,
                    color = PrimaryLight,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(32.dp))

            Image(
                painter = painterResource(R.drawable.ill_import_link),
                contentDescription = "Insert link illustration",
                modifier = Modifier.size(180.dp),
                alpha = 0.6f,
            )
        }
    }
}
