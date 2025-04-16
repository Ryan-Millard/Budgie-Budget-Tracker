package com.example.budgiebudgettracking.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.example.budgiebudgettracking.entities.User

@Dao
interface UserDao {

	@Insert(onConflict = OnConflictStrategy.ABORT)
	suspend fun insertUser(user: User)

	@Query("SELECT * FROM users WHERE email = :email LIMIT 1")
	suspend fun getUserByEmail(email: String): User?

	@Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
	suspend fun login(email: String, password: String): User?
}
