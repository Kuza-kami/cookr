package com.example.domain.repository

import com.example.domain.model.Recipe
import com.example.domain.model.CalorieLog
import com.example.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow

interface CookrRepository {
    // Recipes
    fun getAllRecipes(): Flow<List<Recipe>>
    fun getSavedRecipes(): Flow<List<Recipe>>
    fun getRecipeById(id: String): Flow<Recipe?>
    fun searchRecipes(query: String): Flow<List<Recipe>>
    suspend fun saveRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: String)
    suspend fun saveRecipes(recipes: List<Recipe>)

    // Calorie Logs
    fun getAllCalorieLogs(): Flow<List<CalorieLog>>
    suspend fun saveCalorieLog(log: CalorieLog)
    suspend fun deleteCalorieLog(id: String)
    suspend fun clearCalorieLogs()

    // Groceries
    fun getAllGroceries(): Flow<List<GroceryItem>>
    suspend fun saveGroceryItem(item: GroceryItem)
    suspend fun deleteGroceryItem(id: String)
    suspend fun clearGroceries()
}
