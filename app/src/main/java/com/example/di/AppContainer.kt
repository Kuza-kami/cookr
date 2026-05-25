package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.CookrDatabase
import com.example.data.repository.CookrRepositoryImpl
import com.example.domain.repository.CookrRepository

interface AppContainer {
    val cookrRepository: CookrRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: CookrDatabase by lazy {
        Room.databaseBuilder(context, CookrDatabase::class.java, "cookr_database_v2")
            .fallbackToDestructiveMigration()
            .build()
    }

    override val cookrRepository: CookrRepository by lazy {
        CookrRepositoryImpl(database.cookrDao())
    }
}
