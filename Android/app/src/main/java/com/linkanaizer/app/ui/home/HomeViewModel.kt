package com.linkanaizer.app.ui.home

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

data class HomeUiState(
    val categories: List<CategoryDto> = emptyList(),
    val recentLinks: List<LinkDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Load categories
            when (val catResult = categoryRepository.getCategories()) {
                is Resource.Success -> {
                    val visibleCategories = catResult.data
                        .filter { it.visible }
                        .sortedByDescending { it.count }
                    _uiState.value = _uiState.value.copy(categories = visibleCategories)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = catResult.message)
                }
                is Resource.Loading -> {}
            }

            // Load recent links (all)
            when (val linksResult = linkRepository.getLinksByCategory("all")) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        recentLinks = linksResult.data.take(5),
                        isLoading = false,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = linksResult.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}
