package org.fuusio.flx.app.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.fuusio.flx.app.R
import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.flx.app.feature.settings.presentation.SettingsIntent
import org.fuusio.flx.app.feature.settings.presentation.SettingsProcessor
import org.fuusio.flx.app.feature.settings.presentation.SettingsScreenLaunched
import org.fuusio.flx.app.feature.settings.presentation.SettingsViewState
import org.fuusio.flx.app.feature.settings.presentation.ThemeModeSelected
import org.fuusio.flx.app.ui.theme.FlxTheme
import org.fuusio.kide.navigation.ScreenContext

@Composable
fun SettingsScreen(
    ctx: ScreenContext<SettingsProcessor>,
    showsBackNavigation: Boolean = false,
) {
    val onDispatch = ctx.processor::dispatch
    SettingsScreenContent(
        state = ctx.processor.states.collectAsStateWithLifecycle().value,
        showsBackNavigation = showsBackNavigation,
        onNavigationIconClick = if (showsBackNavigation) {
            ctx.onBack
        } else {
            ctx.callback(ScreenContext.MENU)
        },
        onDispatch = onDispatch,
    )
}

/** The panes of the Settings screen. More are added here as the demo grows. */
private enum class SettingsSection(
    val title: String,
    val summary: String,
) {
    APPEARANCE(
        title = "Appearance",
        summary = "Theme",
    ),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    state: SettingsViewState,
    showsBackNavigation: Boolean,
    onNavigationIconClick: () -> Unit,
    onDispatch: (SettingsIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClick) {
                        Icon(
                            painter = painterResource(
                                if (showsBackNavigation) R.drawable.arrow_back_24px else R.drawable.menu_24px,
                            ),
                            contentDescription = if (showsBackNavigation) "Back" else "Open menu",
                        )
                    }
                },
            )
        },
    ) { innerPaddings ->

        LaunchedEffect(Unit) {
            onDispatch(SettingsScreenLaunched)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddings)
                .verticalScroll(rememberScrollState()),
        ) {
            // Expansion is presentation-only, so it is kept here rather than in SettingsViewState.
            // Savable so the open panes survive configuration changes and process death.
            var expandedSections by rememberSaveable {
                mutableStateOf(setOf(SettingsSection.APPEARANCE.name))
            }

            SettingsSection.entries.forEach { section ->
                SettingsPane(
                    title = section.title,
                    summary = section.summary,
                    isExpanded = section.name in expandedSections,
                    onToggle = {
                        expandedSections = if (section.name in expandedSections) {
                            expandedSections - section.name
                        } else {
                            expandedSections + section.name
                        }
                    },
                ) {
                    when (section) {
                        SettingsSection.APPEARANCE -> ThemePreference(
                            themeMode = state.themeMode,
                            onThemeModeSelected = { themeMode ->
                                onDispatch(ThemeModeSelected(themeMode))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPane(
    title: String,
    summary: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                painter = painterResource(
                    if (isExpanded) R.drawable.keyboard_arrow_up_24px else R.drawable.keyboard_arrow_down_24px,
                ),
                contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                content = content,
            )
        }
    }

    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePreference(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Select the color theme of the application",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == themeMode,
                    onClick = { onThemeModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeMode.entries.size,
                    ),
                ) { Text(mode.label) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() = FlxTheme {
    SettingsScreenContent(SettingsViewState(), showsBackNavigation = false, onNavigationIconClick = {}, onDispatch = {})
}
