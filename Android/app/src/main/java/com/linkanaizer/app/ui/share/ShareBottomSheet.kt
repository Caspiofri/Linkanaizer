package com.linkanaizer.app.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkanaizer.app.ui.components.LoadingAnimation
import com.linkanaizer.app.ui.theme.*

@Composable
fun ShareBottomSheet(
    state: ShareUiState,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite.copy(alpha = 0.3f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = SurfaceWhite,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderLight),
                )
                Spacer(Modifier.height(20.dp))

                when (state.phase) {
                    SharePhase.LOADING_CATEGORIES -> {
                        LoadingAnimation()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Loading your categories...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    SharePhase.CATEGORY_PICKER -> {
                        Text(
                            text = "Save to...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                        )
                        Spacer(Modifier.height(16.dp))

                        // Category grid
                        if (state.categories.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                userScrollEnabled = true,
                            ) {
                                items(state.categories) { cat ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = CategoryCardBg,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onCategorySelected(cat.category) },
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(text = cat.emoji, fontSize = 24.sp)
                                            Text(
                                                text = cat.category.replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center,
                                                color = TextPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // "Let AI choose" button
                        OutlinedButton(
                            onClick = { onCategorySelected(null) },
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GradientStart),
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GradientStart,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Let AI choose", color = GradientStart)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    SharePhase.PROCESSING -> {
                        LoadingAnimation()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Saving your link...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PrimaryDark,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "AI is reading and organizing it for you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    SharePhase.SUCCESS -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = PrimaryLight,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Saved!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PrimaryDark,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "\"${state.linkName}\" → ${state.savedCategory}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    SharePhase.DONE -> {
                        // Auto-triggered after SUCCESS delay — just close
                        onDismiss()
                    }

                    SharePhase.ERROR -> {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Something went wrong",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = onDismiss) { Text("Close") }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
