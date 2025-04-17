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
						Category(categoryName = "Groceries"),
						Category(categoryName = "Transport"),
						Category(categoryName = "Entertainment"),
						Category(categoryName = "Utilities"),
						Category(categoryName = "Healthcare"),
						Category(categoryName = "Dining Out"),
						Category(categoryName = "Shopping"),
						Category(categoryName = "Travel")
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
