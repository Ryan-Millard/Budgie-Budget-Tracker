package com.example.budgiebudgettracking

import java.time.LocalDate
import java.time.ZoneId
import com.example.budgiebudgettracking.components.NavigationSelector
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.example.budgiebudgettracking.components.CalculatorView
import com.example.budgiebudgettracking.entities.MonthlyGoal
import com.example.budgiebudgettracking.viewmodels.MonthlyBudgetViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddMonthlyBudgetActivity : AppCompatActivity() {

	private var currentGoalId: Int = -1

	private lateinit var etMinAmount: TextInputEditText
	private lateinit var etMaxAmount: TextInputEditText
	private var selectedMinAmount: Double? = null
	private var selectedMaxAmount: Double? = null

	private lateinit var btnSave: MaterialButton
	private lateinit var viewModel: MonthlyBudgetViewModel

	private var selectedAmount: Double? = null

	private lateinit var monthNavigator: NavigationSelector

	private var selectedStartMs: Long = 0
	private var selectedEndMs: Long = 0
	private lateinit var selectedYearMonth: YearMonth

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_monthly_budget)

		// bind UI
		etMinAmount = findViewById(R.id.etMinAmount)
		val calculatorViewMin = findViewById<CalculatorView>(R.id.calculatorViewMin)

		etMaxAmount = findViewById(R.id.etMaxAmount)
		val calculatorViewMax = findViewById<CalculatorView>(R.id.calculatorViewMax)

		btnSave        = findViewById(R.id.btnSave)
		monthNavigator = findViewById(R.id.month_selector)

		// disable direct input on amount, use calculator
		etMinAmount.isFocusable = false
		etMinAmount.isClickable = true
		etMaxAmount.isFocusable = false
		etMaxAmount.isClickable = true

		// init ViewModel
		viewModel = ViewModelProvider(
			this,
			MonthlyBudgetViewModel.Factory(application)
		).get(MonthlyBudgetViewModel::class.java)

		// set up calculator callbacks
		calculatorViewMin.setCalculatorListener(object : CalculatorView.CalculatorListener {
			override fun onCalculationResult(result: Double, displayText: String) {
				selectedMinAmount = result
				runOnUiThread { etMinAmount.setText(displayText) }
			}
		})

		calculatorViewMax.setCalculatorListener(object : CalculatorView.CalculatorListener {
			override fun onCalculationResult(result: Double, displayText: String) {
				selectedMaxAmount = result
				runOnUiThread { etMaxAmount.setText(displayText) }
			}
		})

		// set up month selector
		setupMonthNavigator()

		// handle save
		btnSave.setOnClickListener {
			val min = selectedMinAmount
			val max = selectedMaxAmount

			if (min == null || max == null || min < 0.0 || max < 0.0) {
				Snackbar.make(btnSave, "Please calculate and select positive amounts for both goals", Snackbar.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			if (min > max) {
				Snackbar.make(btnSave, "Minimum goal cannot exceed maximum goal", Snackbar.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			val formattedYearMonth = selectedYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault()))

			lifecycleScope.launch(Dispatchers.IO) {
				val goal = MonthlyGoal(
					id = if (currentGoalId == -1) 0 else currentGoalId,
					userId = viewModel.getUserId()!!,
					yearMonth = formattedYearMonth,
					minGoal = min,
					maxGoal = max
				)
				viewModel.upsertGoal(goal)
			}

			viewModel.operationResult.observe(this) { success ->
				if (success) {
					val data = Intent().apply {
						putExtra("NEW_YEAR_MONTH", formattedYearMonth)
						putExtra("NEW_MIN_GOAL", min)
						putExtra("NEW_MAX_GOAL", max)
					}
					setResult(Activity.RESULT_OK, data)
					finish()
				} else {
					Snackbar.make(btnSave, "Failed to save goal. Try again.", Snackbar.LENGTH_LONG).show()
				}
			}
		}
	}

	private fun setupMonthNavigator() {
		val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
		val year = LocalDate.now().year
		val months = (1..12).map { m -> YearMonth.of(year, m).format(fmt) }

		monthNavigator.setItems(months)

		val idx = LocalDate.now().monthValue - 1
		monthNavigator.setCurrentPosition(idx)
		computeDatesForPosition(idx)

		monthNavigator.setOnNavigationChangeListener(object : NavigationSelector.OnNavigationChangeListener {
			override fun onNavigationChanged(position: Int, value: String) {
				computeDatesForPosition(position)
				refreshTransactions()
			}
		})
	}

	private fun computeDatesForPosition(position: Int) {
		val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
		val label = monthNavigator.getCurrentItem() ?: return
		selectedYearMonth = try {
			YearMonth.parse(label, fmt)
		} catch (e: Exception) {
			YearMonth.now() // fallback
		}

		selectedStartMs = selectedYearMonth.atDay(1)
			.atStartOfDay(ZoneId.systemDefault())
			.toInstant().toEpochMilli()

		selectedEndMs = selectedYearMonth.plusMonths(1)
			.atDay(1)
			.atStartOfDay(ZoneId.systemDefault())
			.toInstant().toEpochMilli()
	}

	private fun refreshTransactions() {
		// no-op placeholder if needed
	}
}
