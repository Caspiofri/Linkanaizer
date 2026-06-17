package com.linkanaizer.app.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linkanaizer.app.ui.components.*
import com.linkanaizer.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryName: String,
    categoryEmoji: String,
    onBackClick: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(categoryName) {
        viewModel.loadLinks(categoryName)
    }

    val allTags = remember(state.links) {
        state.links
            .flatMap { it.tags ?: emptyList() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
    }

    val filteredLinks = state.links.filter { link ->
        val matchesSearch = searchQuery.isBlank() ||
            link.name.contains(searchQuery, ignoreCase = true) ||
            link.summary.contains(searchQuery, ignoreCase = true) ||
            link.url.contains(searchQuery, ignoreCase = true)
        val matchesTag = selectedTag == null || (link.tags ?: emptyList()).contains(selectedTag)
        matchesSearch && matchesTag
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "$categoryEmoji $categoryName", onBackClick = onBackClick)

        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" },
                placeholder = "Search in $categoryName...",
            )
        }

        if (allTags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(allTags) { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = if (selectedTag == tag) null else tag },
                        label = { Text(text = tag, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryDark,
                            containerColor = CategoryCardBg,
                            labelColor = TextSecondary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = BorderLight,
                            selectedBorderColor = GradientStart,
                            enabled = true,
                            selected = selectedTag == tag,
                        ),
                    )
                }
            }
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
        } else if (state.links.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No links in this category yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
            }
        } else if (filteredLinks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedTag != null) "No links with tag \"$selectedTag\""
                           else "No links match \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(filteredLinks, key = { it.url }) { link ->
                    SwipeableLinkCard(
                        title = link.name,
                        description = link.summary,
                        url = link.url,
                        thumbnailUrl = link.thumbnail,
                        tags = link.tags,
                        isFavorite = link.isFavorite,
                        isRead = link.isRead,
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

    // Edit link bottom sheet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLinkSheet(
    editState: EditLinkState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit = {},
    onSummaryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSave: () -> Unit,
) {
    var newTagInput by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Edit link",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            // Title
            OutlinedTextField(
                value = editState.editedName,
                onValueChange = onNameChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    focusedLabelColor = GradientStart,
                ),
            )

            // Summary
            OutlinedTextField(
                value = editState.editedSummary,
                onValueChange = onSummaryChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    focusedLabelColor = GradientStart,
                ),
            )

            // Tags
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    editState.editedTags.forEach { tag ->
                        InputChip(
                            selected = true,
                            onClick = { onRemoveTag(tag) },
                            label = { Text(tag, fontSize = 12.sp) },
                            trailingIcon = {
                                Text("×", fontSize = 14.sp, color = TextSecondary)
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = GradientStart.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryDark,
                            ),
                        )
                    }
                }
                if (editState.editedTags.size < 3) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = newTagInput,
                            onValueChange = { newTagInput = it },
                            placeholder = { Text("Add tag", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newTagInput.isNotBlank()) {
                                    onAddTag(newTagInput)
                                    newTagInput = ""
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                            ),
                        )
                        IconButton(
                            onClick = {
                                if (newTagInput.isNotBlank()) {
                                    onAddTag(newTagInput)
                                    newTagInput = ""
                                }
                            },
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add tag", tint = GradientStart)
                        }
                    }
                }
            }

            // Category
            if (editState.availableCategories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(editState.availableCategories) { cat ->
                            val selected = cat.category == editState.selectedCategory
                            FilterChip(
                                selected = selected,
                                onClick = { onCategoryChange(cat.category) },
                                label = { Text("${cat.emoji} ${cat.category.replaceFirstChar { it.uppercase() }}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryDark,
                                    containerColor = CategoryCardBg,
                                    labelColor = TextSecondary,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = BorderLight,
                                    selectedBorderColor = GradientStart,
                                    enabled = true,
                                    selected = selected,
                                ),
                            )
                        }
                    }
                }
            }

            // Save button
            Button(
                onClick = onSave,
                enabled = !editState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
            ) {
                if (editState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Save changes", color = Color.White)
                }
            }
        }
    }
}
