package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.RecipeEntity
import com.example.data.models.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isUserCreated = 1 OR userRating > 0 OR length(userNotes) > 0 OR isFavorite = 1 ORDER BY createdAt DESC")
    fun getJournalRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    @Query("UPDATE recipes SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE recipes SET userRating = :rating, userNotes = :notes WHERE id = :id")
    suspend fun updateRatingAndNotes(id: Long, rating: Int, notes: String)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, id DESC")
    fun getAllItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("UPDATE shopping_items SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchasedState(id: Long, isPurchased: Boolean)

    @Query("DELETE FROM shopping_items WHERE isPurchased = 1")
    suspend fun clearCompleted()

    @Query("SELECT COUNT(*) FROM shopping_items")
    suspend fun getCount(): Int
}
