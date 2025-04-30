package com.example.budgiebudgettracking

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetBudgetActivity : AppCompatActivity() {

    private lateinit var minBudgetEditText: EditText
    private lateinit var maxBudgetEditText: EditText
    private lateinit var updateButton: Button

    private val PREFS_NAME = "BudgetPrefs"
    private val MIN_BUDGET_KEY = "min_budget"
    private val MAX_BUDGET_KEY = "max_budget"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        minBudgetEditText = findViewById(R.id.editTextMinBudget)
        maxBudgetEditText = findViewById(R.id.editTextMaxBudget)
        updateButton = findViewById(R.id.buttonUpdateBudget)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        minBudgetEditText.setText(prefs.getFloat(MIN_BUDGET_KEY, 0f).toString())
        maxBudgetEditText.setText(prefs.getFloat(MAX_BUDGET_KEY, 0f).toString())

        updateButton.setOnClickListener {
            val min = minBudgetEditText.text.toString().toFloatOrNull()
            val max = maxBudgetEditText.text.toString().toFloatOrNull()

            if (min == null || max == null || min < 0 || max < 0 || min > max) {
                Toast.makeText(this, "Please enter valid budget values.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit()
                .putFloat(MIN_BUDGET_KEY, min)
                .putFloat(MAX_BUDGET_KEY, max)
                .apply()

            Toast.makeText(this, "Budget updated successfully.", Toast.LENGTH_SHORT).show()
        }
    }
}