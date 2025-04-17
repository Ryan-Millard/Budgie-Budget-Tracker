package com.example.budgiebudgettracking.dao

import androidx.room.*
import com.example.budgiebudgettracking.entities.Category

@Dao
interface CategoryDao {
	@Insert
	suspend fun insert(category: Category)

	@Update
	suspend fun update(category: Category)

	@Delete
	suspend fun delete(category: Category)

	@Query("SELECT * FROM categories WHERE userId = :userId OR userId IS NULL")
	suspend fun getCategoriesByUser(userId: Int?): List<Category>

	@Query("SELECT * FROM categories WHERE id = :categoryId")
	suspend fun getCategoryById(categoryId: Int): Category?

	@Query("SELECT * FROM categories WHERE categoryName = :name AND userId IS NULL")
    suspend fun getCategoryByName(name: String): Category?
}
