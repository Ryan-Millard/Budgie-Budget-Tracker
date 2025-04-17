package com.example.budgiebudgettracking

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import android.graphics.drawable.GradientDrawable

import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.Category

class AddCategoryActivity : AppCompatActivity() {
	private lateinit var categoryNameInput: EditText
	private lateinit var categoryDescriptionInput: EditText
	private lateinit var categoryColorInput: EditText
	private lateinit var categoryIconInput: EditText
	private lateinit var saveButton: Button
	private lateinit var cancelButton: Button
	private lateinit var colorPickerButton: ImageButton
	private lateinit var db: AppDatabase

	// Default color for the color picker button
	private var selectedColor: Int? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_category)

		// Initialize database
		db = AppDatabase.getDatabase(this)

		// Initialize views
		categoryNameInput = findViewById(R.id.categoryNameInput)
		categoryDescriptionInput = findViewById(R.id.categoryDescriptionInput)
		categoryColorInput = findViewById(R.id.categoryColorInput)
		categoryIconInput = findViewById(R.id.categoryIconInput)
		saveButton = findViewById(R.id.saveButton)
		cancelButton = findViewById(R.id.cancelButton)
		colorPickerButton = findViewById(R.id.colorPickerButton)

		// Setup action bar
		supportActionBar?.apply {
			title = "Add Category"
			setDisplayHomeAsUpEnabled(true)
		}

		// Initialize color picker button with default color
		updateColorPickerButton(selectedColor)

		// Set color picker button click listener
		colorPickerButton.setOnClickListener {
			showColorPickerDialog()
		}
		// Make the EditText also trigger the color picker when clicked
		categoryColorInput.setOnClickListener {
			showColorPickerDialog()
		}

		// Set click listeners for buttons
		saveButton.setOnClickListener {
			saveCategory()
		}

		cancelButton.setOnClickListener {
			finish()
		}
	}

	private fun updateColorPickerButton(color: Int?) {
		val drawable = GradientDrawable().apply {
			shape = GradientDrawable.OVAL
			setColor(color ?: Color.TRANSPARENT) // Transparent if no color
			setStroke(2, Color.DKGRAY)
		}

		colorPickerButton.background = drawable
		colorPickerButton.invalidate()
		colorPickerButton.requestLayout()
	}

	// Helper function to determine if a color is dark
	private fun isColorDark(color: Int): Boolean {
		val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
		return darkness >= 0.5
	}

	private fun showColorPickerDialog() {
		ColorPickerDialog.Builder(this)
		.setTitle("Choose Color")
		.setPreferenceName("ColorPickerDialog")
		.setPositiveButton("Select", ColorEnvelopeListener { envelope, fromUser ->
			// Get the selected color
			selectedColor = envelope.color
			val hexColor = "#" + envelope.hexCode

			// Update the color input field
			categoryColorInput.setText(hexColor)

			// Update the color picker button
			updateColorPickerButton(selectedColor)
		})
		.setNegativeButton("Cancel") { dialogInterface, _ ->
			dialogInterface.dismiss()
		}
		.attachAlphaSlideBar(true)
		.attachBrightnessSlideBar(true)
		.setBottomSpace(12)
		.show()
	}

	private fun saveCategory() {
		val categoryName = categoryNameInput.text.toString().trim()
		val categoryDescription = categoryDescriptionInput.text.toString().trim()
		val categoryColor = categoryColorInput.text.toString().trim()
		val categoryIcon = categoryIconInput.text.toString().trim()

		// Validate input
		if (categoryName.isEmpty()) {
			categoryNameInput.error = "Category name is required"
			return
		}

		// Check if category already exists
		lifecycleScope.launch {
			val existingCategory = db.categoryDao().getCategoryByName(categoryName)
			if (existingCategory != null) {
				runOnUiThread {
					Toast.makeText(
						this@AddCategoryActivity,
						"Category with this name already exists",
						Toast.LENGTH_SHORT
					).show()
				}
				return@launch
			}

			// Create new category
			val newCategory = Category(
				categoryName = categoryName,
				description = categoryDescription.ifEmpty { null },
				hexColorCode = categoryColor.ifEmpty { null },
				icon = categoryIcon.ifEmpty { null }
				// userId is null by default, making it a system-wide category
			)

			// Insert the category
			db.categoryDao().insert(newCategory)

			// Confirm and finish
			runOnUiThread {
				Toast.makeText(
					this@AddCategoryActivity,
					"Category added successfully",
					Toast.LENGTH_SHORT
				).show()
				finish()
			}
		}
	}

	override fun onSupportNavigateUp(): Boolean {
		finish()
		return true
	}
}
