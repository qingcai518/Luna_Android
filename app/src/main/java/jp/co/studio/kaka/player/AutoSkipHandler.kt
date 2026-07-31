package jp.co.studio.kaka.player

import javax.inject.Inject
import javax.inject.Singleton

/**
 * ExoPlayer does not auto-skip failed items by default. Tracks which queue indexes have failed
 * this session and picks the next untried index (wrapping around); once every item has failed
 * once, [onItemFailed] returns null so the caller stops instead of looping forever.
 */
@Singleton
class AutoSkipHandler @Inject constructor() {

    private val failedIndexes = mutableSetOf<Int>()
    private var itemCount = 0

    fun reset(itemCount: Int) {
        failedIndexes.clear()
        this.itemCount = itemCount
    }

    fun onItemFailed(currentIndex: Int): Int? {
        if (itemCount <= 0) return null
        failedIndexes += currentIndex
        if (failedIndexes.size >= itemCount) return null

        var candidate = (currentIndex + 1) % itemCount
        while (candidate in failedIndexes) {
            candidate = (candidate + 1) % itemCount
            if (candidate == currentIndex) return null
        }
        return candidate
    }
}
