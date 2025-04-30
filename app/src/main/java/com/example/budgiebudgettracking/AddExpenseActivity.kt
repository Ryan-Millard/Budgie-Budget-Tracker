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
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.android.material.switchmaterial.SwitchMaterial

import com.example.budgiebudgettracking.components.CalculatorView
import com.example.budgiebudgettracking.entities.Transaction
import com.example.budgiebudgettracking.entities.TransactionWithCategory
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel
import com.example.budgiebudgettracking.viewmodels.UserViewModel
import com.example.budgiebudgettracking.utils.FileUtils

class AddExpenseActivity : AppCompatActivity(), CalculatorView.CalculatorListener {

	companion object {
		const val EXTRA_TX_ID = "TRANSACTION_ID"
	}

	// holds the current user's ID once we fetch it
	private var currentUserId: Int = -1
	// separate ViewModel just for user/session look-ups
	private lateinit var userViewModel: UserViewModel

	private var transactionId: Int = -1

	private lateinit var amountInputLayout: TextInputLayout
	private lateinit var amountInput: TextInputEditText
	private lateinit var descriptionInputLayout: TextInputLayout
	private lateinit var descriptionInput: TextInputEditText

	private lateinit var calculatorView: CalculatorView
	private lateinit var datePickerButton: Button
	private var calculatedResult: Double = 0.0
	private var selectedDate: Long = System.currentTimeMillis()
	private var selectedCategoryId: Int = -1 // Default category

	// Recurring payments
	private lateinit var switchRecurring: SwitchMaterial
	private lateinit var recurringDateLayout: LinearLayout
	private lateinit var startDatePickerButton: Button
	private var selectedStartDate: Long = selectedDate
	private lateinit var endDatePickerButton: Button
	private var selectedEndDate: Long = selectedDate

	// Image related variables
	private lateinit var receiptImageView: ImageView
	private lateinit var addPhotoButton: FloatingActionButton
	private var photoCaptureUri: Uri? = null          // for camera intent
	private var receiptImagePath: String? = null      // the actual saved file path
	private var imageSelected = false

	// ViewModel
	private lateinit var transactionViewModel: TransactionViewModel

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
				// keep the raw Uri if you ever need it…
				photoCaptureUri = uri

