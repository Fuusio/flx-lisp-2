package org.fuusio.flx.app.feature.flx_info.presentation

import org.fuusio.kide.presentation.ViewIntent

sealed interface FlxInfoIntent : ViewIntent

data object FlxInfoScreenLaunched : FlxInfoIntent
