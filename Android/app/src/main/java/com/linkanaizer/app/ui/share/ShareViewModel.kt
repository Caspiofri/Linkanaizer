package com.linkanaizer.app.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkanaizer.app.data.dto.CategoryDto
import com.linkanaizer.app.data.repository.CategoryRepository
import com.linkanaizer.app.data.repository.LinkRepository
import com.linkanaizer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SharePhase {
    LOADING_CATEGORIES,
    CATEGORY_PICKER,
    PROCESSING,
    SUCCESS,
    DONE,   // auto-dismiss after success
    ERROR,
}

data class ShareUiState(
    val phase: SharePhase = SharePhase.LOADING_CATEGORIES,
    val categories: List<CategoryDto> = emptyList(),
    val linkName: String = "",
    val savedCategory: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState

    private var pendingUrl: String = ""

    /** Called once from ShareReceiverActivity with the URL from the intent. */
    fun init(url: String) {
        pendingUrl = url
        viewModelScope.launch {
            when (val result = categoryRepository.getCategories()) {
                is Resource.Success -> {
                    _uiState.value = ShareUiState(
                        phase = SharePhase.CATEGORY_PICKER,
                        categories = result.data,
                    )
                }
                is Resource.Error -> {
                    // Can't load categories — skip picker and let AI decide
                    processWithCategory(null)
                }
                is Resource.Loading -> {}
            }
        }
    }

    /** Called when user picks a category (null = let AI choose). */
    fun processWithCategory(categoryHint: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(phase = SharePhase.PROCESSING)
            when (val result = linkRepository.processUrl(pendingUrl, categoryHint)) {
                is Resource.Success -> {
                    _uiState.value = ShareUiState(
                        phase = SharePhase.SUCCESS,
                        linkName = result.data.name,
                        savedCategory = result.data.category,
                    )
                    delay(2200)
                    _uiState.value = _uiState.value.copy(phase = SharePhase.DONE)
                }
                is Resource.Error -> {
                    _uiState.value = ShareUiState(
                        phase = SharePhase.ERROR,
                        errorMessage = result.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    /** Legacy entry-point — kept so the activity can call either init() or this. */
    fun processSharedUrl(url: String) = init(url)
}
