package org.fuusio.flx.app.domain.usecase

import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.kide.domain.usecase.UseCaseFunction

fun interface SetThemeModeUseCase : suspend (ThemeMode) -> Unit, UseCaseFunction
