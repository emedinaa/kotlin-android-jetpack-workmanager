package com.emedinaa.kworkmanager.di

import android.app.Application
import androidx.work.WorkManager

object Injector {

    private lateinit var workManager: WorkManager

    fun setup(application: Application) {
        workManager = WorkManager.getInstance(application)
    }

    fun provideWorkManager(): WorkManager = workManager
}