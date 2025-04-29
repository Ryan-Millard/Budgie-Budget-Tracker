package com.example.budgiebudgettracking

import android.content.Intent
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import android.net.Uri
import java.io.File

import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.utils.SessionManager
import com.example.budgiebudgettracking.utils.FileUtils
import com.example.budgiebudgettracking.dao.UserDao

class EditProfileActivity : AppCompatActivity() {

	private lateinit var sessionManager: SessionManager
	private lateinit var userDao: UserDao

	private lateinit var etName: EditText
	private lateinit var etEmail: EditText
	private lateinit var etPhone: EditText
	private lateinit var etLocation: EditText
	private lateinit var etBio: EditText
	private lateinit var btnSave: Button
	private lateinit var btnCancel: Button

	private lateinit var profileImageView: ImageView
	private lateinit var btnChangeProfileImage: ImageButton
	private var profilePicPath: String? = null

	private var isDataChanged = false

	private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
		uri?.let {
			val savedImagePath = FileUtils.saveImageToInternalStorage(this, it)
			savedImagePath?.let { path ->
				profilePicPath = path
				Glide.with(this).load(File(path)).into(profileImageView)
				isDataChanged = true
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_edit_profile)

		// Bind views
		etName = findViewById(R.id.etName)
		etEmail = findViewById(R.id.etEmail)
		etPhone = findViewById(R.id.etPhone)
		etLocation = findViewById(R.id.etLocation)
		etBio = findViewById(R.id.etBio)
		btnSave = findViewById(R.id.btnSave)
		btnCancel = findViewById(R.id.btnCancel)
		profileImageView = findViewById(R.id.profileImageView)
		btnChangeProfileImage = findViewById(R.id.btnChangeProfileImage)

		sessionManager = SessionManager.getInstance(applicationContext)
		userDao = AppDatabase.getDatabase(applicationContext).userDao()

		btnChangeProfileImage.setOnClickListener {
			pickImageLauncher.launch("image/*")
		}

		btnSave.setOnClickListener {
			saveProfileChanges()
			val resultIntent = Intent()
			setResult(RESULT_OK, resultIntent)
			finish()
		}

		btnCancel.setOnClickListener {
			if (isDataChanged) {
				showCancelConfirmationDialog()
			} else {
				finish()
			}
		}

		loadProfileData()
	}

	private fun loadProfileData() {
		lifecycleScope.launch {
			// Get the user's email from the session manager
			val currentUserEmail = sessionManager.getUserEmail()
			val currentUser = userDao.getUserByEmail(currentUserEmail)

			currentUser?.let { user ->
				// Set the data from the database
				etName.setText(user.fullName)
				etEmail.setText(user.email)
				etPhone.setText(user.phone)
				etLocation.setText(user.location)
				etBio.setText(user.bio)

				// Load the profile picture if available
				user.profilePicPath?.let {
					Glide.with(this@EditProfileActivity).load(File(it)).into(profileImageView)
					profilePicPath = it
				}
			} ?: run {
				// Load default data if no user found
				etName.setText("John Doe")
				etEmail.setText("john.doe@example.com")
				etPhone.setText("+1 (555) 123-4567")
				etLocation.setText("San Francisco, CA")
				etBio.setText("Software developer with a passion for creating user-friendly mobile applications. Love hiking and photography in my free time.")
			}
		}
	}

	private fun saveProfileChanges() {
		val updatedName = etName.text.toString()
		val updatedEmail = etEmail.text.toString()
		val updatedPhone = etPhone.text.toString()
		val updatedLocation = etLocation.text.toString()
		val updatedBio = etBio.text.toString()
		val updatedProfilePicPath = profilePicPath

		lifecycleScope.launch {
			val currentUserEmail = sessionManager.getUserEmail()
			val currentUser = userDao.getUserByEmail(currentUserEmail)

			if (currentUser != null) {
				val updatedUser = currentUser.copy(
					fullName = updatedName,
					email = updatedEmail,
					phone = updatedPhone,
					location = updatedLocation,
					bio = updatedBio,
					profilePicPath = updatedProfilePicPath
				)
				userDao.updateUser(updatedUser)
			} else {
				Toast.makeText(this@EditProfileActivity, "Error updating user", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun showCancelConfirmationDialog() {
		val dialog = AlertDialog.Builder(this)
		.setTitle(getString(R.string.cancel_changes_title))
		.setMessage(getString(R.string.cancel_changes_message))
		.setPositiveButton(getString(R.string.cancel_yes)) { _, _ ->
			finish()
		}
		.setNegativeButton(getString(R.string.cancel_no), null)
		.create()
		dialog.show()
	}
}
