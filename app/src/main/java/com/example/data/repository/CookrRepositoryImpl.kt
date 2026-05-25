package com.example.data.repository

import com.example.data.local.CookrDao
import com.example.data.local.RecipeEntity
import com.example.data.local.CalorieLogEntity
import com.example.data.local.GroceryEntity
import com.example.domain.model.Recipe
import com.example.domain.model.CalorieLog
import com.example.domain.model.GroceryItem
import com.example.domain.repository.CookrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CookrRepositoryImpl(
    private val cookrDao: CookrDao
) : CookrRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> {
        return cookrDao.getAllRecipes().map { list -> list.map { it.toDomain() } }
    }

    override fun getSavedRecipes(): Flow<List<Recipe>> {
        return cookrDao.getSavedRecipes().map { list -> list.map { it.toDomain() } }
    }

    override fun getRecipeById(id: String): Flow<Recipe?> {
        return cookrDao.getRecipeById(id).map { it?.toDomain() }
    }

    override suspend fun saveRecipe(recipe: Recipe) {
        cookrDao.insertRecipe(recipe.toEntity())
    }

    override suspend fun saveRecipes(recipes: List<Recipe>) {
        cookrDao.insertRecipes(recipes.map { it.toEntity() })
    }

    override suspend fun deleteRecipe(id: String) {
        cookrDao.deleteRecipe(id)
    }

    override fun getAllCalorieLogs(): Flow<List<CalorieLog>> {
        return cookrDao.getAllCalorieLogs().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveCalorieLog(log: CalorieLog) {
        cookrDao.insertCalorieLog(log.toEntity())
    }

    override suspend fun deleteCalorieLog(id: String) {
        cookrDao.deleteCalorieLog(id)
    }

    override suspend fun clearCalorieLogs() {
        cookrDao.clearCalorieLogs()
    }

    override fun getAllGroceries(): Flow<List<GroceryItem>> {
        return cookrDao.getAllGroceries().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveGroceryItem(item: GroceryItem) {
        cookrDao.insertGroceryItem(item.toEntity())
    }

    override suspend fun deleteGroceryItem(id: String) {
        cookrDao.deleteGroceryItem(id)
    }

    override suspend fun clearGroceries() {
        cookrDao.clearGroceries()
    }
}

// --- Mappers ---

fun RecipeEntity.toDomain() = Recipe(
    id = id,
    title = title,
    description = description,
    prepTime = prepTime,
    cookTime = cookTime,
    servings = servings,
    ingredients = ingredients,
    instructions = instructions,
    imageUrl = imageUrl,
    category = category,
    isSaved = isSaved,
    rating = rating,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    author = author,
    isUserSubmitted = isUserSubmitted,
    photoRank = photoRank
)

fun Recipe.toEntity() = RecipeEntity(
    id = id,
    title = title,
    description = description,
    prepTime = prepTime,
    cookTime = cookTime,
    servings = servings,
    ingredients = ingredients,
    instructions = instructions,
    imageUrl = imageUrl,
    category = category,
    isSaved = isSaved,
    rating = rating,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    author = author,
    isUserSubmitted = isUserSubmitted,
    photoRank = photoRank
)

fun CalorieLogEntity.toDomain() = CalorieLog(
    id = id,
    foodName = foodName,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    timestamp = timestamp
)

fun CalorieLog.toEntity() = CalorieLogEntity(
    id = id,
    foodName = foodName,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    timestamp = timestamp
)

fun GroceryEntity.toDomain() = GroceryItem(
    id = id,
    name = name,
    amount = amount,
    unit = unit,
    isCompleted = isCompleted
)

fun GroceryItem.toEntity() = GroceryEntity(
    id = id,
    name = name,
    amount = amount,
    unit = unit,
    isCompleted = isCompleted
)
