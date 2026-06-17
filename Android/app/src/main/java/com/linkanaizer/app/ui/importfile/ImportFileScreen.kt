package com.linkanaizer.app.ui.importfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.linkanaizer.app.R
import com.linkanaizer.app.ui.components.*
import com.linkanaizer.app.ui.theme.*

@Composable
fun ImportFileScreen(
    onBackClick: () -> Unit,
    viewModel: ImportFileViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                val fileName = uri.lastPathSegment ?: "Unknown file"
                viewModel.parseFileContent(content, fileName)
            } catch (e: Exception) {
                // Silently handle — ViewModel will show error state
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "Import File", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Illustration
            Image(
                painter = painterResource(R.drawable.ill_import_file),
                contentDescription = "Import file illustration",
                modifier = Modifier.size(160.dp),
                alpha = 0.7f,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Import links from a text file",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Supports WhatsApp chat exports and plain text files with URLs",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            // No file selected yet — show picker button
            if (state.fileName == null && state.successMessage == null) {
                GradientButton(
                    text = "Choose File",
                    onClick = {
                        filePickerLauncher.launch(arrayOf("text/*"))
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }

            // File selected — show preview
            if (state.fileName != null && state.successMessage == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CategoryCardBg,
                    tonalElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.fileName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Found ${state.linksFound} links",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.linksFound > 0) PrimaryLight else TextSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (state.isImporting) {
                    val total = state.total.coerceAtLeast(1)
                    val done = state.processed + state.skipped + state.errors
                    val progress = done.toFloat() / total.toFloat()

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = PrimaryLight,
                        trackColor = BorderLight,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Processing $done of $total links...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                    )

                    if (state.processed > 0 || state.skipped > 0) {
                        Text(
                            text = "${state.processed} imported, ${state.skipped} skipped",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { viewModel.cancelImport() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Stop Import")
                    }
                } else if (state.linksFound > 0) {
                    GradientButton(
                        text = "Import ${state.linksFound} Links",
                        onClick = { viewModel.importLinks() },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    )

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = { viewModel.reset() }) {
                        Text("Choose a different file", color = PrimaryLight)
                    }
                } else {
                    Text(
                        text = "No links found in this file. Try a WhatsApp chat export.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = { viewModel.reset() }) {
                        Text("Choose a different file", color = PrimaryLight)
                    }
                }
            }

            // Success state
            if (state.successMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CategoryCardBg,
                    tonalElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.successMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryLight,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Links will appear in your library as they are processed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                GradientButton(
                    text = "Import Another File",
                    onClick = { viewModel.reset() },
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }

            // Error state
            if (state.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
