package org.fuusio.flx.app.domain.usecase

import org.fuusio.kide.domain.usecase.UseCaseFunction

fun interface GetFlxReferenceHtmlUseCase : suspend () -> String, UseCaseFunction
