package jp.co.studio.kaka.domain.model

sealed interface DownloadState {
    data object NotDownloaded : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data class Downloaded(val downloadedMusic: DownloadedMusic) : DownloadState
    data object Failed : DownloadState
}
