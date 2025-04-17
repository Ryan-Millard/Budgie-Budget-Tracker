package com.example.budgiebudgettracking.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgiebudgettracking.entities.*
import com.example.budgiebudgettracking.dao.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [User::class, Category::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

	abstract fun userDao(): UserDao
	abstract fun categoryDao(): CategoryDao

	companion object {
		@Volatile
		private var INSTANCE: AppDatabase? = null

		fun getDatabase(context: Context): AppDatabase =
		INSTANCE ?: synchronized(this) {
			INSTANCE ?: Room.databaseBuilder(
				context.applicationContext,
				AppDatabase::class.java, "budgie_db"
			)
			.addCallback(DatabaseCallback(context))
			.addMigrations(MIGRATION_1_2)
			.build()
			.also { INSTANCE = it }
		}

		private val MIGRATION_1_2 = object : Migration(1, 2) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT")
				database.execSQL("ALTER TABLE users ADD COLUMN location TEXT")
				database.execSQL("ALTER TABLE users ADD COLUMN bio TEXT")
				database.execSQL("ALTER TABLE users ADD COLUMN profilePic TEXT")
			}
		}

		private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
			override fun onCreate(db: SupportSQLiteDatabase) {
				super.onCreate(db)

				// Run insertion in the background using IO dispatcher
				GlobalScope.launch(Dispatchers.IO) {
					val instance = getDatabase(context)
					val defaultCategories = listOf(
						Category(
							categoryName = "Groceries",
							description = "Everyday food and household supplies",
							hexColorCode = "#4CAF50" // Green
						),
						Category(
							categoryName = "Transport",
							description = "Public transport, fuel, and commuting",
							hexColorCode = "#2196F3" // Blue
						),
						Category(
							categoryName = "Entertainment",
							description = "Movies, games, and fun activities",
							hexColorCode = "#9C27B0" // Purple
						),
						Category(
							categoryName = "Utilities",
							description = "Electricity, water, internet, etc.",
							hexColorCode = "#FF9800" // Orange
						),
						Category(
							categoryName = "Healthcare",
							description = "Doctor visits, medicine, and insurance",
							hexColorCode = "#F44336" // Red
						),
						Category(
							categoryName = "Dining Out",
							description = "Restaurants, takeout, and coffee shops",
							hexColorCode = "#FF5722" // Deep Orange
						),
						Category(
							categoryName = "Shopping",
							description = "Clothes, gadgets, and other purchases",
							hexColorCode = "#795548" // Brown
						),
						Category(
							categoryName = "Travel",
							description = "Flights, hotels, and holiday expenses",
							hexColorCode = "#3F51B5" // Indigo
						)
					)

					// Insert using suspend function properly
					defaultCategories.forEach {
						instance.categoryDao().insert(it)
					}
				}
			}
		}
	}
}
