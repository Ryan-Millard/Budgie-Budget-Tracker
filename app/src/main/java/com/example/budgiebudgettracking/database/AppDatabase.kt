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

@Database(
	entities = [User::class, Category::class, Transaction::class],
	version = 4,
	exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao
	abstract fun categoryDao(): CategoryDao
	abstract fun transactionDao(): TransactionDao

	companion object {
		@Volatile private var INSTANCE: AppDatabase? = null

		fun getDatabase(context: Context): AppDatabase =
		INSTANCE ?: synchronized(this) {
			INSTANCE ?: Room.databaseBuilder(
				context.applicationContext,
				AppDatabase::class.java, "budgie_db"
			)
			.addCallback(DatabaseCallback(context))
			.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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

		private val MIGRATION_2_3 = object : Migration(2, 3) {
			override fun migrate(database: SupportSQLiteDatabase) {
				// Create the transactions table
				database.execSQL(
					"""
					CREATE TABLE IF NOT EXISTS transactions (
						id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
						amount REAL NOT NULL,
						description TEXT,
						isExpense INTEGER NOT NULL,
						date INTEGER NOT NULL,
						userId INTEGER NOT NULL,
						categoryId INTEGER NOT NULL,
						receiptImagePath TEXT,
						isRecurring INTEGER NOT NULL DEFAULT 0,
						createdAt INTEGER NOT NULL,
						updatedAt INTEGER NOT NULL,
						FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
						FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL
					)
					"""
				)

				// Create indices for better query performance
				database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_userId ON transactions(userId)")
				database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
			}
		}

		// New migration: 3 → 4
		private val MIGRATION_3_4 = object : Migration(3, 4) {
			override fun migrate(db: SupportSQLiteDatabase) {
				// 1) Add startTime and endTime columns to transactions
				db.execSQL("ALTER TABLE transactions ADD COLUMN startTime INTEGER NOT NULL DEFAULT 0")
				db.execSQL("ALTER TABLE transactions ADD COLUMN endTime   INTEGER NOT NULL DEFAULT 0")
				db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)") // Speed up ordering by date
			}
		}

		private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
			override fun onCreate(db: SupportSQLiteDatabase) {
				super.onCreate(db)
				GlobalScope.launch(Dispatchers.IO) {
					// pre-populate default categories
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
					defaultCategories.forEach {
						instance.categoryDao().insert(it)
					}
				}
			}
		}
	}
}
