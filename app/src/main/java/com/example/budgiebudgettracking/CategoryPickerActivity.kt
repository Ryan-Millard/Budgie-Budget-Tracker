package com.example.budgiebudgettracking

import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity

import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.dao.CategoryDao

class CategoryPickerActivity : AppCompatActivity(), FloatingActionButtonHandler {
	private lateinit var db: AppDatabase
	private lateinit var adapter: CategoryAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_category)

		Toast.makeText(this, "1", Toast.LENGTH_SHORT).show()
		createAndAttachFab(destination = AddCategoryActivity::class.java)
		Toast.makeText(this, "2", Toast.LENGTH_SHORT).show()

		// 1) Get DB instance
		db = AppDatabase.getDatabase(this)

		// 2) Prepare RecyclerView + adapter
		val recyclerView: RecyclerView = findViewById(R.id.categoryRecyclerView)
		recyclerView.layoutManager = LinearLayoutManager(this)
		adapter = CategoryAdapter()
		adapter.setOnItemClickListener { category ->
			val resultIntent = Intent()
			resultIntent.putExtra("CATEGORY_ID", category.id)
			resultIntent.putExtra("CATEGORY_NAME", category.categoryName)
			setResult(Activity.RESULT_OK, resultIntent)
			finish()
		}
		recyclerView.adapter = adapter

		// 3) Load categories from DB
		loadCategories()
	}

	private fun loadCategories() {
		db.categoryDao().getCategoriesByUserLive(null).observe(this) {
				adapter.updateCategories(it)
			}
		}
	}
