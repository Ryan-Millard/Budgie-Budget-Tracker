package com.example.budgiebudgettracking

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import net.objecthunter.exp4j.ExpressionBuilder
import com.bumptech.glide.Glide
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseCalculatorActivity : AppCompatActivity() {
	private lateinit var input: EditText
	private lateinit var descriptionInput: EditText
	private lateinit var calculatorGrid: View
	private lateinit var toggleCalculatorButton: Button
	private var calculatorVisible = false
	private var calculatedResult: Double = 0.0
	private var openParenCount = 0

	// Image related variables
	private lateinit var receiptImageView: ImageView
	private lateinit var placeholderImageView: ImageView
	private var currentPhotoUri: Uri? = null
	private var imageSelected = false

	// Permission request launchers
	private val requestCameraPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted ->
		if (isGranted) {
			openCamera()
		} else {
			Toast.makeText(this, "Camera permission is needed to take photos", Toast.LENGTH_LONG).show()
		}
	}

	private val requestGalleryPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted ->
		if (isGranted) {
			openGallery()
		} else {
			Toast.makeText(this, "Storage permission is needed to select photos", Toast.LENGTH_LONG).show()
		}
	}

	// Multiple permission launcher for Android 13+
	private val requestMultiplePermissionsLauncher = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissions ->
		if (permissions[Manifest.permission.CAMERA] == true) {
			openCamera()
		} else {
			Toast.makeText(this, "Camera permission is needed to take photos", Toast.LENGTH_LONG).show()
		}
	}

	// Activity result launcher for gallery
	private val pickImageLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			result.data?.data?.let { uri ->
				loadImageWithGlide(uri)
				currentPhotoUri = uri
				imageSelected = true
				placeholderImageView.visibility = View.GONE
			}
		}
	}

	// Activity result launcher for camera
	private val takePictureLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			currentPhotoUri?.let { uri ->
				loadImageWithGlide(uri)
				imageSelected = true
				placeholderImageView.visibility = View.GONE
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_expense_calculator)

		input = findViewById(R.id.inputAmount)
		descriptionInput = findViewById(R.id.inputDescription)
		calculatorGrid = findViewById(R.id.calculatorGrid)
		toggleCalculatorButton = findViewById(R.id.btnToggleCalculator)
		receiptImageView = findViewById(R.id.receiptImageView)
		placeholderImageView = findViewById(R.id.placeholderImageView)

		// Set up image click listeners for placeholder and user's image
		placeholderImageView.setOnClickListener {
			showImagePickerOptions()
		}
		receiptImageView.setOnClickListener {
			showImagePickerOptions()
		}

		// Set up amount field to show calculator
		input.setOnClickListener {
			showCalculator()
		}

		// Set up toggle calculator button
		toggleCalculatorButton.setOnClickListener {
			toggleCalculator()
		}

		// Set up delete button
		findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
			deleteLastCharacter()
		}

		// Set up number buttons (0-9)
		for (i in 0..9) {
			val buttonId = resources.getIdentifier("btn$i", "id", packageName)
			findViewById<Button>(buttonId).setOnClickListener {
				input.append(i.toString())
			}
		}

		// Set up operator buttons individually
		findViewById<Button>(R.id.btnPlus).setOnClickListener { input.append("+") }
		findViewById<Button>(R.id.btnMinus).setOnClickListener { input.append("-") }
		findViewById<Button>(R.id.btnMultiply).setOnClickListener { input.append("*") }
		findViewById<Button>(R.id.btnDivide).setOnClickListener { input.append("/") }
		findViewById<Button>(R.id.btnDot).setOnClickListener { input.append(".") }

		// Percentage button (%)
		findViewById<Button>(R.id.btnPercent).setOnClickListener { 
			onPercent()
		}

		// Parentheses button
		findViewById<Button>(R.id.btnParen).setOnClickListener {
			handleParentheses()
		}

		// Clear button
		findViewById<Button>(R.id.btnClear).setOnClickListener {
			input.text.clear()
			openParenCount = 0
		}

		// Negate button
		findViewById<Button>(R.id.btnNegate).setOnClickListener {
			onToggleSign()
		}

		// Equals button
		findViewById<Button>(R.id.btnEquals).setOnClickListener {
			calculateExpression()
		}

		// Confirm button
		findViewById<Button>(R.id.confirmAmountBtn).setOnClickListener {
			// Save transaction with image if available
			saveTransaction()
		}
	}

	private fun showImagePickerOptions() {
		val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
		val builder = androidx.appcompat.app.AlertDialog.Builder(this)
		builder.setTitle("Add Receipt Image")

		builder.setItems(options) { dialog, which ->
			when (which) {
				0 -> checkCameraPermissionAndOpen()
				1 -> checkStoragePermissionAndOpenGallery()
				2 -> dialog.dismiss()
			}
		}
		builder.show()
	}

	private fun checkCameraPermissionAndOpen() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			// For Android 13+, request camera permission
			requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
		} else {
			// For older Android versions
			if (ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.CAMERA
			) != PackageManager.PERMISSION_GRANTED
		) {
			requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
		} else {
			openCamera()
		}
	}
}

