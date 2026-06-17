package com.linkanaizer.app.ui.category

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkanaizer.app.data.dto.CategoryDto
import com.linkanaizer.app.data.dto.LinkDto
import com.linkanaizer.app.data.repository.CategoryRepository
import com.linkanaizer.app.data.repository.LinkRepository
import com.linkanaizer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditLinkState(
    val show: Boolean = false,
    val url: String = "",
    val editedName: String = "",
    val editedSummary: String = "",
    val editedTags: List<String> = emptyList(),
    val selectedCategory: String = "",
    val availableCategories: List<CategoryDto> = emptyList(),
    val isSaving: Boolean = false,
)

data class CategoryUiState(
    val links: List<LinkDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Delete
    val linkToDelete: String? = null,
    val showDeleteDialog: Boolean = false,
    // Edit (covers move + edit summary/tags)
    val editLink: EditLinkState = EditLinkState(),
    val successMessage: String? = null,
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState

    private var currentCategoryName: String = ""

    fun loadLinks(categoryName: String) {
        currentCategoryName = categoryName
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, successMessage = null)
            when (val result = linkRepository.getLinksByCategory(categoryName)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        links = result.data,
                        isLoading = false,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Open link (marks as read) ──

    fun openLink(url: String, context: Context) {
        // Mark read locally immediately (no flicker)
        _uiState.value = _uiState.value.copy(
            links = _uiState.value.links.map { if (it.url == url) it.copy(isRead = true) else it },
        )
        // Fire-and-forget API call
        viewModelScope.launch { linkRepository.markRead(url) }
        // Open in browser
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    // ── Favorites ──

    fun toggleFavorite(url: String) {
        viewModelScope.launch {
            when (val result = linkRepository.toggleFavorite(url)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        links = _uiState.value.links.map { link ->
                            if (link.url == url) link.copy(isFavorite = result.data.isFavorite) else link
                        },
                    )
                }
                else -> {}
            }
        }
    }

    // ── Delete ──

    fun requestDeleteLink(url: String) {
        _uiState.value = _uiState.value.copy(linkToDelete = url, showDeleteDialog = true)
    }

    fun dismissDeleteLink() {
        _uiState.value = _uiState.value.copy(linkToDelete = null, showDeleteDialog = false)
    }

    fun confirmDeleteLink() {
        val url = _uiState.value.linkToDelete ?: return
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
        viewModelScope.launch {
            when (val result = linkRepository.deleteLink(url)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        links = _uiState.value.links.filter { it.url != url },
                        linkToDelete = null,
                        successMessage = "Link deleted",
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        linkToDelete = null,
                        errorMessage = result.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Edit (move + edit summary/tags) ──

    fun requestEditLink(url: String) {
        val link = _uiState.value.links.find { it.url == url } ?: return
        viewModelScope.launch {
            val cats = when (val r = categoryRepository.getCategories()) {
                is Resource.Success -> r.data
                else -> emptyList()
            }
            _uiState.value = _uiState.value.copy(
                editLink = EditLinkState(
                    show = true,
                    url = url,
                    editedName = link.name,
                    editedSummary = link.summary,
                    editedTags = link.tags ?: emptyList(),
                    selectedCategory = link.category,
                    availableCategories = cats,
                ),
            )
        }
    }

    fun updateEditName(name: String) {
        _uiState.value = _uiState.value.copy(
            editLink = _uiState.value.editLink.copy(editedName = name),
        )
    }

    fun updateEditSummary(summary: String) {
        _uiState.value = _uiState.value.copy(
            editLink = _uiState.value.editLink.copy(editedSummary = summary),
        )
    }

    fun updateEditCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            editLink = _uiState.value.editLink.copy(selectedCategory = category),
        )
    }

    fun addEditTag(tag: String) {
        val current = _uiState.value.editLink.editedTags
        if (tag.isBlank() || current.size >= 3 || current.any { it.equals(tag, ignoreCase = true) }) return
        _uiState.value = _uiState.value.copy(
            editLink = _uiState.value.editLink.copy(editedTags = current + tag.trim().lowercase()),
        )
    }

    fun removeEditTag(tag: String) {
        _uiState.value = _uiState.value.copy(
            editLink = _uiState.value.editLink.copy(
                editedTags = _uiState.value.editLink.editedTags.filter { it != tag },
            ),
        )
    }

    fun dismissEditLink() {
        _uiState.value = _uiState.value.copy(editLink = EditLinkState())
    }

    fun confirmEditLink() {
        val edit = _uiState.value.editLink
        val url = edit.url.ifBlank { return }
        _uiState.value = _uiState.value.copy(editLink = edit.copy(isSaving = true))

        val link = _uiState.value.links.find { it.url == url } ?: return
        val categoryChanged = edit.selectedCategory != link.category
        val newCategoryArg = if (categoryChanged) edit.selectedCategory else null

        viewModelScope.launch {
            when (val result = linkRepository.updateLink(
                url = url,
                name = edit.editedName.ifBlank { null },
                summary = edit.editedSummary,
                tags = edit.editedTags,
                newCategory = newCategoryArg,
            )) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        links = _uiState.value.links.mapNotNull { l ->
                            if (l.url != url) return@mapNotNull l
                            if (categoryChanged && currentCategoryName != "all") null
                            else l.copy(
                                name = edit.editedName.ifBlank { l.name },
                                summary = edit.editedSummary,
                                tags = edit.editedTags,
                                category = edit.selectedCategory,
                            )
                        },
                        editLink = EditLinkState(),
                        successMessage = "Link updated",
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        editLink = edit.copy(isSaving = false),
                        errorMessage = result.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    // Keep old move API for backward compat (used by AllLinksScreen)
    val showMoveSheet: Boolean get() = false
    val availableCategories: List<CategoryDto> get() = emptyList()
    fun requestMoveLink(url: String) = requestEditLink(url)
    fun dismissMoveSheet() = dismissEditLink()
    fun confirmMoveLink(newCategory: String) {
        updateEditCategory(newCategory)
        confirmEditLink()
    }
}
