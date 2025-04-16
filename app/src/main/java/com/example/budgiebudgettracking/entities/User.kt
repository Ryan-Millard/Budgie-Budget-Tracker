package com.example.budgiebudgettracking.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val fullName: String,
	val email: String,
	val password: String,

	// Non-imperative fields
	val phone: String? = null,
	val location: String? = null,
	val bio: String? = null,
	val profilePic: String? = null
)

