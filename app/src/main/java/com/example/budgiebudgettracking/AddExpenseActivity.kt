package com.example.budgiebudgettracking

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.budgiebudgettracking.entities.Transaction
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel
import com.example.budgiebudgettracking.CategoryPickerActivity
import com.bumptech.glide.Glide
import net.objecthunter.exp4j.ExpressionBuilder
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {
	private var transactionId: Int = -1

	private lateinit var input: EditText
	private lateinit var descriptionInput: EditText
	private lateinit var calculatorGrid: View
	private lateinit var toggleCalculatorButton: Button
	private lateinit var datePickerButton: Button
	private var calculatorVisible = false
	private var calculatedResult: Double = 0.0
	private var openParenCount = 0
	private var selectedDate: Long = System.currentTimeMillis()
	private var selectedCategoryId: Int = 1 // Default category

	// Image related variables
	private lateinit var receiptImageView: ImageView
	private lateinit var placeholderImageView: ImageView
	private var currentPhotoUri: Uri? = null
	private var imageSelected = false

	// ViewModel
	private lateinit var viewModel: TransactionViewModel

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

	// Activity result launcher for category selection
	private val categoryPickerLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			result.data?.let { data ->
				selectedCategoryId = data.getIntExtra("CATEGORY_ID", 1)
				val categoryName = data.getStringExtra("CATEGORY_NAME") ?: "General"
				findViewById<Button>(R.id.btnCategory).text = categoryName
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_expense)

		viewModel = ViewModelProvider(
			this,
			TransactionViewModel.Factory(application)
		)[TransactionViewModel::class.java]

		viewModel.operationResult.observe(this) { success ->
			if (success) {
				Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
				setResult(Activity.RESULT_OK)
				finish()
			} else {
				Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
			}
		}

		input               = findViewById(R.id.inputAmount)
		descriptionInput    = findViewById(R.id.inputDescription)
		calculatorGrid      = findViewById(R.id.calculatorGrid)
		toggleCalculatorButton = findViewById(R.id.btnToggleCalculator)
		datePickerButton    = findViewById(R.id.btnDatePicker)
		receiptImageView    = findViewById(R.id.receiptImageView)
		placeholderImageView= findViewById(R.id.placeholderImageView)

		updateDateButtonText()
		datePickerButton.setOnClickListener { showDatePicker() }

		findViewById<Button>(R.id.btnCategory).setOnClickListener {
			val intent = Intent(this, CategoryPickerActivity::class.java)
			categoryPickerLauncher.launch(intent)
		}

		placeholderImageView.setOnClickListener { showImagePickerOptions() }
		receiptImageView     .setOnClickListener { showImagePickerOptions() }

		input.setOnClickListener { showCalculator() }
		toggleCalculatorButton.setOnClickListener { toggleCalculator() }

		findViewById<ImageButton>(R.id.btnDelete)
		.setOnClickListener { deleteLastCharacter() }

		for (i in 0..9) {
			val btnId = resources.getIdentifier("btn$i", "id", packageName)
			findViewById<Button>(btnId).setOnClickListener {
				input.append(i.toString())
			}
		}

		findViewById<Button>(R.id.btnPlus)    .setOnClickListener { input.append("+") }
		findViewById<Button>(R.id.btnMinus)   .setOnClickListener { input.append("-") }
		findViewById<Button>(R.id.btnMultiply).setOnClickListener { input.append("*") }
		findViewById<Button>(R.id.btnDivide)  .setOnClickListener { input.append("/") }
		findViewById<Button>(R.id.btnDot)     .setOnClickListener { input.append(".") }
		findViewById<Button>(R.id.btnPercent) .setOnClickListener { onPercent() }
		findViewById<Button>(R.id.btnParen)   .setOnClickListener { handleParentheses() }
		findViewById<Button>(R.id.btnClear)   .setOnClickListener {
			input.text.clear()
			openParenCount = 0
		}
		findViewById<Button>(R.id.btnNegate)  .setOnClickListener { onToggleSign() }
		findViewById<Button>(R.id.btnEquals)  .setOnClickListener { calculateExpression() }

		findViewById<Button>(R.id.confirmAmountBtn).setOnClickListener {
			saveTransaction()
		}

		transactionId = intent.getIntExtra("TRANSACTION_ID", -1)
		if (transactionId != -1) {
			loadExistingTransaction(transactionId)
		}
	}

private fun updateDateButtonText() {
	val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
	val formattedDate = sdf.format(Date(selectedDate))
	datePickerButton.text = "Date: $formattedDate"
}

private fun showDatePicker() {
	val calendar = Calendar.getInstance()
	calendar.timeInMillis = selectedDate

	val year = calendar.get(Calendar.YEAR)
	val month = calendar.get(Calendar.MONTH)
	val day = calendar.get(Calendar.DAY_OF_MONTH)

	val datePickerDialog = DatePickerDialog(
		this,
		{ _, selectedYear, selectedMonth, selectedDayOfMonth ->
			val selectedCalendar = Calendar.getInstance()
			selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
			selectedDate = selectedCalendar.timeInMillis
			updateDateButtonText()
		},
		year,
		month,
		day
	)

	datePickerDialog.show()
}

