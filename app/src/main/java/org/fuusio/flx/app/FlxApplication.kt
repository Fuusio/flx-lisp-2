package org.fuusio.flx.app

import android.app.Application
import org.fuusio.flx.app.feature.about.AboutFeature
import org.fuusio.flx.app.feature.flx_info.FlxInfoFeature
import org.fuusio.flx.app.feature.flx_reference.FlxReferenceFeature
import org.fuusio.flx.app.feature.repl.FlxReplFeature
import org.fuusio.flx.app.feature.settings.SettingsFeature
import org.fuusio.kide.log.KideLog
import org.fuusio.kide.log.KideLogger
import org.fuusio.kide.log.LogLevel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.Module
import timber.log.Timber

class FlxApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val isDebuggable =
            (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        startKoin {
            androidContext(this@FlxApplication.applicationContext)
            modules(getModules())
        }

        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
            KideLog.minLevel = LogLevel.Debug
        } else {
            KideLog.minLevel = LogLevel.Warning
        }
        KideLog.logger = KideLogger { level, tag, message, throwable ->
            val tree = Timber.tag(tag)
            when (level) {
                LogLevel.Verbose -> tree.v(throwable, message)
                LogLevel.Debug -> tree.d(throwable, message)
                LogLevel.Info -> tree.i(throwable, message)
                LogLevel.Warning -> tree.w(throwable, message)
                LogLevel.Error -> tree.e(throwable, message)
                LogLevel.None -> Unit
            }
        }

        initializeFeatures()
    }

    private fun getModules(): List<Module> = listOf(
        FlxApplicationFeature.koinModule(applicationContext),
        FlxInfoFeature.koinModule(applicationContext),
        FlxReplFeature.koinModule(applicationContext),
        FlxReferenceFeature.koinModule(applicationContext),
        SettingsFeature.koinModule(applicationContext),
        AboutFeature.koinModule(applicationContext),
    )

    private fun initializeFeatures() {
        FlxInfoFeature.initialize()
        FlxReplFeature.initialize()
        FlxReferenceFeature.initialize()
        SettingsFeature.initialize()
        AboutFeature.initialize()
    }
}
