package com.example.budgiebudgettracking.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.example.budgiebudgettracking.R
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private var calculatedResult: Double = 0.0
	private var openParenCount: Int = 0
	private var calculatorInputField: EditText

	// Interface to communicate with the parent
	interface CalculatorListener {
		fun onCalculationResult(result: Double, displayText: String)
	}

	private var listener: CalculatorListener? = null

	init {
		val view = LayoutInflater.from(context).inflate(R.layout.view_calculator, this, true)
		calculatorInputField = view.findViewById(R.id.calculatorInput)

		// Set up number buttons
		for (i in 0..9) {
			val btnId = resources.getIdentifier("btn$i", "id", context.packageName)
			view.findViewById<Button>(btnId)?.setOnClickListener {
				calculatorInputField.append(i.toString())
			}
		}

		// Set up operator buttons
		view.findViewById<Button>(R.id.btnPlus).setOnClickListener { calculatorInputField.append("+") }
		view.findViewById<Button>(R.id.btnMinus).setOnClickListener { calculatorInputField.append("-") }
		view.findViewById<Button>(R.id.btnMultiply).setOnClickListener { calculatorInputField.append("*") }
		view.findViewById<Button>(R.id.btnDivide).setOnClickListener { calculatorInputField.append("/") }
		view.findViewById<Button>(R.id.btnDot).setOnClickListener { calculatorInputField.append(".") }

		// Functional buttons
		view.findViewById<Button>(R.id.btnClear).setOnClickListener {
			calculatorInputField.text.clear()
			openParenCount = 0
		}
		view.findViewById<Button>(R.id.btnParen).setOnClickListener { handleParentheses() }
		view.findViewById<Button>(R.id.btnPercent).setOnClickListener { onPercent() }
		view.findViewById<Button>(R.id.btnNegate).setOnClickListener { onToggleSign() }
		view.findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateExpression() }

		// Watch for manual text changes
		calculatorInputField.doAfterTextChanged { text ->
			// You could add validation logic here if needed
		}
	}

	// Public methods

	fun setCalculatorListener(listener: CalculatorListener) {
		this.listener = listener
	}

	fun setDisplayText(text: String) {
		calculatorInputField.setText(text)
		calculatorInputField.setSelection(text.length)
	}

	fun getDisplayText(): String {
		return calculatorInputField.text.toString()
	}

	fun getCurrentResult(): Double {
		return calculatedResult
	}

	fun calculateExpression() {
		val expr = calculatorInputField.text.toString()
		if (expr.isBlank()) return

		try {
			// Auto-close any open parentheses
			var finalExpr = expr
			repeat(openParenCount) { finalExpr += ")" }

			// Replace display symbols with operators the library understands
			finalExpr = finalExpr.replace("×", "*").replace("÷", "/").replace("±", "-")

			val expression = ExpressionBuilder(finalExpr).build()
			calculatedResult = expression.evaluate()

			// Display neatly (no trailing .0 if integer)
			val resultText = if (calculatedResult % 1.0 == 0.0)
			calculatedResult.toLong().toString()
			else
			calculatedResult.toString()

			calculatorInputField.setText(resultText)
			calculatorInputField.setSelection(resultText.length)
			openParenCount = 0 // Reset after calculation

			// Notify the listener
			listener?.onCalculationResult(calculatedResult, resultText)

		} catch (e: Exception) {
			Toast.makeText(context, "Error in calculation", Toast.LENGTH_SHORT).show()
		}
	}

	// Private helper methods

	private fun handleParentheses() {
		val currentText = calculatorInputField.text.toString()
		val cursorPosition = calculatorInputField.selectionStart

		if (openParenCount > 0) {
			// Check if we should add a closing parenthesis
			val textBeforeCursor = currentText.substring(0, cursorPosition)
			val lastChar = if (textBeforeCursor.isNotEmpty()) textBeforeCursor.last() else ' '

			if (lastChar !in listOf('+', '-', '*', '/', '(', ' ')) {
				calculatorInputField.text.insert(cursorPosition, ")")
				openParenCount--
			} else {
				calculatorInputField.text.insert(cursorPosition, "(")
				openParenCount++
			}
		} else {
			// Just add an opening parenthesis
			calculatorInputField.text.insert(cursorPosition, "(")
			openParenCount++
		}
	}

	private fun onPercent() {
		val currentText = calculatorInputField.text.toString()
		if (currentText.isEmpty()) return

		try {
			// Replace display symbols first
			val expr = currentText.replace("×", "*").replace("÷", "/").replace("±", "-")

			// Try to evaluate the current expression
			val expression = ExpressionBuilder(expr).build()
			val value = expression.evaluate()
			calculatedResult = value / 100.0

			val resultText = if (calculatedResult % 1.0 == 0.0)
			calculatedResult.toLong().toString()
			else
			calculatedResult.toString()

			calculatorInputField.setText(resultText)
			calculatorInputField.setSelection(resultText.length)

			// Notify the listener
			listener?.onCalculationResult(calculatedResult, resultText)

		} catch (e: Exception) {
			Toast.makeText(context, "Error calculating percentage", Toast.LENGTH_SHORT).show()
		}
	}

	private fun onToggleSign() {
		val currentText = calculatorInputField.text.toString()
		if (currentText.isEmpty()) return

		try {
			// Replace display symbols first
			val expr = currentText.replace("×", "*").replace("÷", "/").replace("±", "-")

			// Try to evaluate the current expression
			val expression = ExpressionBuilder(expr).build()
			calculatedResult = -expression.evaluate()

			// Display neatly
			val resultText = if (calculatedResult % 1.0 == 0.0)
			calculatedResult.toLong().toString()
			else
			calculatedResult.toString()

			calculatorInputField.setText(resultText)
			calculatorInputField.setSelection(resultText.length)

			// Notify the listener
			listener?.onCalculationResult(calculatedResult, resultText)

		} catch (e: Exception) {
			// If evaluation fails, just add a negative sign at the beginning
			if (currentText.startsWith("-")) {
				calculatorInputField.setText(currentText.substring(1))
			} else {
				calculatorInputField.setText("-$currentText")
			}
			calculatorInputField.setSelection(calculatorInputField.text.length)
		}
	}

	fun deleteLastCharacter() {
		val currentText = calculatorInputField.text.toString()
		val cursorPosition = calculatorInputField.selectionStart

		if (currentText.isNotEmpty() && cursorPosition > 0) {
			// Check if we're deleting a closing parenthesis
			if (currentText[cursorPosition - 1] == ')') {
				openParenCount++
			}
			// Check if we're deleting an opening parenthesis
			else if (currentText[cursorPosition - 1] == '(') {
				openParenCount--
			}
			// Delete the character
			calculatorInputField.text.delete(cursorPosition - 1, cursorPosition)
		}
	}
}
