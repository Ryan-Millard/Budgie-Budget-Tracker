package com.example.budgiebudgettracking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

	private lateinit var etName: EditText
	private lateinit var etEmail: EditText
	private lateinit var etPhone: EditText
	private lateinit var etLocation: EditText
	private lateinit var etBio: EditText
	private lateinit var btnSave: Button
	private lateinit var btnCancel: Button

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

		// TODO: Optionally load current profile info into fields here
		loadProfileData()

		btnSave.setOnClickListener {
			// TODO: Validate inputs and save changes (e.g., update local storage or send to server)
			// For now, simply finish the activity to return to the profile screen.
			saveProfileChanges()
			finish()
		}

		btnCancel.setOnClickListener {
			// Discard changes and go back
			finish()
		}
	}

	private fun loadProfileData() {
		// Load user info into form fields.
		// This could be from an Intent extra, shared preferences, or a database.
		etName.setText("John Doe")
		etEmail.setText("john.doe@example.com")
		etPhone.setText("+1 (555) 123-4567")
		etLocation.setText("San Francisco, CA")
		etBio.setText("Software developer with a passion for creating user-friendly mobile applications. Love hiking and photography in my free time.")
	}

	private fun saveProfileChanges() {
		// Retrieve updated values
		val updatedName = etName.text.toString()
		val updatedEmail = etEmail.text.toString()
		val updatedPhone = etPhone.text.toString()
		val updatedLocation = etLocation.text.toString()
		val updatedBio = etBio.text.toString()

		// TODO: Save the updated info to your storage system or backend service.
		// This is a placeholder for your saving logic.
	}
}
