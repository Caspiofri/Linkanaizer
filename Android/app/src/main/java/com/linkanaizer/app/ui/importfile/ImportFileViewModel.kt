package com.linkanaizer.app.ui.importfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkanaizer.app.data.repository.LinkRepository
import com.linkanaizer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportFileUiState(
    val fileName: String? = null,
    val fileContent: String? = null,
    val linksFound: Int = 0,
    val isImporting: Boolean = false,
    val processed: Int = 0,
    val skipped: Int = 0,
    val errors: Int = 0,
    val total: Int = 0,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ImportFileViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportFileUiState())
    val uiState: StateFlow<ImportFileUiState> = _uiState

    private var importJob: Job? = null
    private var pollingJob: Job? = null

    fun parseFileContent(content: String, fileName: String) {
        val regex = Regex("""(\d{1,2}\.\d{1,2}\.\d{4}), (\d{1,2}:\d{2}) - .*?(https?://\S+)""")
        val matches = regex.findAll(content).count()

        _uiState.value = _uiState.value.copy(
            fileName = fileName,
            fileContent = content,
            linksFound = matches,
            successMessage = null,
            errorMessage = null,
        )
    }

    fun importLinks() {
        val content = _uiState.value.fileContent ?: return
        importJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                errorMessage = null,
                successMessage = null,
                processed = 0,
                skipped = 0,
                errors = 0,
                total = _uiState.value.linksFound,
            )
            when (val result = linkRepository.importText(content)) {
                is Resource.Success -> {
                    // Import kicked off on server — start polling for progress
                    startPolling()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        errorMessage = result.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(2000) // Poll every 2 seconds
                when (val result = linkRepository.getImportProgress()) {
                    is Resource.Success -> {
                        val progress = result.data
                        _uiState.value = _uiState.value.copy(
                            processed = progress.processed,
                            skipped = progress.skipped,
                            errors = progress.errors,
                            total = if (progress.total > 0) progress.total else _uiState.value.total,
                        )
                        when (progress.status) {
                            "done" -> {
                                _uiState.value = _uiState.value.copy(
                                    isImporting = false,
                                    successMessage = "Done! ${progress.processed} imported, ${progress.skipped} skipped, ${progress.errors} errors",
                                )
                                return@launch
                            }
                            "cancelled" -> {
                                _uiState.value = _uiState.value.copy(
                                    isImporting = false,
                                    errorMessage = "Import stopped. ${progress.processed} links were imported before cancellation.",
                                )
                                return@launch
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Keep polling even if one request fails
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun cancelImport() {
        viewModelScope.launch {
            // Tell the server to stop processing
            linkRepository.cancelImport()
        }
        // UI will update when polling picks up the "cancelled" status
    }

    fun reset() {
        importJob?.cancel()
        pollingJob?.cancel()
        importJob = null
        pollingJob = null
        _uiState.value = ImportFileUiState()
    }
}
