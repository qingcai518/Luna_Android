package jp.co.studio.kaka.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.model.SearchResult
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.SearchRepository
import jp.co.studio.kaka.download.DownloadStateHolder
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.Constants
import jp.co.studio.kaka.util.SearchType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 300ms debounce + distinctUntilChanged + collectLatest: a new keystroke cancels whatever search
 * (debounce wait or in-flight network call) was still pending for the previous keyword. iOS
 * fires a request on every keystroke with no debounce - this is a deliberate improvement.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val downloadStateHolder: DownloadStateHolder,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val keywordFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            keywordFlow
                .debounce(Constants.SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { keyword -> performSearch(keyword) }
        }
        viewModelScope.launch {
            downloadStateHolder.states.collect { states -> _uiState.update { it.copy(downloadStates = states) } }
        }
    }

    fun onKeywordChange(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
        keywordFlow.value = keyword
    }

    fun downloadMusic(music: Music) {
        downloadStateHolder.download(music)
        viewModelScope.launch { eventRepository.trackDownload(music.id, source = "search_result") }
    }

    private suspend fun performSearch(keyword: String) {
        if (keyword.isBlank()) {
            _uiState.update { it.copy(isLoading = false, result = SearchResult(), errorMessage = null) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        eventRepository.trackSearch(keyword, source = "search")
        val result = searchRepository.search(
            keyword = keyword,
            type = SearchType.ALL,
            page = Constants.SEARCH_DEFAULT_PAGE,
            size = Constants.SEARCH_DEFAULT_SIZE,
        )
        when (result) {
            is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, result = result.data) }
            is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            is ApiResult.NetworkError -> _uiState.update {
                it.copy(isLoading = false, errorMessage = "网络连接失败，请检查网络后重试")
            }
        }
    }
}
