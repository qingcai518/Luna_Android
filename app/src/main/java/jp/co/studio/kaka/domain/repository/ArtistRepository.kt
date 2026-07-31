package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.util.ApiResult

interface ArtistRepository {
    suspend fun getArtists(): ApiResult<List<Artist>>
}
