package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CookrDao {
    // --- Recipes ---
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isSaved = 1 ORDER BY id DESC")
    fun getSavedRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: String): Flow<RecipeEntity?>

    @Query("""
        SELECT * FROM recipes 
        WHERE title LIKE :query 
           OR description LIKE :query 
           OR category LIKE :query 
           OR author LIKE :query 
           OR ingredients LIKE :query 
           OR instructions LIKE :query
        ORDER BY id DESC
    """)
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: String)

    // --- Calorie Logs ---
    @Query("SELECT * FROM calorie_logs ORDER BY timestamp DESC")
    fun getAllCalorieLogs(): Flow<List<CalorieLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalorieLog(log: CalorieLogEntity)

    @Query("DELETE FROM calorie_logs WHERE id = :id")
    suspend fun deleteCalorieLog(id: String)

    @Query("DELETE FROM calorie_logs")
    suspend fun clearCalorieLogs()

    // --- Groceries ---
    @Query("SELECT * FROM groceries ORDER BY id DESC")
    fun getAllGroceries(): Flow<List<GroceryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryEntity)

    @Query("DELETE FROM groceries WHERE id = :id")
    suspend fun deleteGroceryItem(id: String)

    @Query("DELETE FROM groceries")
    suspend fun clearGroceries()
}
