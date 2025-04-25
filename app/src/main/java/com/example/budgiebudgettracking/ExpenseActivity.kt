package com.example.budgiebudgettracking

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel
import com.example.budgiebudgettracking.entities.TransactionWithCategory

class ExpenseActivity : BaseActivity(), FloatingActionButtonHandler {
	private lateinit var viewModel: TransactionViewModel
	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: TransactionAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_expense)
		createAndAttachFab(destination = AddExpenseActivity::class.java)

		recyclerView = findViewById(R.id.transactionsRecyclerView)
		recyclerView.layoutManager = LinearLayoutManager(this)
		adapter = TransactionAdapter(emptyList())
		recyclerView.adapter = adapter

		viewModel = ViewModelProvider(this, TransactionViewModel.Factory(application))
		.get(TransactionViewModel::class.java)
		viewModel.allWithCategory.observe(this) { list ->
			adapter.updateData(list)
		}
	}
}
