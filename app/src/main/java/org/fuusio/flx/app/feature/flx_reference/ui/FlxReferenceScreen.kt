package org.fuusio.flx.app.feature.flx_reference.ui

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.fuusio.flx.app.R
import org.fuusio.flx.app.extensions.openUrl
import org.fuusio.flx.app.feature.flx_reference.presentation.FlxReferenceIntent
import org.fuusio.flx.app.feature.flx_reference.presentation.FlxReferenceProcessor
import org.fuusio.flx.app.feature.flx_reference.presentation.FlxReferenceScreenLaunched
import org.fuusio.flx.app.feature.flx_reference.presentation.FlxReferenceViewState
import org.fuusio.kide.navigation.ScreenContext
import org.json.JSONObject

/** Not a real site: only the base the page is loaded with. */
private const val BASE_URL = "https://flx-lisp.invalid/reference/"

/** The scheme of the page's links to its own sections; see `docs/reference/build_reference_html.py`. */
private const val ANCHOR_SCHEME = "flx-anchor"

@Composable
fun FlxReferenceScreen(ctx: ScreenContext<FlxReferenceProcessor>) {
    FlxReferenceScreenContent(
        state = ctx.processor.states.collectAsStateWithLifecycle().value,
        onMenu = ctx.callback(ScreenContext.MENU),
        onDispatch = ctx.processor::dispatch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlxReferenceScreenContent(
    state: FlxReferenceViewState,
    onMenu: () -> Unit,
    onDispatch: (FlxReferenceIntent) -> Unit,
) {
    LaunchedEffect(Unit) { onDispatch(FlxReferenceScreenLaunched) }

    val history = remember { ReferenceHistory() }
    // Back returns to where the reader was before an in-page link, and only then leaves the screen.
    BackHandler(enabled = history.canGoBack) { history.goBack() }

    // The page follows the app's theme: its colours come from the Material colour scheme, which in
    // turn follows the theme mode selected in the Settings.
    val scheme = MaterialTheme.colorScheme
    val colors = ReferenceColors(
        background = scheme.background.toArgb(),
        foreground = scheme.onBackground.toArgb(),
        primary = scheme.primary.toArgb(),
        codeBackground = scheme.surfaceContainerHigh.toArgb(),
        border = scheme.outlineVariant.toArgb(),
        isDark = scheme.background.luminance() < 0.5f,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reference_title)) },
                navigationIcon = {
                    if (history.canGoBack) {
                        IconButton(onClick = history::goBack) {
                            Icon(painterResource(R.drawable.arrow_back_24px), stringResource(R.string.reference_back))
                        }
                    } else {
                        IconButton(onClick = onMenu) {
                            Icon(painterResource(R.drawable.menu_24px), stringResource(R.string.open_menu))
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            when {
                state.html != null -> ReferenceWebView(state.html, colors, history, Modifier.fillMaxSize())
                state.hasFailed -> Text(
                    stringResource(R.string.reference_unavailable),
                    color = MaterialTheme.colorScheme.error,
                )
                else -> CircularProgressIndicator()
            }
        }
    }
}

/**
 * Where the reader has jumped from by following in-page links, as scroll positions, newest last.
 * The page's own history is not used: a link is followed by scrolling, not by navigating.
 */
private class ReferenceHistory {
    private val positions = mutableStateListOf<Int>()
    var webView: WebView? = null

    val canGoBack: Boolean get() = positions.isNotEmpty()

    /** Scrolls [view] to the section [id], remembering where it was. */
    fun jumpTo(view: WebView, id: String) {
        positions.add(view.scrollY)
        view.evaluateJavascript(scrollToJs(id), null)
    }

    fun goBack() {
        if (positions.isEmpty()) return
        val position = positions.removeAt(positions.lastIndex)
        webView?.scrollTo(0, position)
    }
}

/**
 * Shows [html] in a WebView. The page is static and local: it has no network access and the only
 * script run in it is the one that changes its colours. Links to other sites are opened in the
 * browser, and links to sections scroll inside the page (see [ReferenceHistory]).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ReferenceWebView(
    html: String,
    colors: ReferenceColors,
    history: ReferenceHistory,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val css = remember(colors) { colors.toCss() }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            WebView(viewContext).apply {
                // Painted before the page is, so that a dark theme does not flash white.
                setBackgroundColor(colors.background)
                settings.javaScriptEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.blockNetworkLoads = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val url = request.url
                        when {
                            // The page's links to its own sections: flx-anchor:section-id
                            url.scheme == ANCHOR_SCHEME -> history.jumpTo(view, url.schemeSpecificPart)
                            url.scheme == "http" || url.scheme == "https" || url.scheme == "mailto" ->
                                context.openUrl(url.toString())
                        }
                        return true
                    }
                }
                tag = css
                history.webView = this
                loadDataWithBaseURL(BASE_URL, html.withThemeCss(css), "text/html", "utf-8", null)
            }
        },
        update = { view ->
            // A theme change restyles the page in place, so the reader keeps their position.
            if (view.tag != css) {
                view.tag = css
                view.setBackgroundColor(colors.background)
                view.evaluateJavascript(setThemeJs(css), null)
            }
        },
        onRelease = { view ->
            history.webView = null
            view.destroy()
        },
    )
}

private fun setThemeJs(css: String) =
    "document.getElementById('flx-theme').textContent = ${JSONObject.quote(css)};"

private fun scrollToJs(id: String) =
    "var e = document.getElementById(${JSONObject.quote(id)}); if (e) e.scrollIntoView();"
