package jp.co.studio.kaka.util

import jp.co.studio.kaka.domain.model.LyricLine

/** Parses standard LRC text (`[mm:ss.xx]lyric`); non-timed metadata tags like [ar:]/[ti:] are ignored. */
object LrcParser {

    private val TIME_TAG_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})]""")

    fun parse(lrcText: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        lrcText.lineSequence().forEach { rawLine ->
            val tags = TIME_TAG_REGEX.findAll(rawLine).toList()
            if (tags.isEmpty()) return@forEach
            val text = rawLine.substring(tags.last().range.last + 1).trim()
            tags.forEach { tag ->
                val (minutes, seconds, fraction) = tag.destructured
                val fractionMs = if (fraction.length == 2) fraction.toLong() * 10 else fraction.toLong()
                val timeMs = minutes.toLong() * 60_000L + seconds.toLong() * 1_000L + fractionMs
                lines += LyricLine(timeMs = timeMs, text = text)
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}
