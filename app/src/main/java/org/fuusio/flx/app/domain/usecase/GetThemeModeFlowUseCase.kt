package org.fuusio.flx.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.kide.domain.usecase.UseCaseFunction

fun interface GetThemeModeFlowUseCase : () -> Flow<ThemeMode>, UseCaseFunction
