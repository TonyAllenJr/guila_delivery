package br.com.antonio.distribuidoradoguila

import android.app.Application
import br.com.antonio.distribuidoradoguila.modules.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(level = Level.ERROR)
            androidContext(this@AppApplication)
            modules(
                listOf(dataBaseModule, uiModule, repositoryModule, viewModelModule, firebaseModule
                )
            )
        }
    }
}