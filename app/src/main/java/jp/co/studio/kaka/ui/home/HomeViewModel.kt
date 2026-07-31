package jp.co.studio.kaka.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.repository.ArtistRepository
import jp.co.studio.kaka.domain.repository.CategoryRepository
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            coroutineScope {
                val artistsDeferred = async { artistRepository.getArtists() }
                val categoriesDeferred = async { categoryRepository.getCategories() }
                val artistsResult = artistsDeferred.await()
                val categoriesResult = categoriesDeferred.await()

                val artists = (artistsResult as? ApiResult.Success)?.data.orEmpty()
                val categories = (categoriesResult as? ApiResult.Success)?.data.orEmpty()
                val errorMessage = when {
                    artistsResult is ApiResult.Error -> artistsResult.message
                    categoriesResult is ApiResult.Error -> categoriesResult.message
                    artistsResult is ApiResult.NetworkError || categoriesResult is ApiResult.NetworkError ->
                        "网络连接失败，请检查网络后重试"
                    else -> null
                }
                _uiState.update {
                    HomeUiState(isLoading = false, artists = artists, categories = categories, errorMessage = errorMessage)
                }
            }
        }
    }
}
