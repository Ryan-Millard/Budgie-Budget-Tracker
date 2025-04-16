package com.example.budgiebudgettracking.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.budgiebudgettracking.entities.User
import com.example.budgiebudgettracking.dao.UserDao

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

	abstract fun userDao(): UserDao

	companion object {
		@Volatile private var INSTANCE: AppDatabase? = null

		fun getDatabase(context: Context): AppDatabase =
		INSTANCE ?: synchronized(this) {
			INSTANCE ?: Room.databaseBuilder(
				context.applicationContext,
				AppDatabase::class.java, "budgie_db"
			).build().also { INSTANCE = it }
		}
	}
}