				// save and show
				FileUtils.saveImageToInternalStorage(this, uri)?.let { path ->
					receiptImagePath = path
					Glide.with(this)
					.load(File(path))
					.centerCrop()
					.into(receiptImageView)
					imageSelected = true
					addPhotoButton.visibility = View.GONE
				}
			}
		}
	}

	// Activity result launcher for camera
	private val takePictureLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			photoCaptureUri?.let { uri ->
				FileUtils.saveImageToInternalStorage(this, uri)?.let { path ->
					receiptImagePath = path
					Glide.with(this)
					.load(File(path))
					.centerCrop()
					.into(receiptImageView)
					imageSelected = true
					addPhotoButton.visibility = View.GONE
				}
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

		transactionViewModel = ViewModelProvider(
			this,
			TransactionViewModel.Factory(application)
		)[TransactionViewModel::class.java]

		transactionViewModel.operationResult.observe(this) { success ->
			if (success) {
				Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
				setResult(Activity.RESULT_OK)
				finish()
			} else {
				Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
			}
		}

		userViewModel = ViewModelProvider(
			this,
			UserViewModel.Factory(application)
		)[UserViewModel::class.java]
		// fire off the lookup; when it returns we'll stash the ID
		userViewModel.getCurrentUser { user ->
			currentUserId = user?.id ?: -1
		}

		// Initialize UI components
		amountInput            = findViewById(R.id.inputAmount)
		amountInputLayout      = findViewById(R.id.amountLayout)
		descriptionInput       = findViewById(R.id.inputDescription)
		descriptionInputLayout = findViewById(R.id.descriptionLayout)
		calculatorView         = findViewById(R.id.calculatorView)
		datePickerButton       = findViewById(R.id.btnDatePicker)
		receiptImageView       = findViewById(R.id.receiptImageView)
		addPhotoButton         = findViewById(R.id.addPhotoButton)
		switchRecurring        = findViewById(R.id.switchRecurring)
		recurringDateLayout    = findViewById(R.id.recurringDateLayout)
		startDatePickerButton    = findViewById(R.id.btnStartDatePicker)
		endDatePickerButton    = findViewById(R.id.btnEndDatePicker)

		// Set up calculator listener
		calculatorView.setCalculatorListener(this)

		// Make the amount input display update when the calculator changes
		amountInput.setOnClickListener {
			// When they click the display, focus the calculator instead
			calculatorView.requestFocus()
		}

		val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
		datePickerButton.setOnClickListener {
			showDatePicker(selectedDate) { newDate ->
				selectedDate = newDate
				val date = Date(selectedDate)
				datePickerButton.text = dateFormat.format(date)
			}
		}
		startDatePickerButton.setOnClickListener {
			showDatePicker(selectedStartDate) { newDate ->
				selectedStartDate = newDate
				val date = Date(selectedStartDate)
				startDatePickerButton.text = dateFormat.format(date)
			}
		}
		endDatePickerButton.setOnClickListener {
			showDatePicker(selectedEndDate) { newDate ->
				selectedEndDate = newDate
				val date = Date(selectedEndDate)
				endDatePickerButton.text = dateFormat.format(date)
			}
		}

		switchRecurring.setOnCheckedChangeListener { _, isChecked ->
			recurringDateLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
		}

		findViewById<Button>(R.id.btnCategory).setOnClickListener {
			openCategoryPicker()
		}

		// Set up photo button
		addPhotoButton.setOnClickListener {
			showImagePickerOptions()
		}

		// Set up save button
		findViewById<Button>(R.id.confirmAmountBtn).setOnClickListener {
			saveTransaction()
		}

		// Check if we're editing an existing transaction
		transactionId = intent.getIntExtra(EXTRA_TX_ID, -1)
		if (transactionId != -1) {
			// We're editing an existing transaction
			loadExistingTransaction(transactionId)
		}
	}

	private fun loadExistingTransaction(id: Int) {
		transactionViewModel.getTransactionWithCategoryById(id).observe(this) { twc ->
			twc?.let { (transaction, category) ->
				// Set transaction type
				val isExpense = transaction.isExpense
				findViewById<RadioButton>(if (isExpense) R.id.radioExpense else R.id.radioIncome).isChecked = true

				// Set amount (without negative sign for expenses)
				calculatedResult = Math.abs(transaction.amount)
				val amountText = if (calculatedResult % 1.0 == 0.0) 
				calculatedResult.toLong().toString()
				else 
				calculatedResult.toString()

				calculatorView.setDisplayText(amountText)
				amountInput.setText(amountText)

				// Set description
				descriptionInput.setText(transaction.description)

				// Set date
				selectedDate = transaction.date
				val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
				datePickerButton.text = dateFormat.format(Date(selectedDate))

				// Set category
				selectedCategoryId = transaction.categoryId
				findViewById<Button>(R.id.btnCategory).text = category?.categoryName ?: "General"

				val isRecurring = transaction.isRecurring
				switchRecurring.isChecked = isRecurring
				if (isRecurring) {
					selectedStartDate = transaction.startTime
					selectedEndDate = transaction.endTime
					startDatePickerButton.text = dateFormat.format(Date(selectedStartDate))
					endDatePickerButton.text = dateFormat.format(Date(selectedEndDate))
				}

				// Set image if exists
				transaction.receiptImagePath?.let { uriString ->
					photoCaptureUri = Uri.parse(uriString)
					loadImageWithGlide(photoCaptureUri!!)
					imageSelected = true
					addPhotoButton.visibility = View.GONE
				}
			}
		}
	}

	private fun showDatePicker(initialDate: Long, onDateChosen: (Long) -> Unit) {
		val calendar = Calendar.getInstance().apply { timeInMillis = initialDate }
		DatePickerDialog(
			this,
			{ _, year, month, day ->
				calendar.set(year, month, day)
				onDateChosen(calendar.timeInMillis)
			},
			calendar.get(Calendar.YEAR),
			calendar.get(Calendar.MONTH),
			calendar.get(Calendar.DAY_OF_MONTH)
		).show()
	}

	private fun openCategoryPicker() {
		val intent = Intent(this, CategoryPickerActivity::class.java)
		intent.putExtra("CURRENT_CATEGORY_ID", selectedCategoryId)
		categoryPickerLauncher.launch(intent)
	}

	private fun showImagePickerOptions() {
		val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

		val builder = androidx.appcompat.app.AlertDialog.Builder(this)
		builder.setTitle("Add Receipt")
		builder.setItems(options) { dialog, item ->
			when (options[item]) {
				"Take Photo" -> checkCameraPermission()
				"Choose from Gallery" -> checkGalleryPermission()
				"Cancel" -> dialog.dismiss()
			}
		}
		builder.show()
	}

	private fun checkCameraPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			// For Android 13+, we need to request camera permission
			requestMultiplePermissionsLauncher.launch(
				arrayOf(Manifest.permission.CAMERA)
			)
		} else {
			when {
				ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == 
				PackageManager.PERMISSION_GRANTED -> {
					openCamera()
				}
				else -> {
					requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
				}
			}
		}
	}

	private fun checkGalleryPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			// For Android 13+, READ_MEDIA_IMAGES is needed
			openGallery() // Gallery access doesn't need runtime permission in Android 13+
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			// Android 11+ has scoped storage, no permission needed
			openGallery()
		} else {
			// For older versions, check READ_EXTERNAL_STORAGE permission
			when {
				ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
				PackageManager.PERMISSION_GRANTED -> {
					openGallery()
				}
				else -> {
					requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
				}
			}
		}
	}

	private fun openCamera() {
		val photoFile: File? = try {
			createImageFile()
		} catch (ex: IOException) {
			Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
			null
		}

		photoFile?.let {
			photoCaptureUri = FileProvider.getUriForFile(
				this,
				"${applicationContext.packageName}.fileprovider",
				it
			)

			val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoCaptureUri)
			takePictureLauncher.launch(takePictureIntent)
		}
	}

	private fun openGallery() {
		val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
		pickImageLauncher.launch(intent)
	}

	private fun createImageFile(): File {
		val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
		val imageFileName = "JPEG_${timeStamp}_"
		val storageDir = getExternalFilesDir("receipts")

		return File.createTempFile(
			imageFileName,
			".jpg",
			storageDir
		)
	}

	private fun loadImageWithGlide(uri: Uri) {
		Glide.with(this)
		.load(uri)
		.centerCrop()
		.into(receiptImageView)
	}

	private fun saveTransaction() {
		// Basic validation
		if (calculatedResult <= 0) {
			Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
			return
		}
		if (selectedCategoryId == -1) {
			Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
			return
		}
		if (currentUserId == -1) {
			Toast.makeText(this, "User session error, please try again", Toast.LENGTH_SHORT).show()
			return
		}

		// Get transaction type (income/expense)
		val isExpense = findViewById<RadioButton>(R.id.radioExpense).isChecked
		val amount = if (isExpense) -calculatedResult else calculatedResult

		val isRecurring = switchRecurring.isChecked

		val description = descriptionInput.text.toString().trim()

		// Create transaction object
		val transaction = Transaction(
			userId = currentUserId,
			categoryId = selectedCategoryId,
			amount = amount,
			description = description,
			date = selectedDate,
			receiptImagePath = receiptImagePath,
			isRecurring = isRecurring,
			startTime = if (isRecurring) selectedStartDate else selectedDate,
			endTime = if (isRecurring) selectedEndDate else selectedDate,
			isExpense = isExpense,
			createdAt = if (transactionId != -1) 0L else System.currentTimeMillis() // Only set for new records
		)

		// Save transaction
		if (transactionId != -1) {
			transactionViewModel.updateTransaction(transaction)
		} else {
			transactionViewModel.addTransaction(transaction)
		}
	}

	override fun onCalculationResult(result: Double, displayText: String) {
		calculatedResult = result
		amountInput.setText(displayText)
	}
}
