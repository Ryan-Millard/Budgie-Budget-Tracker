package com.example.budgiebudgettracking

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel

class ExpenseActivity : BaseActivity(), FloatingActionButtonHandler {
	private lateinit var viewModel: TransactionViewModel
	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: TransactionAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_expense)

		createAndAttachFab(destination = AddExpenseActivity::class.java)

		viewModel = ViewModelProvider(
			this,
			TransactionViewModel.Factory(application)
		).get(TransactionViewModel::class.java)

		recyclerView = findViewById(R.id.transactionsRecyclerView)
		adapter = TransactionAdapter(emptyList())

		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.adapter = adapter

		viewModel.allTransactions.observe(this) { txs ->
			adapter.updateData(txs)
		}
	}
}
