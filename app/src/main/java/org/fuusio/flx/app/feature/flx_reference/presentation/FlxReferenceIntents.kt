package org.fuusio.flx.app.feature.flx_reference.presentation

import org.fuusio.kide.presentation.ViewIntent

sealed interface FlxReferenceIntent : ViewIntent

data object FlxReferenceScreenLaunched : FlxReferenceIntent
