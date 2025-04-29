package com.example.budgiebudgettracking

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.dao.CategoryDao

class CategoryActivity : BaseActivity(), FloatingActionButtonHandler {
	private lateinit var db: AppDatabase
	private lateinit var adapter: CategoryAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_category)

		createAndAttachFab(destination = AddCategoryActivity::class.java)

		// 1) Get DB instance
		db = AppDatabase.getDatabase(this)

		// 2) Prepare RecyclerView + adapter
		val recyclerView: RecyclerView = findViewById(R.id.categoryRecyclerView)
		recyclerView.layoutManager = LinearLayoutManager(this)
		adapter = CategoryAdapter()
		recyclerView.adapter = adapter

		// 3) Load categories from DB
		loadCategories()
		val etFilter = findViewById<EditText>(R.id.filterMonth)
		val btnFilter = findViewById<Button>(R.id.btnFilter)
		btnFilter.setOnClickListener {
			val ym = etFilter.text.toString().trim()
			viewModel.getByMonth(userId, ym).observe(this) { list ->
				adapter.submitList(list)
			}
		}
	}

	private fun loadCategories() {
		db.categoryDao().getCategoriesByUserLive(null).observe(this) {
				adapter.updateCategories(it)
			}
		}
	}
