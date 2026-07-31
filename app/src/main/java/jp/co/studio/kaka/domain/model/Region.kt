package jp.co.studio.kaka.domain.model

data class Region(
    val regionCode: String?,
    val regionNameZh: String?,
    val regionNameJa: String?,
    val regionNameEn: String?,
) {
    /** Best-effort display name - region results are read-only in the UI (matches iOS behavior). */
    val displayName: String
        get() = regionNameZh ?: regionNameEn ?: regionNameJa ?: regionCode.orEmpty()
}
