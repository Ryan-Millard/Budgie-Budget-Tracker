package com.example.budgiebudgettracking.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgiebudgettracking.entities.User
import com.example.budgiebudgettracking.dao.UserDao

@Database(entities = [User::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

	abstract fun userDao(): UserDao

	companion object {
		@Volatile 
		private var INSTANCE: AppDatabase? = null

		fun getDatabase(context: Context): AppDatabase =
			INSTANCE ?: synchronized(this) {
				INSTANCE ?: Room.databaseBuilder(
					context.applicationContext,
					AppDatabase::class.java, "budgie_db"
				)
				.addMigrations(MIGRATION_1_2)
				.build().also { INSTANCE = it }
			}

		// Migration from version 1 to 2: Add new columns for phone, location, bio, and profilePic.
		private val MIGRATION_1_2 = object : Migration(1, 2) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT")
				database.execSQL("ALTER TABLE users ADD COLUMN location TEXT")
				database.execSQL("ALTER TABLE users ADD COLUMN bio TEXT")
				database.execSQL("ALTER TABLE users ADD COLUMN profilePic TEXT")
			}
		}
	}
}
