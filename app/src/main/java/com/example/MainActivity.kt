package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.presentation.CookrViewModel
import com.example.presentation.CookrViewModelFactory
import com.example.presentation.core.theme.CookrTheme
import com.example.presentation.screens.MainLayout

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Repository & ViewModel setup using App application context container
        val app = application as CookrApplication
        val repository = app.container.cookrRepository
        val viewModel: CookrViewModel by viewModels {
            CookrViewModelFactory(app, repository)
        }

//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val syncWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.domain.worker.RecipeSyncWorker>(
//                    15, java.util.concurrent.TimeUnit.MINUTES
//                ).build()
//                androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
//                    "RecipeSync",
//                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
//                    syncWorkRequest
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

        setContent {
            val themeSelection by viewModel.themeSelection.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
            
            CookrTheme(
                themeSelection = themeSelection,
                themeMode = themeMode,
                dynamicColor = dynamicColorEnabled
            ) {
                MainLayout(viewModel = viewModel)
            }
        }
    }
}
