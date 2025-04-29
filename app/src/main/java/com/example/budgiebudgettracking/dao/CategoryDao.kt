package com.example.budgiebudgettracking.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.budgiebudgettracking.entities.Category

@Dao
interface CategoryDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(category: Category)

	@Update
	suspend fun update(category: Category)

	@Delete
	suspend fun delete(category: Category)

	@Query("SELECT * FROM categories WHERE userId = :userId OR userId IS NULL")
	fun getCategoriesByUserLive(userId: Int?): LiveData<List<Category>>

	@Query("SELECT * FROM categories WHERE id = :categoryId")
	suspend fun getCategoryById(categoryId: Int): Category?

	@Query("SELECT * FROM categories WHERE categoryName = :name AND userId IS NULL")
    suspend fun getCategoryByName(name: String): Category?

	@Query(
		"SELECT DISTINCT c.* FROM categories c"
				+ " JOIN transactions t ON c.id = t.categoryId"
				+ " WHERE t.transactionDate BETWEEN :startTs AND :endTs"
				+ " AND c.userId = :userId"
	)
	fun getCategoriesForMonth(userId: Int, startTs: Long, endTs: Long): LiveData<List<Category>>
}
