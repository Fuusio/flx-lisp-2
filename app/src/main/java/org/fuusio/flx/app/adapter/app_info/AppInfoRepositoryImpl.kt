package org.fuusio.flx.app.adapter.app_info

import org.fuusio.flx.app.domain.adapter.app_info.AppInfoRepository
import org.fuusio.kide.adapter.AbstractRepository

class AppInfoRepositoryImpl(private val dataSource: AppInfoDataSource) :
    AbstractRepository(), AppInfoRepository {

    override suspend fun getVersion() = dataSource.getVersion()
}
