package org.fuusio.flx.app.framework.values

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.fuusio.flx.app.adapter.values.ValuesDataSource
import org.fuusio.kide.framework.AbstractDataSource

/** A [ValuesDataSource] backed by Preferences DataStore. */
class ValuesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : AbstractDataSource(), ValuesDataSource {

    override suspend fun putString(key: String, value: String) {
        dispatch { dataStore.edit { it[stringPreferencesKey(key)] = value } }
    }

    override suspend fun getString(key: String): String? =
        dispatch { dataStore.data.first()[stringPreferencesKey(key)] }

    override fun getStringFlow(key: String): Flow<String?> =
        dataStore.data.map { it[stringPreferencesKey(key)] }
}
