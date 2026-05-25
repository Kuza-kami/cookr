package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val prepTime: String,
    val cookTime: String,
    val servings: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val imageUrl: String,
    val category: String,
    val isSaved: Boolean,
    val rating: Float,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val author: String,
    val isUserSubmitted: Boolean,
    val photoRank: Int
)

@Entity(tableName = "calorie_logs")
data class CalorieLogEntity(
    @PrimaryKey val id: String,
    val foodName: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val timestamp: Long
)

@Entity(tableName = "groceries")
data class GroceryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val unit: String,
    val isCompleted: Boolean
)
