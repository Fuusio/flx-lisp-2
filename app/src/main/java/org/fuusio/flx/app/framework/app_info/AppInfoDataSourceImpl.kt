package org.fuusio.flx.app.framework.app_info

import org.fuusio.flx.app.BuildConfig
import org.fuusio.flx.app.adapter.app_info.AppInfoDataSource
import org.fuusio.kide.framework.AbstractDataSource

/** Reports the version the app was built as, from `BuildConfig.VERSION_NAME`. */
class AppInfoDataSourceImpl : AbstractDataSource(), AppInfoDataSource {

    override fun getVersion(): String = BuildConfig.VERSION_NAME
}
