package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.RegionDto
import jp.co.studio.kaka.data.remote.dto.SearchResponseDto
import jp.co.studio.kaka.domain.model.Region
import jp.co.studio.kaka.domain.model.SearchResult

fun RegionDto.toDomain(): Region = Region(
    regionCode = regionCode,
    regionNameZh = regionNameZh,
    regionNameJa = regionNameJa,
    regionNameEn = regionNameEn,
)

fun SearchResponseDto.toDomain(): SearchResult = SearchResult(
    musics = musics?.map { it.toDomain() },
    artists = artists?.map { it.toDomain() },
    categories = categories?.map { it.toDomain() },
    regions = regions?.map { it.toDomain() },
)
