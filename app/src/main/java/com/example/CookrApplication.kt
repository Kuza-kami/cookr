package com.example

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.example.domain.worker.RecipeSyncWorker
import java.util.concurrent.TimeUnit

class CookrApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
