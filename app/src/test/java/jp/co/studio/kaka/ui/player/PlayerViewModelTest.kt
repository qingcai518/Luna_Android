package jp.co.studio.kaka.ui.player

import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.player.MediaControllerRepository
import jp.co.studio.kaka.player.PlayerRepeatMode
import jp.co.studio.kaka.player.PlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerViewModelTest {

    private fun musicFixture(id: Long) = Music(
        id = id,
        title = "曲目$id",
        coverUrl = null,
        audioUrl = "https://resource.qingcai518.com/music/$id.mp3",
        releaseDate = null,
        durationSeconds = null,
        artist = null,
        category = null,
    )

    private class FakeMediaControllerRepository : MediaControllerRepository {
        private val _uiState = MutableStateFlow(PlayerUiState())
        override val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

        val calls = mutableListOf<String>()
        var lastPlayQueue: Pair<List<Music>, Int>? = null
        var lastSeekPositionMs: Long? = null
        var lastSeekQueueIndex: Int? = null
        var lastRepeatMode: PlayerRepeatMode? = null
        var lastShuffleEnabled: Boolean? = null

        override fun playQueue(musics: List<Music>, startIndex: Int) {
            calls += "playQueue"
            lastPlayQueue = musics to startIndex
        }

        override fun playPause() { calls += "playPause" }
        override fun seekTo(positionMs: Long) { calls += "seekTo"; lastSeekPositionMs = positionMs }
        override fun skipNext() { calls += "skipNext" }
        override fun skipPrevious() { calls += "skipPrevious" }
        override fun seekToQueueItem(index: Int) { calls += "seekToQueueItem"; lastSeekQueueIndex = index }
        override fun setRepeatMode(mode: PlayerRepeatMode) { calls += "setRepeatMode"; lastRepeatMode = mode }
        override fun setShuffleEnabled(enabled: Boolean) { calls += "setShuffleEnabled"; lastShuffleEnabled = enabled }
        override fun stopAndClear() { calls += "stopAndClear" }
    }

    @Test
    fun `uiState is the same StateFlow instance exposed by the media controller repository`() {
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = PlayerViewModel(mediaControllerRepository)

        // MainScaffold hands this ViewModel to both MiniPlayerBar and FullPlayerScreen expecting
        // them to observe the exact same source, not a copy - identity matters here, not just equality.
        assertSame(mediaControllerRepository.uiState, viewModel.uiState)
    }

    @Test
    fun `playQueue forwards the queue and start index unchanged`() {
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = PlayerViewModel(mediaControllerRepository)
        val queue = listOf(musicFixture(1L), musicFixture(2L), musicFixture(3L))

        viewModel.playQueue(queue, startIndex = 2)

        assertEquals(queue to 2, mediaControllerRepository.lastPlayQueue)
    }

    @Test
    fun `every transport and playback control delegates directly with correct arguments`() {
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = PlayerViewModel(mediaControllerRepository)

        viewModel.playPause()
        viewModel.seekTo(3_000L)
        viewModel.skipNext()
        viewModel.skipPrevious()
        viewModel.seekToQueueItem(1)
        viewModel.setRepeatMode(PlayerRepeatMode.ONE)
        viewModel.setShuffleEnabled(true)
        viewModel.stopAndClear()

        assertEquals(
            listOf("playPause", "seekTo", "skipNext", "skipPrevious", "seekToQueueItem", "setRepeatMode", "setShuffleEnabled", "stopAndClear"),
            mediaControllerRepository.calls,
        )
        assertEquals(3_000L, mediaControllerRepository.lastSeekPositionMs)
        assertEquals(1, mediaControllerRepository.lastSeekQueueIndex)
        assertEquals(PlayerRepeatMode.ONE, mediaControllerRepository.lastRepeatMode)
        assertTrue(mediaControllerRepository.lastShuffleEnabled == true)
    }
}
