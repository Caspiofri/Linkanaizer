package com.linkanaizer.app.ui.insert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkanaizer.app.data.repository.LinkRepository
import com.linkanaizer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsertLinkUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class InsertLinkViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsertLinkUiState())
    val uiState: StateFlow<InsertLinkUiState> = _uiState

    fun processUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = InsertLinkUiState(isLoading = true)
            when (val result = linkRepository.processUrl(url)) {
                is Resource.Success -> {
                    _uiState.value = InsertLinkUiState(
                        successMessage = "Saved as \"${result.data.name}\" in ${result.data.category}",
                    )
                }
                is Resource.Error -> {
                    _uiState.value = InsertLinkUiState(errorMessage = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
