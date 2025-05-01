package com.example.budgiebudgettracking

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
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import kotlin.math.abs

class MonthlyBudgetActivity : BaseActivity(), FloatingActionButtonHandler {

	private lateinit var monthlyBudgetViewModel: MonthlyBudgetViewModel
	private lateinit var transactionViewModel: TransactionViewModel

	private lateinit var addGoalLauncher: ActivityResultLauncher<Intent>

	private lateinit var sessionManager: SessionManager

	// UI Components
	private lateinit var spentValue: TextView
	private lateinit var remainingValue: TextView
	private lateinit var progressBar: ProgressBar
	private lateinit var minValue: TextView
	private lateinit var maxValue: TextView
	private lateinit var recycler: RecyclerView
	private lateinit var adapter: MonthlyGoalsAdapter

	// Hold the "source of truth" for max and spent:
	private var currentMaxValue: Double? = null
	private var amountSpentInCurrentMonth: Double? = null
		set(value) {
			field = value?.let { abs(it) }
		}

	private val currencyFormatter by lazy {
		NumberFormat.getCurrencyInstance(Locale.getDefault())
	}

	private val yearMonth: String by lazy {
		SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
	}

	private var userId: Int = -1

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_monthly_budget)

		addGoalLauncher = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult()
		) { result ->
			if (result.resultCode == Activity.RESULT_OK) {
				val data = result.data ?: return@registerForActivityResult
				val newYearMonth = data.getStringExtra("NEW_YEAR_MONTH")!!
				val newMin = data.getDoubleExtra("NEW_MIN_GOAL", 0.0)
				val newMax = data.getDoubleExtra("NEW_MAX_GOAL", 0.0)

				// 1) Reload the current month’s goal from the database:
				monthlyBudgetViewModel.loadGoalForMonth(userId, newYearMonth)

				// 2) (Optionally) show a toast or update some UI immediately:
				Toast.makeText(
					this,
					"Saved goals for $newYearMonth: min=${currencyFormatter.format(newMin)}, max=${currencyFormatter.format(newMax)}",
					Toast.LENGTH_SHORT
				).show()
			}
		}

		createAndAttachFab(
			destination = AddMonthlyBudgetActivity::class.java,
			onClickFun = {
				val intent = Intent(this, AddMonthlyBudgetActivity::class.java)
				addGoalLauncher.launch(intent)
			}
		)

		// Initialize UI
		spentValue = findViewById(R.id.spentValue)
		remainingValue = findViewById(R.id.remainingValue)
		progressBar = findViewById(R.id.budgetProgressBar)
		minValue = findViewById(R.id.minValue)
		maxValue = findViewById(R.id.maxValue)
		recycler = findViewById(R.id.goalsRecyclerView)

		adapter = MonthlyGoalsAdapter()
		recycler.layoutManager = LinearLayoutManager(this)
		recycler.adapter = adapter

		sessionManager = SessionManager.getInstance(this)

		monthlyBudgetViewModel = ViewModelProvider(
			this,
			MonthlyBudgetViewModel.Factory(application)
		).get(MonthlyBudgetViewModel::class.java)

		transactionViewModel = ViewModelProvider(
			this,
			TransactionViewModel.Factory(application)
		).get(TransactionViewModel::class.java)

		// Load user & then data
		val email = sessionManager.getUserEmail()
		if (email.isBlank()) {
			Toast.makeText(this, "No user logged in", Toast.LENGTH_LONG).show()
			finish(); return
		}

		monthlyBudgetViewModel.loadUserByEmail(email) { user ->
			if (user == null) {
				Toast.makeText(this, "Unknown user", Toast.LENGTH_LONG).show()
				finish()
			} else {
				userId = user.id
				setupObservers()
				monthlyBudgetViewModel.loadGoalForMonth(userId, yearMonth)
			}
		}
	}

	private fun setupObservers() {
		// When the monthly goal (min/max) loads:
		monthlyBudgetViewModel.currentMonthGoal.observe(this) { goal ->
			currentMaxValue = goal?.maxGoal ?: 0.0
			// update the min/max labels:
			minValue.text = currencyFormatter.format(goal?.minGoal ?: 0.0)
			maxValue.text = currencyFormatter.format(currentMaxValue!!)
			// and then refresh the UI:
			refreshBudgetUI()
		}

		// Observe all goals list
		monthlyBudgetViewModel.allGoals.observe(this) { goals ->
			adapter.submitList(goals)
		}

		// Observe save result
		monthlyBudgetViewModel.operationResult.observe(this) { success ->
			Toast.makeText(
				this,
				if (success) "Saved!" else "Error saving goal",
				Toast.LENGTH_SHORT
			).show()
			monthlyBudgetViewModel.loadGoalForMonth(userId, yearMonth)
		}

		// When monthly expenses arrive:
		transactionViewModel.monthlyExpenses.observe(this) { spent ->
			amountSpentInCurrentMonth = spent
			refreshBudgetUI()
		}
	}

	/** Call anytime `currentMaxValue` or `amountSpentInCurrentMonth` changes */
	private fun refreshBudgetUI() {
		val max   = currentMaxValue ?: return
		val spent = amountSpentInCurrentMonth ?: return

		// Compute remaining, clamped at 0…max:
		val remaining = (max - spent).coerceAtLeast(0.0)

		// Update the views:
		spentValue.text     = currencyFormatter.format(spent)
		remainingValue.text = currencyFormatter.format(remaining)

		// Sync the progress bar:
		progressBar.max      = max.toInt().coerceAtLeast(0)
		progressBar.progress = spent.toInt().coerceIn(0, progressBar.max)
	}
}
