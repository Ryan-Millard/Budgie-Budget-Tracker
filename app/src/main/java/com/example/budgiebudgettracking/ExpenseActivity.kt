package com.example.budgiebudgettracking

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Locale
import com.google.android.material.button.MaterialButton
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.activity.result.ActivityResult
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.NumberFormat
import java.util.Currency

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
	
	// Summary views
	private lateinit var tvTotalAmount: TextView
	private lateinit var tvTransactionCount: TextView

	// Current filter state
	private lateinit var btnCategory: MaterialButton
	private lateinit var categoryFilterSwitch: SwitchMaterial
	private var selectedCategoryId: Int = -1 // Default category
	private var selectedStartMs: Long = 0L
	private var selectedEndMs: Long = 0L
	private enum class Mode { ALL, ONE_OFF, RECURRING }
	private var currentMode = Mode.ALL

	// Activity result launcher for category selection
	private val categoryPickerLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			result.data?.let { data ->
				selectedCategoryId = data.getIntExtra("CATEGORY_ID", 1)
				val categoryName = data.getStringExtra("CATEGORY_NAME") ?: "General"
				btnCategory.text = categoryName
				refreshTransactions()  // Refresh transactions right after category selection
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_expense)
		createAndAttachFab(destination = AddExpenseActivity::class.java)

		val header = findViewById<LinearLayout>(R.id.filter_header)
		val content = findViewById<LinearLayout>(R.id.filter_content)
		val arrow = findViewById<ImageView>(R.id.filter_arrow)

		header.setOnClickListener {
			val expanded = content.visibility == View.VISIBLE
			content.visibility = if (expanded) View.GONE else View.VISIBLE
			arrow.animate().rotation(if (expanded) 0f else 90f).setDuration(200).start()
		}

		// --- Find & set up views ---
		recyclerView = findViewById(R.id.transactionsRecyclerView)
		monthNavigator = findViewById(R.id.month_selector)
		filterGroup = findViewById(R.id.rg_recurring_filter)
		btnCategory = findViewById(R.id.btnCategory)
		categoryFilterSwitch = findViewById(R.id.categoryFilterSwitch)
		
		// Initialize summary views
		tvTotalAmount = findViewById(R.id.tv_total_amount)
		tvTransactionCount = findViewById(R.id.tv_transaction_count)

		recyclerView.layoutManager = LinearLayoutManager(this)

		// Initialize ViewModel first so we can safely use it in the adapter
		viewModel = ViewModelProvider(this, TransactionViewModel.Factory(application))
			.get(TransactionViewModel::class.java)

		// Set up category button
		btnCategory.setOnClickListener {
			openCategoryPicker()
		}

		// Set up category filter toggle
		categoryFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
			btnCategory.isEnabled = isChecked
			
			if (!isChecked) {
				// Reset category filter when turned off
				selectedCategoryId = -1
				btnCategory.text = "Category"
			}
			
			refreshTransactions()
		}

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
	
	/**
	 * Format amount as currency
	 */
	private fun formatCurrency(amount: Double): String {
		val format = NumberFormat.getCurrencyInstance()
		format.currency = Currency.getInstance(Locale.getDefault())
		return format.format(amount)
	}
	
	/**
	 * Calculate and display transaction summary
	 */
	private fun updateTransactionSummary(transactions: List<TransactionWithCategory>) {
		// Calculate total amount
		val totalAmount = transactions.sumOf { it.transaction.amount }
		
		// Update UI
		tvTotalAmount.text = formatCurrency(totalAmount)
		tvTransactionCount.text = transactions.size.toString()
	}

	private fun refreshTransactions() {
		// First get transactions based on the selected mode (ALL, ONE_OFF, RECURRING)
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
		}

		live.observe(this) { list ->
			// Filter list by selected category if a category is selected and filter is enabled
			val filteredList = if (categoryFilterSwitch.isChecked && selectedCategoryId != -1) {
				list?.filter { it?.category?.id == selectedCategoryId } ?: emptyList()
			} else {
				list ?: emptyList()
			}
			
			// Update the transaction summary
			updateTransactionSummary(filteredList)
			
			// Update the adapter data
			adapter.updateData(filteredList)
		}
	}

	private fun openCategoryPicker() {
		val intent = Intent(this, CategoryPickerActivity::class.java)
		intent.putExtra("CURRENT_CATEGORY_ID", selectedCategoryId)
		categoryPickerLauncher.launch(intent)
	}
}
