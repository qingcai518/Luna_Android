package jp.co.studio.kaka.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Lets a ViewModel emit either a server-supplied message or a localized string resource without
 * depending on Android's Context/Resources - resolution to an actual String happens in Compose,
 * at render time, so it always reflects the current app locale.
 */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    data class Resource(@StringRes val resId: Int) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> stringResource(resId)
    }
}
