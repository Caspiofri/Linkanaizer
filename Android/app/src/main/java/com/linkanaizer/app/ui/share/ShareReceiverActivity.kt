package com.linkanaizer.app.ui.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.linkanaizer.app.ui.theme.LinkanazerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedUrl = extractUrl(intent)
        if (sharedUrl != null) {
            viewModel.init(sharedUrl)
        } else {
            finish()
            return
        }

        setContent {
            LinkanazerTheme {
                val state by viewModel.uiState.collectAsState()
                ShareBottomSheet(
                    state = state,
                    onDismiss = { finish() },
                    onCategorySelected = { category -> viewModel.processWithCategory(category) },
                )
            }
        }
    }

    private fun extractUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val urlRegex = Regex("https?://[^\\s]+")
        return urlRegex.find(text)?.value
    }
}
