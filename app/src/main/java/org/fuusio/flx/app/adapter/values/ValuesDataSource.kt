package org.fuusio.flx.app.adapter.values

import kotlinx.coroutines.flow.Flow

/** Persistent key-value storage for small values, such as user settings. */
interface ValuesDataSource {

    suspend fun putString(key: String, value: String)

    suspend fun getString(key: String): String?

    fun getStringFlow(key: String): Flow<String?>
}
