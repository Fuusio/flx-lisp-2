package org.fuusio.flx.app.domain.adapter.app_info

interface AppInfoRepository {

    suspend fun getVersion(): String
}
