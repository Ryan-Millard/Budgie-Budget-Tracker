package com.example.budgiebudgettracking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.core.widget.doAfterTextChanged
import com.example.budgiebudgettracking.BaseActivity
import com.example.budgiebudgettracking.entities.MonthlyGoal
import com.example.budgiebudgettracking.utils.SessionManager
import com.example.budgiebudgettracking.viewmodels.MonthlyGoalViewModel

class MonthlyBudgetActivity : BaseActivity() {
	private lateinit var viewModel: MonthlyGoalViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_monthly_budget)

		viewModel = ViewModelProvider(this).get(MonthlyGoalViewModel::class.java)

		val etYearMonth = findViewById<EditText>(R.id.editYearMonth)
		val etMin = findViewById<EditText>(R.id.editMinGoal)
		val etMax = findViewById<EditText>(R.id.editMaxGoal)
		val btnUpdate = findViewById<Button>(R.id.updateBudgetButton)
		val userId = SessionManager(this).getUserId()

		btnUpdate.setOnClickListener {
			val ym = etYearMonth.text.toString().trim()
			val minVal = etMin.text.toString().toDoubleOrNull() ?: 0.0
			val maxVal = etMax.text.toString().toDoubleOrNull() ?: 0.0
			val goal = MonthlyGoal(
				userId = userId,
				yearMonth = ym,
				minGoal = minVal,
				maxGoal = maxVal
			)
			viewModel.upsert(goal)
			Toast.makeText(this, "Monthly goal saved", Toast.LENGTH_SHORT).show()
		}

		// Pre-fill if exists
		etYearMonth.doAfterTextChanged { text ->
			val ym = text.toString().trim()
			if (ym.matches(Regex("\d{4}-\d{2}"))) {
				viewModel.getGoal(ym, userId).observe(this) { existing ->
					existing?.let {
						etMin.setText(it.minGoal.toString())
						etMax.setText(it.maxGoal.toString())
					}
				}
			}
		}
	}
}