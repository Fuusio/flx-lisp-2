package org.fuusio.flx.app.feature.about.presentation

import org.fuusio.kide.presentation.ViewIntent

sealed interface AboutIntent : ViewIntent

data object AboutScreenLaunched : AboutIntent
