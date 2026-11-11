package org.fuusio.flx.app.feature.flx_reference.presentation

import org.fuusio.kide.presentation.ViewState

/** [html] is the reference page once it is loaded; [hasFailed] when it could not be. */
data class FlxReferenceViewState(
    val html: String? = null,
    val hasFailed: Boolean = false,
) : ViewState
