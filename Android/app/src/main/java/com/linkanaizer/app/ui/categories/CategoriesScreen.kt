package com.linkanaizer.app.ui.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linkanaizer.app.ui.components.*
import com.linkanaizer.app.ui.theme.*

@Composable
fun CategoriesScreen(
    onBackClick: () -> Unit,
    onCategoryClick: ((String, String) -> Unit)? = null,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }
    var showAddField by remember { mutableStateOf(false) }

    val filteredCategories = if (state.searchQuery.isBlank()) {
        state.categories
    } else {
        state.categories.filter {
            it.category.contains(state.searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "Categories", onBackClick = onBackClick)

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onClear = { viewModel.updateSearchQuery("") },
                placeholder = "Search categories...",
            )
        }

        // Add new category section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showAddField) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("New category name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedBorderColor = GradientStart,
                        unfocusedBorderColor = BorderLight,
                        focusedLabelColor = TextPrimary,
                        unfocusedLabelColor = TextTertiary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                GradientButton(
                    text = if (state.isCreating) "..." else "Create",
                    onClick = {
                        if (newCategoryName.isNotBlank() && !state.isCreating) {
                            viewModel.createCategory(newCategoryName)
                            newCategoryName = ""
                            showAddField = false
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    height = 48.dp,
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddField = true },
                    shape = RoundedCornerShape(10.dp),
                    color = CategoryCardBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add category",
                            tint = PrimaryLight,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Add New Category",
                            color = PrimaryLight,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        // Success/Error messages
        if (state.successMessage != null) {
            Text(
                text = state.successMessage!!,
                color = PrimaryLight,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Category list
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingAnimation()
            }
        } else if (filteredCategories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (state.searchQuery.isNotBlank()) "No categories match \"${state.searchQuery}\""
                           else "No categories yet. Create one above!",
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
                items(filteredCategories) { category ->
                    CategoryCardWithActions(
                        emoji = category.emoji,
                        title = category.category,
                        count = category.count,
                        isVisible = category.visible,
                        onClick = {
                            onCategoryClick?.invoke(category.category, category.emoji)
                        },
                        onToggleVisibility = { viewModel.toggleVisibility(category.category, category.visible) },
                        onDelete = { viewModel.requestDelete(category.category) },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Delete Category") },
            text = {
                Text("This will permanently delete \"${state.categoryToDelete}\" and all its links. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun CategoryCardWithActions(
    emoji: String,
    title: String,
    count: Int,
    isVisible: Boolean,
    onClick: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(0.5.dp, BorderLight),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "$count Links",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                )
            }
            Checkbox(
                checked = isVisible,
                onCheckedChange = { onToggleVisibility() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryLight,
                    uncheckedColor = BorderLight,
                ),
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
