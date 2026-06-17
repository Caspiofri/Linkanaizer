package com.linkanaizer.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linkanaizer.app.R
import com.linkanaizer.app.ui.components.*
import com.linkanaizer.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    onCategoryClick: (String, String) -> Unit,
    onAllLinksClick: () -> Unit,
    onInsertLinkClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onImportFileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Reload data every time the screen becomes visible (e.g., navigating back)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadData()
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.loadData() },
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header with greeting + illustration + settings
        item {
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                // Settings gear in top-right
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                    )
                }
                // Greeting + illustration below
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )
                        Text(
                            text = "${userName.ifEmpty { "there" }}!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Image(
                        painter = painterResource(R.drawable.ill_home),
                        contentDescription = "Home illustration",
                        modifier = Modifier.size(180.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Categories section
        if (state.categories.isNotEmpty()) {
            item {
                Text(
                    text = "Your Top Categories:",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                )
            }

            items(state.categories.take(6)) { category ->
                CategoryCard(
                    emoji = category.emoji,
                    title = category.category,
                    count = category.count,
                    onClick = { onCategoryClick(category.category, category.emoji) },
                )
                Spacer(Modifier.height(8.dp))
            }

            // All My Links link
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAllLinksClick)
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "All My Links",
                        fontSize = 13.sp,
                        color = TextTertiary,
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View all links",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Action buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ActionButton(
                    label = "Categories",
                    icon = Icons.Default.Category,
                    onClick = onCategoriesClick,
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    label = "Import File",
                    icon = Icons.Default.FileUpload,
                    onClick = onImportFileClick,
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    label = "Insert Link",
                    icon = Icons.Default.Link,
                    onClick = onInsertLinkClick,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // Recent links section
        if (state.recentLinks.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Links",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                )
            }

            items(state.recentLinks) { link ->
                LinkCard(
                    title = link.name,
                    description = link.summary,
                    url = link.url,
                    thumbnailUrl = link.thumbnail,
                    tags = link.tags,
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // Loading
        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingAnimation()
                }
            }
        }

        // Error
        if (state.errorMessage != null) {
            item {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        // Empty state
        if (!state.isLoading && state.categories.isEmpty() && state.recentLinks.isEmpty() && state.errorMessage == null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ill_welcome),
                        contentDescription = "Empty library",
                        modifier = Modifier.size(200.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Your link library is empty",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Save your first link using the button above\nor share a link from any app!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
    } // end PullToRefreshBox
}
