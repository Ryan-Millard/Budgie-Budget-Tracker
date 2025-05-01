package com.example.budgiebudgettracking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgiebudgettracking.entities.MonthlyGoal
import com.example.budgiebudgettracking.utils.SessionManager
import com.example.budgiebudgettracking.viewmodels.MonthlyBudgetViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MonthlyBudgetActivity : BaseActivity(), FloatingActionButtonHandler {

	private lateinit var viewModel: MonthlyBudgetViewModel
	private lateinit var sessionManager: SessionManager

	// UI Components
	private lateinit var spentValue: TextView
	private lateinit var remainingValue: TextView
	private lateinit var progressBar: ProgressBar
	private lateinit var minValue: TextView
	private lateinit var maxValue: TextView
	private lateinit var recycler: RecyclerView
	private lateinit var adapter: MonthlyGoalsAdapter

	private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
	private val yearMonth: String by lazy {
		SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
	}
	private var userId: Int = -1

	private val addBudgetLauncher =
	registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			viewModel.loadGoalForMonth(userId, yearMonth)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_monthly_budget)

		createAndAttachFab(destination = AddMonthlyBudgetActivity::class.java)

		// Initialize UI components
		spentValue = findViewById(R.id.spentValue)
		remainingValue = findViewById(R.id.remainingValue)
		progressBar = findViewById(R.id.budgetProgressBar)
		minValue = findViewById(R.id.minValue)
		maxValue = findViewById(R.id.maxValue)
		recycler = findViewById(R.id.goalsRecyclerView)

		// Set up RecyclerView
		adapter = MonthlyGoalsAdapter()
		recycler.layoutManager = LinearLayoutManager(this)
		recycler.adapter = adapter

		// Initialize SessionManager and ViewModel
		sessionManager = SessionManager.getInstance(this)
		viewModel = ViewModelProvider(
			this,
			MonthlyBudgetViewModel.Factory(application)
		).get(MonthlyBudgetViewModel::class.java)

		// Load user and observe data
		val email = sessionManager.getUserEmail()
		if (email.isNotEmpty()) {
			viewModel.loadUserByEmail(email) { user ->
				if (user == null) {
					Toast.makeText(this, "Unknown user", Toast.LENGTH_LONG).show()
					finish()
				} else {
					userId = user.id
					setupObservers()
					viewModel.loadGoalForMonth(userId, yearMonth)
				}
			}
		} else {
			Toast.makeText(this, "No user logged in", Toast.LENGTH_LONG).show()
			finish()
		}
	}

	private fun setupObservers() {
		// Observe current month's goal
		viewModel.currentMonthGoal.observe(this) { goal ->
			val min = goal?.minGoal ?: 0.0
			val max = goal?.maxGoal ?: 0.0
			minValue.text = currencyFormatter.format(min)
			maxValue.text = currencyFormatter.format(max)

			// Update progress bar and remaining/spent values
			// val spent = goal?.spentAmount ?: 0.0
			// val remaining = max - spent
			// spentValue.text = currencyFormatter.format(spent)
			// remainingValue.text = currencyFormatter.format(remaining)
			// progressBar.max = max.toInt()
			// progressBar.progress = spent.toInt()
		}

		// Observe all goals
		viewModel.allGoals.observe(this) { goals ->
			adapter.submitList(goals)
		}

		// Observe operation result
		viewModel.operationResult.observe(this) { success ->
			Toast.makeText(
				this,
				if (success) "Saved!" else "Error saving goal",
				Toast.LENGTH_SHORT
			).show()
			viewModel.loadGoalForMonth(userId, yearMonth)
		}
	}
}
