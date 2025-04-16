package com.example.budgiebudgettracking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		findViewById<Button>(R.id.btnLogin).setOnClickListener {
			startActivity(Intent(this, LoginActivity::class.java))
		}

		findViewById<Button>(R.id.btnRegister).setOnClickListener {
			startActivity(Intent(this, RegisterActivity::class.java))
		}

		findViewById<Button>(R.id.btnCategory).setOnClickListener {
			startActivity(Intent(this, CategoryActivity::class.java))
		}

		findViewById<Button>(R.id.btnExpense).setOnClickListener {
			startActivity(Intent(this, ExpenseActivity::class.java))
		}

		findViewById<Button>(R.id.btnMonthlyBudget).setOnClickListener {
			startActivity(Intent(this, MonthlyBudgetActivity::class.java))
		}
	}
}
