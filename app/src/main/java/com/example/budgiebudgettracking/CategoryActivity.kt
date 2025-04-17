package com.example.budgiebudgettracking

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryActivity : BaseActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_category)

		// Hardcoded list of expense categories
		val categoryList = listOf(
			"Groceries",
			"Transport",
			"Entertainment",
			"Utilities",
			"Healthcare",
			"Dining Out",
			"Shopping",
			"Travel"
		)

		// Initialize RecyclerView
		val recyclerView: RecyclerView = findViewById(R.id.categoryRecyclerView)
		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.adapter = CategoryAdapter(categoryList)
	}
}
