package jp.co.studio.kaka.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutoSkipHandlerTest {

    @Test
    fun `advances to the next index on failure`() {
        val handler = AutoSkipHandler()
        handler.reset(itemCount = 3)

        assertEquals(1, handler.onItemFailed(currentIndex = 0))
    }

    @Test
    fun `wraps around past the end of the queue`() {
        val handler = AutoSkipHandler()
        handler.reset(itemCount = 3)

        assertEquals(0, handler.onItemFailed(currentIndex = 2))
    }

    @Test
    fun `skips indexes that already failed`() {
        val handler = AutoSkipHandler()
        handler.reset(itemCount = 4)

        handler.onItemFailed(currentIndex = 0) // fails 0, suggests 1
        assertEquals(2, handler.onItemFailed(currentIndex = 1)) // fails 1, 1 is known-bad, so suggest 2
    }

    @Test
    fun `returns null once every item in the queue has failed`() {
        val handler = AutoSkipHandler()
        handler.reset(itemCount = 2)

        handler.onItemFailed(currentIndex = 0)
        assertNull(handler.onItemFailed(currentIndex = 1))
    }

    @Test
    fun `reset clears previously failed indexes`() {
        val handler = AutoSkipHandler()
        handler.reset(itemCount = 2)
        handler.onItemFailed(currentIndex = 0)
        handler.onItemFailed(currentIndex = 1) // whole queue now failed

        handler.reset(itemCount = 2)

        assertEquals(1, handler.onItemFailed(currentIndex = 0))
    }

    @Test
    fun `single-item queue fails immediately with no candidate`() {
        val handler = AutoSkipHandler()
        handler.reset(itemCount = 1)

        assertNull(handler.onItemFailed(currentIndex = 0))
    }
}
