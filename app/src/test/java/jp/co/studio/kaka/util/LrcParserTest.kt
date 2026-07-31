package jp.co.studio.kaka.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun `parses standard mm colon ss dot xx lines in order`() {
        val lrc = """
            [00:01.00]line one
            [00:12.50]line two
            [01:05.99]line three
        """.trimIndent()

        val result = LrcParser.parse(lrc)

        assertEquals(3, result.size)
        assertEquals(1_000L, result[0].timeMs)
        assertEquals("line one", result[0].text)
        assertEquals(12_500L, result[1].timeMs)
        assertEquals("line two", result[1].text)
        assertEquals(65_990L, result[2].timeMs)
        assertEquals("line three", result[2].text)
    }

    @Test
    fun `sorts out-of-order timestamps`() {
        val lrc = """
            [00:30.00]second
            [00:00.00]first
        """.trimIndent()

        val result = LrcParser.parse(lrc)

        assertEquals(listOf("first", "second"), result.map { it.text })
    }

    @Test
    fun `ignores non-timed metadata tags`() {
        val lrc = """
            [ar:Some Artist]
            [ti:Some Title]
            [00:05.00]actual lyric
        """.trimIndent()

        val result = LrcParser.parse(lrc)

        assertEquals(1, result.size)
        assertEquals("actual lyric", result[0].text)
    }

    @Test
    fun `expands a line with multiple time tags into one entry per tag`() {
        val lrc = "[00:10.00][00:40.00]repeated chorus"

        val result = LrcParser.parse(lrc)

        assertEquals(2, result.size)
        assertEquals(10_000L, result[0].timeMs)
        assertEquals(40_000L, result[1].timeMs)
        assertTrue(result.all { it.text == "repeated chorus" })
    }

    @Test
    fun `returns empty list for blank input`() {
        assertEquals(emptyList<Any>(), LrcParser.parse(""))
    }

    @Test
    fun `supports both 2-digit and 3-digit fractional seconds`() {
        // 2-digit fraction (centiseconds, x10 to get ms) vs 3-digit fraction (milliseconds as-is).
        val twoDigit = LrcParser.parse("[00:01.50]a")
        val threeDigit = LrcParser.parse("[00:01.500]a")

        assertEquals(1_500L, twoDigit.first().timeMs)
        assertEquals(1_500L, threeDigit.first().timeMs)
    }
}
