package com.example.budgiebudgettracking

import android.widget.Toast
import android.os.Bundle
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Locale

import com.example.budgiebudgettracking.components.NavigationSelector
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel
import com.example.budgiebudgettracking.entities.TransactionWithCategory
import com.google.android.material.radiobutton.MaterialRadioButton

class ExpenseActivity : BaseActivity(), FloatingActionButtonHandler {

	private lateinit var viewModel: TransactionViewModel
	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: TransactionAdapter
	private lateinit var monthNavigator: NavigationSelector
	private lateinit var filterGroup: RadioGroup

	// Current filter state
	private var selectedStartMs: Long = 0L
	private var selectedEndMs: Long = 0L
	private enum class Mode { ALL, ONE_OFF, RECURRING }
	private var currentMode = Mode.ALL

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_expense)
		createAndAttachFab(destination = AddExpenseActivity::class.java)

		// --- Find & set up views ---
		recyclerView   = findViewById(R.id.transactionsRecyclerView)
		monthNavigator = findViewById(R.id.month_selector)
		filterGroup    = findViewById(R.id.rg_recurring_filter)

		recyclerView.layoutManager = LinearLayoutManager(this)

		// Initialize ViewModel first so we can safely use it in the adapter
		viewModel = ViewModelProvider(this, TransactionViewModel.Factory(application))
		.get(TransactionViewModel::class.java)

		// Setup adapter with fixed click listener
		adapter = TransactionAdapter(emptyList(), object : TransactionAdapter.OnItemClickListener {
			override fun onItemClick(item: TransactionWithCategory) {
				// Verify transaction has a valid ID before showing bottom sheet
				if (item.transaction.id <= 0) {
					Toast.makeText(this@ExpenseActivity, "Invalid transaction ID", Toast.LENGTH_SHORT).show()
					return
				}

				try {
					// Show a more helpful toast
					Toast.makeText(
						this@ExpenseActivity, 
						"Opening transaction details...", 
						Toast.LENGTH_SHORT
					).show()
					Toast.makeText(this@ExpenseActivity, "${item.transaction.id}", Toast.LENGTH_SHORT).show()

					// Create and show the bottom sheet with proper error handling
					val bottomSheet = TransactionDetailBottomSheet.newInstance(item.transaction.id)
					bottomSheet.show(supportFragmentManager, null)
				} catch (e: Exception) {
					// Handle exceptions that might occur
					Toast.makeText(
						this@ExpenseActivity,
						"Error showing details: ${e.message}",
						Toast.LENGTH_SHORT
					).show()
				}
			}
		})

		recyclerView.adapter = adapter

		setupMonthNavigator()
		setupFilterListener()

		// initial query
		refreshTransactions()
	}

	private fun setupMonthNavigator() {
		val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
		val year = LocalDate.now().year
		monthNavigator.setItems((1..12).map { m -> YearMonth.of(year, m).format(fmt) })

		val idx = Month.from(LocalDate.now()).value - 1
		monthNavigator.setCurrentPosition(idx)
		computeDatesForPosition(idx)

		monthNavigator.setOnNavigationChangeListener(object :
		NavigationSelector.OnNavigationChangeListener {
			override fun onNavigationChanged(position: Int, value: String) {
				computeDatesForPosition(position)
				refreshTransactions()
			}
		})
	}

	private fun setupFilterListener() {
		filterGroup.setOnCheckedChangeListener { _, checkedId ->
			currentMode = when (checkedId) {
				R.id.rb_one_off -> Mode.ONE_OFF
				R.id.rb_recurring -> Mode.RECURRING
				else -> Mode.ALL
			}
			refreshTransactions()
		}
	}

	private fun computeDatesForPosition(position: Int) {
		val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
		val label = monthNavigator.getCurrentItem()!!
		val ym = YearMonth.parse(label, fmt)

		selectedStartMs = ym.atDay(1)
		.atStartOfDay(ZoneId.systemDefault())
		.toInstant().toEpochMilli()

		selectedEndMs = ym.plusMonths(1)
		.atDay(1)
		.atStartOfDay(ZoneId.systemDefault())
		.toInstant().toEpochMilli()
	}

	private fun refreshTransactions() {
		val live = when (currentMode) {
			Mode.ALL -> viewModel.getTransactionsWithCategoryByDateRange(
				selectedStartMs, selectedEndMs
			)
			Mode.ONE_OFF -> viewModel.getTransactionsByDateAndRecurring(
				selectedStartMs, selectedEndMs, isRecurring = false
			)
			Mode.RECURRING -> viewModel.getTransactionsByDateAndRecurring(
				selectedStartMs, selectedEndMs, isRecurring = true
			)
		} // ← Now the when wraps all three cases

		live.observe(this) { list ->
			adapter.updateData(list ?: emptyList())
		}
	}
}
