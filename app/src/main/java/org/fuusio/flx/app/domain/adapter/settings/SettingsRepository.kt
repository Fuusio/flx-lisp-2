package org.fuusio.flx.app.domain.adapter.settings

import kotlinx.coroutines.flow.Flow
import org.fuusio.flx.app.domain.entity.ThemeMode

/**
 * Repository for reading and writing user settings.
 */
interface SettingsRepository {

    /**
     * The currently selected [ThemeMode] as an observable [Flow]. Emits [ThemeMode.DEFAULT] when
     * no theme mode has been persisted.
     */
    val themeMode: Flow<ThemeMode>

    suspend fun getThemeMode(): ThemeMode

    suspend fun setThemeMode(themeMode: ThemeMode)
}
