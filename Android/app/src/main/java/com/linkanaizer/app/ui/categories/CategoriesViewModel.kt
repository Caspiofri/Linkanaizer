package com.linkanaizer.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkanaizer.app.data.dto.CategoryDto
import com.linkanaizer.app.data.repository.CategoryRepository
import com.linkanaizer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<CategoryDto> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isCreating: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val categoryToDelete: String? = null,
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState

    private var hasTriedEmojiFixThisSession = false

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = categoryRepository.getCategories()) {
                is Resource.Success -> {
                    val categories = result.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false,
                    )
                    // Auto-fix pin emojis only once per session to avoid rate limits
                    if (!hasTriedEmojiFixThisSession) {
                        val hasPinEmojis = categories.any { it.emoji == "📌" }
                        if (hasPinEmojis) {
                            hasTriedEmojiFixThisSession = true
                            launch {
                                categoryRepository.fixEmojis()
                                val refreshed = categoryRepository.getCategories()
                                if (refreshed is Resource.Success) {
                                    _uiState.value = _uiState.value.copy(
                                        categories = refreshed.data ?: emptyList()
                                    )
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
                is Resource.Loading -> { /* handled by isLoading state */ }
            }
        }
    }

    fun createCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, successMessage = null, errorMessage = null)
            when (val result = categoryRepository.insertCategory(name)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        successMessage = "Category created!",
                    )
                    loadCategories()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = result.message,
                    )
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    fun toggleVisibility(categoryName: String, currentVisibility: Boolean) {
        viewModelScope.launch {
            categoryRepository.changeVisibility(categoryName, !currentVisibility)
            loadCategories()
        }
    }

    fun requestDelete(categoryName: String) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            categoryToDelete = categoryName,
        )
    }

    fun confirmDelete() {
        val name = _uiState.value.categoryToDelete ?: return
        _uiState.value = _uiState.value.copy(showDeleteDialog = false, categoryToDelete = null)
        viewModelScope.launch {
            categoryRepository.deleteCategory(name)
            loadCategories()
        }
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false, categoryToDelete = null)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
