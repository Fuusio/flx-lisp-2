package org.fuusio.flx.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.flx.app.domain.usecase.GetThemeModeFlowUseCase
import org.fuusio.flx.app.feature.about.navigation.AboutNavKey
import org.fuusio.flx.app.feature.flx_info.navigation.FlxInfoNavKey
import org.fuusio.flx.app.feature.flx_reference.navigation.FlxReferenceNavKey
import org.fuusio.flx.app.feature.repl.navigation.FlxReplNavKey
import org.fuusio.flx.app.feature.settings.navigation.SettingsNavKey
import org.fuusio.flx.app.ui.compose.HorizontalSpacer
import org.fuusio.flx.app.ui.theme.FlxTheme
import org.fuusio.kide.di.get
import org.fuusio.kide.navigation.AppNavigation
import org.fuusio.kide.navigation.ScreenContext
import org.fuusio.kide.navigation.ScreenNavKey
import org.fuusio.kide.navigation.currentNavKey
import org.fuusio.kide.navigation.navigateTo
import org.fuusio.kide.navigation.rememberAppNavBackStack

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // The whole UI follows the theme mode selected in the Settings. The selection is
            // observed as a Flow, so a change in the Settings is applied immediately.
            val themeModeFlow = remember { get<GetThemeModeFlowUseCase>()() }
            val themeMode = themeModeFlow.collectAsStateWithLifecycle(ThemeMode.DEFAULT).value
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            DisposableEffect(isDark) {
                val barStyle = if (isDark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose {}
            }

            FlxTheme(darkTheme = isDark) {
                FlxApp()
            }
        }
    }
}

/** A top-level destination, reached from the drawer. */
private data class NavItem(
    val navKey: ScreenNavKey<*>,
    val label: String,
    val iconRes: Int,
)

/** The drawer's items, in the order they are shown. */
@Composable
private fun drawerItems(): List<NavItem> {
    val resources = LocalResources.current
    return remember {
        listOf(
            NavItem(FlxInfoNavKey, resources.getString(R.string.label_drawer_item_flx_info), R.drawable.info_24px),
            NavItem(FlxReplNavKey, resources.getString(R.string.label_drawer_item_flx_repl), R.drawable.terminal_2_24px),
            NavItem(FlxReferenceNavKey, resources.getString(R.string.label_drawer_item_flx_reference), R.drawable.book_24px),
            NavItem(SettingsNavKey(), resources.getString(R.string.label_drawer_item_settings), R.drawable.settings_24px),
            NavItem(AboutNavKey, resources.getString(R.string.label_drawer_item_about), R.drawable.mobile_info_24px),
        )
    }
}

@Composable
private fun FlxApp() {
    val backStack = rememberAppNavBackStack(FlxInfoNavKey)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentKey = backStack.currentNavKey()
    val keyboardController = LocalSoftwareKeyboardController.current
    val items = drawerItems()

    // The drawer's edge swipe is allowed exactly where the hamburger is: on the top-level screens.
    val isAtTopLevel = items.any { it.navKey == currentKey }

    // The soft keyboard is not dismissed automatically when the drawer opens, so it would stay on
    // top of the drawer sheet (the REPL keeps its input focused). Keying on targetValue also covers
    // the drawer being opened with the edge-swipe gesture instead of the menu button.
    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) {
            keyboardController?.hide()
        }
    }

    val onMenu: () -> Unit = {
        scope.launch {
            if (drawerState.isClosed) drawerState.open() else drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // The reference scrolls its page, and its wide code blocks and tables scroll sideways: the
        // edge swipe would open the drawer in the middle of that. There the menu icon opens it, and
        // an open drawer can still be swiped shut.
        gesturesEnabled = isAtTopLevel && (currentKey != FlxReferenceNavKey || drawerState.isOpen),
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier.size(100.dp),
                        painter = painterResource(id = R.drawable.flx_logo_512x512px),
                        contentDescription = "FLX Logo",
                    )
                    HorizontalSpacer(16.dp)
                    Text(
                        text = "F L X   L I S P",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    items.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            icon = {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = null,
                                )
                            },
                            selected = currentKey == item.navKey,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (currentKey != item.navKey) backStack.navigateTo(item.navKey)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
    ) {
        AppNavigation(
            backStack = backStack,
            callbacks = mapOf(ScreenContext.MENU to onMenu),
        )
    }
}
