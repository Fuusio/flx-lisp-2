package org.fuusio.flx.app.feature.flx_info.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.fuusio.flx.app.FlxLinks
import org.fuusio.flx.app.R
import org.fuusio.flx.app.extensions.openUrl
import org.fuusio.flx.app.feature.flx_info.presentation.FlxInfoIntent
import org.fuusio.flx.app.feature.flx_info.presentation.FlxInfoProcessor
import org.fuusio.flx.app.feature.flx_info.presentation.FlxInfoScreenLaunched
import org.fuusio.flx.app.feature.flx_info.presentation.FlxInfoViewState
import org.fuusio.flx.app.feature.repl.navigation.FlxReplNavKey
import org.fuusio.flx.app.ui.compose.VerticalSpacer
import org.fuusio.flx.app.ui.theme.FlxTheme
import org.fuusio.kide.navigation.ScreenContext

/** What FLX offers, one line each. */
private val HIGHLIGHTS = listOf(
    "A small Lisp dialect implemented in pure Kotlin",
    "Embeddable in any Android or JVM app as a library",
    "Immutable lists, vectors, maps and sets",
    "Functions, closures, macros and objects",
    "Calls into Kotlin and Java through proxies",
    "A virtual machine that can be started, paused and stopped",
)

/** A short program shown on the screen, using forms the REPL accepts as-is. */
private const val EXAMPLE = """(fun square [x] (* x x))

(val numbers (range 1 6))

(println (map square numbers))

(reduce + (map square numbers))"""

@Composable
fun FlxInfoScreen(ctx: ScreenContext<FlxInfoProcessor>) {
    FlxInfoScreenContent(
        state = ctx.processor.states.collectAsStateWithLifecycle().value,
        onMenu = ctx.callback(ScreenContext.MENU),
        onOpenRepl = { ctx.navigateTo(FlxReplNavKey) },
        onDispatch = ctx.processor::dispatch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlxInfoScreenContent(
    state: FlxInfoViewState,
    onMenu: () -> Unit,
    onOpenRepl: () -> Unit,
    onDispatch: (FlxInfoIntent) -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FLX Info") },
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(
                            painter = painterResource(R.drawable.menu_24px),
                            contentDescription = "Open menu",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->

        LaunchedEffect(Unit) {
            onDispatch(FlxInfoScreenLaunched)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier.size(140.dp),
                painter = painterResource(id = R.drawable.flx_logo_512x512px),
                contentDescription = "FLX Logo",
            )
            VerticalSpacer(16.dp)
            Text(
                text = "FLX Lisp",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            if (state.version.isNotEmpty()) {
                Text(
                    text = "Demo app v${state.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VerticalSpacer(16.dp)
            Text(
                text = "FLX is an open source Lisp for Kotlin and Android. Use it to script your " +
                    "app, let users write small programs, or explore Lisp interactively in the REPL.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            VerticalSpacer(24.dp)
            SectionTitle("Highlights")
            VerticalSpacer(8.dp)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HIGHLIGHTS.forEach { highlight ->
                    Row {
                        Text("•  ", color = MaterialTheme.colorScheme.primary)
                        Text(highlight, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            VerticalSpacer(24.dp)
            SectionTitle("A taste of FLX")
            VerticalSpacer(8.dp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Text(
                    text = EXAMPLE,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            VerticalSpacer(24.dp)
            Button(
                onClick = onOpenRepl,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.terminal_2_24px),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("Try it in the REPL")
            }
            VerticalSpacer(8.dp)
            OutlinedButton(
                onClick = { context.openUrl(FlxLinks.GITHUB_URL) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.code_24px),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("View on GitHub")
            }
            Text(
                text = FlxLinks.GITHUB_LABEL,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

@Preview(showBackground = true)
@Composable
private fun FlxInfoPreview() = FlxTheme {
    FlxInfoScreenContent(FlxInfoViewState(version = "1.0.0"), onMenu = {}, onOpenRepl = {}, onDispatch = {})
}
