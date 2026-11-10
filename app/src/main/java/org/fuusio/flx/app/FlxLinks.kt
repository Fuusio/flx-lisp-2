package org.fuusio.flx.app

/**
 * The addresses the app points people at, in one place.
 *
 * Each address is kept twice on purpose: a label without the scheme, for people to read, and the
 * full URL to actually open.
 */
object FlxLinks {

    const val GITHUB_LABEL = "github.com/Fuusio/flx-lisp-2"
    const val GITHUB_URL = "https://$GITHUB_LABEL"

    const val SUPPORT_EMAIL = "fuusio.app@gmail.com"

    const val WEBSITE_LABEL = "fuusio.org"
    const val WEBSITE_URL = "https://$WEBSITE_LABEL"

    const val KIDE_LIBRARY_URL = "https://www.fuusio.org/Kide"
    const val KIDE_LIBRARY_LABEL = "Open source KIDE architecture library"
}
