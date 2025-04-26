package com.example.budgiebudgettracking

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

import com.example.budgiebudgettracking.components.NavigationSelector
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

		val monthNavigator = findViewById<NavigationSelector>(R.id.month_selector)

		// Populate with items
		// 2.1 Create a formatter for "FullMonthName Year", e.g. "April 2025"
		val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
		// 2.2 Get the current year
		val currentYear = LocalDate.now().year
		// 2.3 Build a List<String> of all 12 months in that year
		val monthsWithYear = (1..12).map { monthIndex ->
			YearMonth.of(currentYear, monthIndex).format(formatter)
		}
		// 2.4 Supply to your selector
		monthNavigator.setItems(monthsWithYear)

		val currentMonth = Month.from(LocalDate.now()).value - 1
		monthNavigator.setCurrentPosition(currentMonth)

		// Set a listener to handle navigation changes
		monthNavigator.setOnNavigationChangeListener(object :
		NavigationSelector.OnNavigationChangeListener {
			override fun onNavigationChanged(position: Int, value: String) {
				// Parse "April 2025" back into a YearMonth
				val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
				val ym = YearMonth.parse(value, formatter)

				// Start = first day of month at 00:00
				val startDate = ym.atDay(1)
				.atStartOfDay(ZoneId.systemDefault())
				.toInstant().toEpochMilli()

				// End = first day of next month at 00:00
				val endDate = ym.plusMonths(1)
				.atDay(1)
				.atStartOfDay(ZoneId.systemDefault())
				.toInstant().toEpochMilli()

				// Observe the filtered LiveData
				viewModel.getTransactionsWithCategoryByDateRange(startDate, endDate)
				.observe(this@ExpenseActivity) { filteredList ->
					adapter.updateData(filteredList)
				}
			}
		})
	}
}