private fun checkStoragePermissionAndOpenGallery() {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		// For Android 13+, request media images permission
		if (ContextCompat.checkSelfPermission(
			this,
			Manifest.permission.READ_MEDIA_IMAGES
		) != PackageManager.PERMISSION_GRANTED
	) {
		requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
	} else {
		openGallery()
	}
} else {
	// For older Android versions
	if (ContextCompat.checkSelfPermission(
		this,
		Manifest.permission.READ_EXTERNAL_STORAGE
	) != PackageManager.PERMISSION_GRANTED
) {
	requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
} else {
	openGallery()
}
		}
	}

	private fun openCamera() {
		try {
			val photoFile = createImageFile()
			val photoURI = FileProvider.getUriForFile(
				this,
				"${applicationContext.packageName}.fileprovider",
				photoFile
			)
			currentPhotoUri = photoURI

			val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

			// Verify that the intent will resolve to an activity
			if (takePictureIntent.resolveActivity(packageManager) != null) {
				takePictureLauncher.launch(takePictureIntent)
			} else {
				Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
			}
		} catch (ex: IOException) {
			Toast.makeText(this, "Error creating image file: ${ex.message}", Toast.LENGTH_SHORT).show()
		} catch (ex: Exception) {
			Toast.makeText(this, "Error launching camera: ${ex.message}", Toast.LENGTH_SHORT).show()
		}
	}

	private fun openGallery() {
		val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
		try {
			pickImageLauncher.launch(intent)
		} catch (ex: Exception) {
			Toast.makeText(this, "Error opening gallery: ${ex.message}", Toast.LENGTH_SHORT).show()
		}
	}

	@Throws(IOException::class)
	private fun createImageFile(): File {
		val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
		val imageFileName = "JPEG_${timeStamp}_"
		val storageDir = getExternalFilesDir(null) ?: throw IOException("External storage not available")
		return File.createTempFile(imageFileName, ".jpg", storageDir)
	}

	private fun loadImageWithGlide(uri: Uri) {
		try {
			Glide.with(this)
			.load(uri)
			.centerCrop()
			.error(R.drawable.ic_add_photo)
			.into(receiptImageView)
		} catch (e: Exception) {
			Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
		}
	}

	private fun showCalculator() {
		if (!calculatorVisible) {
			calculatorGrid.visibility = View.VISIBLE
			toggleCalculatorButton.text = "Hide Calculator"
			calculatorVisible = true
		}
	}

	private fun toggleCalculator() {
		if (calculatorVisible) {
			calculatorGrid.visibility = View.GONE
			toggleCalculatorButton.text = "Show Calculator"
			calculatorVisible = false
		} else {
			calculatorGrid.visibility = View.VISIBLE
			toggleCalculatorButton.text = "Hide Calculator"
			calculatorVisible = true
		}
	}

	private fun saveTransaction() {
		if (input.text.isBlank()) {
			Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
			return
		}

		val description = descriptionInput.text.toString()
		if (description.isBlank()) {
			Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
			return
		}

		// Get the amount
		val amount = if (calculatedResult != 0.0) {
			calculatedResult
		} else {
			try {
				input.text.toString().toDouble()
			} catch (e: NumberFormatException) {
				Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
				return
			}
		}

		// Get transaction type
		val isExpense = findViewById<android.widget.RadioButton>(R.id.radioExpense).isChecked

		// Here you would save to your database
		// Including the image URI if available
		val imagePath = currentPhotoUri?.toString()

		// Just showing a toast for demonstration
		val message = if (imagePath != null) {
			"Saved ${if (isExpense) "expense" else "income"} of $amount for '$description' with receipt image"
		} else {
			"Saved ${if (isExpense) "expense" else "income"} of $amount for '$description' without receipt image"
		}

		Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

		// Optionally send result back to calling activity
		val resultIntent = Intent()
		resultIntent.putExtra("AMOUNT", amount)
		resultIntent.putExtra("DESCRIPTION", description)
		resultIntent.putExtra("IS_EXPENSE", isExpense)
		resultIntent.putExtra("IMAGE_URI", imagePath)
		setResult(Activity.RESULT_OK, resultIntent)
		finish()
	}

	private fun deleteLastCharacter() {
		val currentText = input.text.toString()
		val cursorPosition = input.selectionStart
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
			input.text.delete(cursorPosition - 1, cursorPosition)
		}
	}

	private fun handleParentheses() {
		val currentText = input.text.toString()
		val cursorPosition = input.selectionStart
		if (openParenCount > 0) {
			// Check if we should add a closing parenthesis
			val textBeforeCursor = currentText.substring(0, cursorPosition)
			val lastChar = if (textBeforeCursor.isNotEmpty()) textBeforeCursor.last() else ' '
			if (lastChar !in listOf('+', '-', '*', '/', '(', ' ')) {
				input.text.insert(cursorPosition, ")")
				openParenCount--
			} else {
				input.text.insert(cursorPosition, "(")
				openParenCount++
			}
		} else {
			// Just add an opening parenthesis
			input.text.insert(cursorPosition, "(")
			openParenCount++
		}
	}

	private fun calculateExpression() {
		val expr = input.text.toString()
		if (expr.isBlank()) return
		try {
			// Auto-close any open parentheses
			var finalExpr = expr
			repeat(openParenCount) { finalExpr += ")" }
			// Replace display symbols with operators the library understands
			finalExpr = finalExpr.replace("×", "*").replace("÷", "/").replace("−", "-")
			val expression = ExpressionBuilder(finalExpr).build()
			calculatedResult = expression.evaluate()
			// Display neatly (no trailing .0 if integer)
			val resultText = if (calculatedResult % 1.0 == 0.0)
			calculatedResult.toLong().toString()
			else
			calculatedResult.toString()
			input.setText(resultText)
			input.setSelection(resultText.length)
			openParenCount = 0 // Reset after calculation
		} catch (e: Exception) {
			input.setText("Error")
			input.setSelection(input.text.length)
		}
	}

	private fun onPercent() {
		val currentText = input.text.toString()
		if (currentText.isEmpty()) return
		try {
			// Replace display symbols first
			val expr = currentText.replace("×", "*").replace("÷", "/").replace("−", "-")
			// Try to evaluate the current expression
			val expression = ExpressionBuilder(expr).build()
			val value = expression.evaluate()
			calculatedResult = value / 100.0
			val resultText = if (calculatedResult % 1.0 == 0.0)
			calculatedResult.toLong().toString()
			else
			calculatedResult.toString()
			input.setText(resultText)
			input.setSelection(resultText.length)
		} catch (e: Exception) {
			// Just append % if we can't evaluate yet
			input.append("%")
		}
	}

	private fun onToggleSign() {
		val currentText = input.text.toString()
		if (currentText.isEmpty()) return
		val cursorPosition = input.selectionStart
		// Find the boundaries of the current number or expression
		var startPos = cursorPosition
		var endPos = cursorPosition
		// Find the start of the current number
		while (startPos > 0) {
			val prevChar = currentText[startPos - 1]
			// Stop if we hit an operator (but not if it's part of a number like -5)
			if (prevChar in "+-/*()") {
				// Special case: keep the minus sign with the number if it's a negative number
				if (prevChar == '-' && (startPos == 1 || currentText[startPos - 2] in "+-/*(")) {
					startPos--
				}
				break
			}
			startPos--
		}
		// Find the end of the current number
		while (endPos < currentText.length && currentText[endPos] !in "+-/*()" && currentText[endPos].isDigit()) {
			endPos++
		}
		// If we've identified a number or part of the expression
		if (startPos < endPos) {
			val segment = currentText.substring(startPos, endPos)
			val before = currentText.substring(0, startPos)
			val after = currentText.substring(endPos)
			// Toggle the sign
			val newSegment = if (segment.startsWith("-")) {
				segment.substring(1)
			} else {
				"-$segment"
			}
			// Update the text
			input.setText(before + newSegment + after)
			// Place cursor at the end of the modified number
			input.setSelection(startPos + newSegment.length)
		} else {
			// Fallback: toggle sign for the entire input
			if (currentText.startsWith("-")) {
				input.setText(currentText.substring(1))
			} else {
				input.setText("-$currentText")
			}
			input.setSelection(input.text.length)
		}
	}
}
