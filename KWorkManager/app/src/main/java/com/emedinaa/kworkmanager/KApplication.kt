package com.emedinaa.kworkmanager

import android.app.Application
import com.emedinaa.kworkmanager.di.Injector

class KApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Injector.setup(this)
    }
}