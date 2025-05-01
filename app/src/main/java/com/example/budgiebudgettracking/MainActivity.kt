package com.example.budgiebudgettracking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.budgiebudgettracking.BaseActivity
import com.example.budgiebudgettracking.utils.SessionManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.core.content.ContextCompat




class MainActivity : BaseActivity() {
	private lateinit var sessionManager: SessionManager
	private lateinit var lineChart: LineChart


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// Initialize session manager
		sessionManager = SessionManager.getInstance(applicationContext)

		// Check if the user is logged in
		if(!sessionManager.isLoggedIn()) {
			startActivity(Intent(this, LoginActivity::class.java))
			finish()
		}
		// Initialize LineChart
		lineChart = findViewById(R.id.lineChart)

		// Set up the chart data
		setupChartData()
	}
	private fun setupChartData() {
		// Example data for the line chart (Jan-May)
		val months = listOf("Jan", "Feb", "Mar", "Apr", "May")
		val incomeData = listOf(1000f, 1200f, 1500f, 1800f, 2000f)  // Example income data
		val expenseData = listOf(800f, 900f, 1000f, 1100f, 1200f)  // Example expense data

		// Create entries for the income and expense datasets
		val incomeEntries = ArrayList<Entry>()
		val expenseEntries = ArrayList<Entry>()

		for (i in months.indices) {
			incomeEntries.add(Entry(i.toFloat(), incomeData[i]))
			expenseEntries.add(Entry(i.toFloat(), expenseData[i]))
		}

		// Create the LineDataSets
		val incomeDataSet = LineDataSet(incomeEntries, "Income")
		incomeDataSet.color = resources.getColor(R.color.colorPrimary)  // Teal/Green
		incomeDataSet.valueTextColor = resources.getColor(R.color.colorTextPrimary)


		val expenseDataSet = LineDataSet(expenseEntries, "Expenses")
		expenseDataSet.color = resources.getColor(R.color.colorError)  // Coral Red
		expenseDataSet.valueTextColor = resources.getColor(R.color.colorTextPrimary)

		val lineDataSetIncome = LineDataSet(incomeEntries, "Income")
		lineDataSetIncome.color = ContextCompat.getColor(this, R.color.colorPrimary)
		lineDataSetIncome.lineWidth = 8f //
		lineDataSetIncome.circleRadius = 5f

		val lineDataSetExpenses = LineDataSet(expenseEntries, "Expenses")
		lineDataSetExpenses.color = ContextCompat.getColor(this, R.color.colorAccent)
		lineDataSetExpenses.lineWidth = 8f //
		lineDataSetExpenses.circleRadius = 5f

		// Create LineData with both datasets
		val lineData = LineData(incomeDataSet, expenseDataSet)

		// Set the LineData to the chart
		lineChart.data = lineData

		// Customize the X-axis with months as labels
		val xAxis = lineChart.xAxis
		xAxis.valueFormatter = IndexAxisValueFormatter(months)

		// Set other chart settings
		lineChart.description.isEnabled = false
		lineChart.legend.isEnabled = true

		lineChart.invalidate()  // Refresh the chart
	}
}