private fun loadExistingTransaction(transactionId: Int) {
	viewModel.getTransactionById(transactionId) { transaction ->
		if (transaction != null) {
			// Update UI with transaction details
			runOnUiThread {
				// Set income/expense selection
				findViewById<RadioButton>(if (transaction.isExpense) R.id.radioExpense else R.id.radioIncome).isChecked = true

				// Set description
				descriptionInput.setText(transaction.description)

				// Set amount
				input.setText(transaction.amount.toString())
				calculatedResult = transaction.amount

				// Set date
				selectedDate = transaction.date
				updateDateButtonText()

				// Set category
				selectedCategoryId = transaction.categoryId

				// Set image if available
				transaction.receiptImagePath?.let { path ->
					if (path.isNotEmpty()) {
						currentPhotoUri = Uri.parse(path)
						loadImageWithGlide(Uri.parse(path))
						imageSelected = true
						placeholderImageView.visibility = View.GONE
					}
				}

				// Update title
				title = "Edit Transaction"
				findViewById<Button>(R.id.confirmAmountBtn).text = "Update Transaction"
			}
		}
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

	// Get the amount
	val amount = if (calculatedResult != 0.0) {
		calculatedResult
	} else {
		try {
			calculateExpression()
			calculatedResult
		} catch (e: Exception) {
			try {
				input.text.toString().toDouble()
			} catch (e: NumberFormatException) {
				Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
				return
			}
		}
	}

	// Get transaction type
	val isExpense = findViewById<RadioButton>(R.id.radioExpense).isChecked

	// Get description (optional)
	val description = descriptionInput.text.toString()

	// Get image path if available
	val imagePath = currentPhotoUri?.toString()

	// Let the VM build & save a transaction with the correct userId
	if (transactionId != -1) {
		// for updates, you’ll still need to pass an existing txId + full object,
		// so you might add a similar updateNewTransaction(...) helper
	} else {
		// …validate, build your fields…
		viewModel.addNewTransaction(
			amount           = amount,
			description      = description,
			isExpense        = isExpense,
			date             = selectedDate,
			categoryId       = selectedCategoryId,
			receiptImagePath = imagePath
		)
	}
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
		Toast.makeText(this, "Error in calculation", Toast.LENGTH_SHORT).show()
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
		Toast.makeText(this, "Error calculating percentage", Toast.LENGTH_SHORT).show()
	}
}

private fun onToggleSign() {
	val currentText = input.text.toString()
	if (currentText.isEmpty()) return

	try {
		// Replace display symbols first
		val expr = currentText.replace("×", "*").replace("÷", "/").replace("−", "-")
		// Try to evaluate the current expression
		val expression = ExpressionBuilder(expr).build()
		calculatedResult = -expression.evaluate()
		// Display neatly
		val resultText = if (calculatedResult % 1.0 == 0.0)
		calculatedResult.toLong().toString()
		else
		calculatedResult.toString()
		input.setText(resultText)
		input.setSelection(resultText.length)
	} catch (e: Exception) {
		// If evaluation fails, just add a negative sign at the beginning
		if (currentText.startsWith("-")) {
			input.setText(currentText.substring(1))
		} else {
			input.setText("-$currentText")
		}
		input.setSelection(input.text.length)
	}
}

override fun onSaveInstanceState(outState: Bundle) {
	super.onSaveInstanceState(outState)
	// Save important state data
	outState.putString("CURRENT_INPUT", input.text.toString())
	outState.putString("DESCRIPTION", descriptionInput.text.toString())
	outState.putDouble("CALCULATED_RESULT", calculatedResult)
	outState.putInt("OPEN_PAREN_COUNT", openParenCount)
	outState.putLong("SELECTED_DATE", selectedDate)
	outState.putInt("SELECTED_CATEGORY_ID", selectedCategoryId)
	outState.putBoolean("CALCULATOR_VISIBLE", calculatorVisible)
	currentPhotoUri?.let { outState.putString("CURRENT_PHOTO_URI", it.toString()) }
	outState.putBoolean("IMAGE_SELECTED", imageSelected)
}

override fun onRestoreInstanceState(savedInstanceState: Bundle) {
	super.onRestoreInstanceState(savedInstanceState)
	// Restore state data
	input.setText(savedInstanceState.getString("CURRENT_INPUT", ""))
	descriptionInput.setText(savedInstanceState.getString("DESCRIPTION", ""))
	calculatedResult = savedInstanceState.getDouble("CALCULATED_RESULT", 0.0)
	openParenCount = savedInstanceState.getInt("OPEN_PAREN_COUNT", 0)
	selectedDate = savedInstanceState.getLong("SELECTED_DATE", System.currentTimeMillis())
	selectedCategoryId = savedInstanceState.getInt("SELECTED_CATEGORY_ID", 1)

	calculatorVisible = savedInstanceState.getBoolean("CALCULATOR_VISIBLE", false)
	if (calculatorVisible) {
		calculatorGrid.visibility = View.VISIBLE
		toggleCalculatorButton.text = "Hide Calculator"
	} else {
		calculatorGrid.visibility = View.GONE
		toggleCalculatorButton.text = "Show Calculator"
	}

	savedInstanceState.getString("CURRENT_PHOTO_URI")?.let {
		currentPhotoUri = Uri.parse(it)
	}

	imageSelected = savedInstanceState.getBoolean("IMAGE_SELECTED", false)
	if (imageSelected && currentPhotoUri != null) {
		loadImageWithGlide(currentPhotoUri!!)
		placeholderImageView.visibility = View.GONE
	}

	updateDateButtonText()
}

override fun onDestroy() {
	super.onDestroy()
	// Clean up any observers if needed
	viewModel.operationResult.removeObservers(this)
}
}
