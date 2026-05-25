package com.example.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val prepTime: String,
    val cookTime: String,
    val servings: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val imageUrl: String,
    val category: String,
    val isSaved: Boolean = false,
    val rating: Float = 4.5f,
    val calories: Int = 350,
    val protein: Int = 20,
    val carbs: Int = 40,
    val fat: Int = 10,
    val author: String = "Chef AI",
    val isUserSubmitted: Boolean = false,
    val photoRank: Int = 0
)

data class CalorieLog(
    val id: String,
    val foodName: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val timestamp: Long
)

data class GroceryItem(
    val id: String,
    val name: String,
    val amount: Double,
    val unit: String,
    val isCompleted: Boolean = false
)
