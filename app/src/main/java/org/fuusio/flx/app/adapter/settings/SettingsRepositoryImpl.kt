package org.fuusio.flx.app.adapter.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.fuusio.flx.app.adapter.values.ValuesDataSource
import org.fuusio.flx.app.domain.adapter.settings.SettingsRepository
import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.kide.adapter.AbstractRepository

class SettingsRepositoryImpl(
    private val valuesDataSource: ValuesDataSource,
) : AbstractRepository(), SettingsRepository {

    override val themeMode: Flow<ThemeMode> =
        valuesDataSource.getStringFlow(KEY_THEME_MODE).map { key -> ThemeMode.fromKey(key) }

    override suspend fun getThemeMode(): ThemeMode =
        ThemeMode.fromKey(valuesDataSource.getString(KEY_THEME_MODE))

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        valuesDataSource.putString(KEY_THEME_MODE, themeMode.name)
    }

    companion object {
        const val KEY_THEME_MODE = "settings.themeMode"
    }
}
