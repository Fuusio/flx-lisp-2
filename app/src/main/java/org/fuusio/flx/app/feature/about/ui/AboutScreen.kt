package org.fuusio.flx.app.feature.about.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.fuusio.flx.app.FlxLinks
import org.fuusio.flx.app.R
import org.fuusio.flx.app.extensions.openEmail
import org.fuusio.flx.app.extensions.openUrl
import org.fuusio.flx.app.feature.about.presentation.AboutIntent
import org.fuusio.flx.app.feature.about.presentation.AboutProcessor
import org.fuusio.flx.app.feature.about.presentation.AboutScreenLaunched
import org.fuusio.flx.app.feature.about.presentation.AboutViewState
import org.fuusio.flx.app.license.OSILicense
import org.fuusio.flx.app.ui.compose.VerticalSpacer
import org.fuusio.kide.navigation.ScreenContext

@Composable
fun AboutScreen(ctx: ScreenContext<AboutProcessor>) {
    AboutScreenContent(
        state = ctx.processor.states.collectAsStateWithLifecycle().value,
        onMenu = ctx.callback(ScreenContext.MENU),
        onDispatch = ctx.processor::dispatch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreenContent(
    state: AboutViewState,
    onMenu: () -> Unit,
    onDispatch: (AboutIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
            onDispatch(AboutScreenLaunched)
        }

        // One LazyColumn for the whole page rather than a fixed header above a scrolling list, so
        // at a large font or display scale the header cannot squeeze the license list to nothing.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        modifier = Modifier.size(160.dp),
                        painter = painterResource(id = R.drawable.flx_logo_512x512px),
                        contentDescription = "FLX Logo",
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "FLX Lisp Demo v${state.version}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Copyright © Marko Salmela 2016 - 2026",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item { ContactSection(version = state.version) }

            item { SourceCodeSection() }

            item { BuildWithSection() }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle("OSI Licenses:")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            itemsIndexed(OSI_LIBRARIES) { index, library ->
                OsiLicenseEntry(
                    libraryName = library.name,
                    copyright = library.copyright,
                    license = library.license,
                    customLicenseText = library.licenseNote,
                    githubUrl = library.githubUrl,
                )
                if (index < OSI_LIBRARIES.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

/**
 * How to reach the developer. The mail row puts the app version in the subject line, so a report
 * arrives already saying which build it came from.
 */
@Composable
private fun ContactSection(version: String) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Contact:")
        Spacer(modifier = Modifier.height(4.dp))

        ContactRow(
            iconRes = R.drawable.alternate_email_24px,
            label = FlxLinks.SUPPORT_EMAIL,
            clickLabel = "Send email to ${FlxLinks.SUPPORT_EMAIL}",
        ) {
            context.openEmail(address = FlxLinks.SUPPORT_EMAIL, subject = "FLX Lisp Demo v$version")
        }

        ContactRow(
            iconRes = R.drawable.web_24px,
            label = FlxLinks.WEBSITE_LABEL,
            clickLabel = "Open ${FlxLinks.WEBSITE_LABEL} in a browser",
        ) {
            context.openUrl(FlxLinks.WEBSITE_URL)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SourceCodeSection() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Source code:")
        Spacer(modifier = Modifier.height(4.dp))

        ContactRow(
            iconRes = R.drawable.code_24px,
            label = FlxLinks.GITHUB_LABEL,
            clickLabel = "Open FLX Lisp on GitHub",
        ) {
            context.openUrl(FlxLinks.GITHUB_URL)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BuildWithSection() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Build with:")
        Spacer(modifier = Modifier.height(4.dp))

        ContactRow(
            iconRes = R.drawable.logo_kide_278x278,
            label = FlxLinks.KIDE_LIBRARY_LABEL,
            useIcon = false,
            clickLabel = "KIDE architecture library in Github",
        ) {
            context.openUrl(FlxLinks.KIDE_LIBRARY_URL)
        }
    }
}

/**
 * One tappable contact line: an icon, the destination as its own label, and nothing else.
 *
 * @param clickLabel What the row does, for TalkBack. It goes on the row rather than on the icon so
 *  the whole line is announced once as a single button — an icon with its own description would be
 *  read out a second time as a separate element.
 */
@Composable
private fun ContactRow(
    @DrawableRes iconRes: Int,
    label: String,
    clickLabel: String,
    useIcon: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Material's minimum touch target. The text alone is well under it, and a link that is
            // hard to hit is the kind of thing only the person who wrote it can reliably tap.
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClickLabel = clickLabel, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (useIcon) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = iconRes),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            // color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        )
    }
}

/**
 * One acknowledged open-source dependency.
 *
 * @property licenseNote Shown in place of the standard license text when a library needs something
 *   said that the license alone does not cover, such as which artifacts a grouped entry stands for.
 */
private data class OsiLibrary(
    val name: String,
    val copyright: String,
    val license: OSILicense,
    val githubUrl: String? = null,
    val licenseNote: String? = null,
)

/**
 * The open-source software this app ships, in alphabetical order. Kept in step with the
 * `implementation` dependencies of `:app` and `:flx`.
 */
private val OSI_LIBRARIES: List<OsiLibrary> = listOf(
    OsiLibrary(
        name = "AndroidX (Jetpack)",
        copyright = "Copyright © The Android Open Source Project",
        license = OSILicense.APACHE_2,
        githubUrl = "https://github.com/androidx/androidx",
        licenseNote = "Covers the AndroidX libraries this app uses: Jetpack Compose, Material 3, " +
            "Activity, Lifecycle, Navigation 3, DataStore and Core KTX.\n\n" +
            OSILicense.APACHE_2.licenseText,
    ),
    OsiLibrary(
        name = "FLX Lisp 2",
        copyright = "Copyright © 2016 - 2026 Marko Salmela",
        license = OSILicense.APACHE_2,
        githubUrl = FlxLinks.GITHUB_URL,
    ),
    OsiLibrary(
        name = "Kide",
        copyright = "Copyright © 2026 Marko Salmela",
        license = OSILicense.APACHE_2,
        githubUrl = "https://github.com/Fuusio/kide",
    ),
    OsiLibrary(
        name = "Koin",
        copyright = "Copyright © 2017 Arnaud Giuliani, Laurent Baresse",
        license = OSILicense.APACHE_2,
        githubUrl = "https://github.com/InsertKoinIO/koin",
    ),
    OsiLibrary(
        name = "Kotlin and kotlinx libraries",
        copyright = "Copyright © JetBrains s.r.o. and Kotlin Programming Language contributors",
        license = OSILicense.APACHE_2,
        githubUrl = "https://github.com/JetBrains/kotlin",
        licenseNote = "Covers the Kotlin standard library and the kotlinx " +
            "libraries this app uses: Coroutines, Serialization and Collections Immutable.\n\n" +
            OSILicense.APACHE_2.licenseText,
    ),
    OsiLibrary(
        name = "Timber",
        copyright = "Copyright © 2013 Jake Wharton",
        license = OSILicense.APACHE_2,
        githubUrl = "https://github.com/JakeWharton/timber",
    ),
)

@Composable
private fun OsiLicenseEntry(
    libraryName: String,
    copyright: String,
    license: OSILicense,
    customLicenseText: String? = null,
    githubUrl: String? = null,
) {
    val isExpanded = remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded.value) 180f else 0f
    )
    val context = LocalContext.current

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = libraryName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                VerticalSpacer(8.dp)
                Text(
                    text = copyright,
                    style = MaterialTheme.typography.bodySmall,
                )
                VerticalSpacer(4.dp)
                Text(
                    text = license.displayName,
                    style = MaterialTheme.typography.labelMedium,
                )
                VerticalSpacer(4.dp)
            }
            Icon(
                painter = painterResource(R.drawable.keyboard_arrow_down_24px),
                contentDescription = if (isExpanded.value) "Collapse" else "Expand",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationState)
                    .clickable { isExpanded.value = !isExpanded.value }
            )
        }

        if (isExpanded.value) {
            // Both of these used to call `startActivity` directly, which throws
            // `ActivityNotFoundException` on a device with no browser — a crash on the About
            // screen for a tap on a license link. `openUrl` reports it instead.
            Text(
                text = license.url,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = "Open the ${license.displayName} license in a browser",
                ) { context.openUrl(license.url) }
            )
            if (githubUrl != null) {
                VerticalSpacer(8.dp)
                Text(
                    text = githubUrl,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = "Open $libraryName on GitHub",
                    ) { context.openUrl(githubUrl) }
                )
            }

            Text(
                text = customLicenseText ?: license.licenseText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 0.dp, top = 4.dp, end = 0.dp, bottom = 8.dp)
            )
        }
    }
}

