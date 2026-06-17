package com.linkanaizer.app.ui.links

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linkanaizer.app.ui.category.CategoryViewModel
import com.linkanaizer.app.ui.category.EditLinkState
import com.linkanaizer.app.ui.category.EditLinkSheet
import com.linkanaizer.app.ui.components.*
import com.linkanaizer.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllLinksScreen(
    onBackClick: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadLinks("all")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "All My Links", onBackClick = onBackClick)

        var searchQuery by remember { mutableStateOf("") }
        var showNeedsReviewOnly by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" },
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = showNeedsReviewOnly,
                onClick = { showNeedsReviewOnly = !showNeedsReviewOnly },
                label = { Text("Needs Review") },
            )
        }

        if (state.successMessage != null) {
            Text(
                text = state.successMessage!!,
                color = PrimaryLight,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingAnimation()
            }
        } else {
            val baseLinks = state.links
                .let { links ->
                    if (searchQuery.isBlank()) links
                    else links.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.summary.contains(searchQuery, ignoreCase = true) ||
                        it.url.contains(searchQuery, ignoreCase = true)
                    }
                }
                .let { links -> if (showNeedsReviewOnly) links.filter { it.needsReview } else links }

            // Favorites float to the top
            val sortedLinks = baseLinks.sortedWith(compareByDescending { it.isFavorite })

            if (sortedLinks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No links found", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(sortedLinks, key = { it.url }) { link ->
                        SwipeableLinkCard(
                            title = link.name,
                            description = link.summary,
                            url = link.url,
                            thumbnailUrl = link.thumbnail,
                            tags = link.tags,
                            isFavorite = link.isFavorite,
                            isRead = link.isRead,
                            needsReview = link.needsReview,
                            onDelete = { viewModel.requestDeleteLink(link.url) },
                            onLongClick = { viewModel.requestEditLink(link.url) },
                            onFavoriteToggle = { viewModel.toggleFavorite(link.url) },
                            onOpenLink = { url -> viewModel.openLink(url, context) },
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteLink,
            title = { Text("Delete Link") },
            text = { Text("Are you sure you want to delete this link? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDeleteLink,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteLink) { Text("Cancel") }
            },
        )
    }

    // Edit link sheet
    if (state.editLink.show) {
        EditLinkSheet(
            editState = state.editLink,
            onDismiss = viewModel::dismissEditLink,
            onNameChange = viewModel::updateEditName,
            onSummaryChange = viewModel::updateEditSummary,
            onCategoryChange = viewModel::updateEditCategory,
            onAddTag = viewModel::addEditTag,
            onRemoveTag = viewModel::removeEditTag,
            onSave = viewModel::confirmEditLink,
        )
    }
}
