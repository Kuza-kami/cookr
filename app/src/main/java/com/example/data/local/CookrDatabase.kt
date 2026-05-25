package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RecipeEntity::class, CalorieLogEntity::class, GroceryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CookrDatabase : RoomDatabase() {
    abstract fun cookrDao(): CookrDao
}
