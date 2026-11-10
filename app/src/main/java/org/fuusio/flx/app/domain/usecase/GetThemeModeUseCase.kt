package org.fuusio.flx.app.domain.usecase

import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.kide.domain.usecase.UseCaseFunction

fun interface GetThemeModeUseCase : suspend () -> ThemeMode, UseCaseFunction
