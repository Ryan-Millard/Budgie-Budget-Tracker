package com.example.budgiebudgettracking

import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Activity

import com.example.budgiebudgettracking.dao.UserDao
import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.utils.SessionManager
import com.example.budgiebudgettracking.entities.User

class ProfileActivity : BaseActivity() {
	private lateinit var sessionManager: SessionManager
	private lateinit var userDao: UserDao

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_profile)

		sessionManager = SessionManager.getInstance(applicationContext)
		userDao = AppDatabase.getDatabase(applicationContext).userDao()

		setupClickListeners()
		loadUserProfileData()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			// Reload the user profile data after returning from EditProfileActivity
			loadUserProfileData()
		}
	}

	private fun setupClickListeners() {
		// Launch EditProfileActivity when edit button is clicked
		val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
		btnEditProfile.setOnClickListener {
			val intent = Intent(this, EditProfileActivity::class.java)
			startActivityForResult(intent, 1001)  // 1001 is a request code
		}

		val btnLogout = findViewById<Button>(R.id.btnLogout)
		btnLogout.setOnClickListener {
			sessionManager.logout()
			Toast.makeText(this@ProfileActivity, "Logged out successfully!", Toast.LENGTH_SHORT).show()
			startActivity(Intent(this, LoginActivity::class.java))
		}
	}

	private fun loadUserProfileData() {
		// Fetch the user's data from the database
		val email = sessionManager.getUserEmail() // Assume session manager stores user's email

		// Run in a coroutine for database operations
		CoroutineScope(Dispatchers.IO).launch {
			val user = userDao.getUserByEmail(email)

			withContext(Dispatchers.Main) {
				if (user != null) {
					// User data found in the database, display it
					updateUIWithUserData(user)
				} else {
					// No user data found, show default values
					showDefaultProfileData()
				}
			}
		}
	}

	private fun updateUIWithUserData(user: User) {
		// Update the UI with the user's data
		findViewById<TextView>(R.id.tvProfileName).text = user.fullName
		findViewById<TextView>(R.id.tvProfileEmail).text = user.email
		findViewById<TextView>(R.id.tvPhoneValue).text = user.phone ?: "N/A"
		findViewById<TextView>(R.id.tvLocationValue).text = user.location ?: "N/A"
		findViewById<TextView>(R.id.tvBioValue).text = user.bio ?: "N/A"

		// Load the profile picture if available
		val profileImageView = findViewById<ImageView>(R.id.profileImageView)
		user.profilePicPath?.let { imagePath ->
			Glide.with(this).load(imagePath).into(profileImageView)

			profileImageView.setOnClickListener {
				startActivity(
					Intent(this@ProfileActivity, FullScreenImageActivity::class.java)
					.putExtra(FullScreenImageActivity.EXTRA_IMAGE_PATH, imagePath)
				)
			}
		}
	}

	private fun showDefaultProfileData() {
		// Show default values if no user data is found
		findViewById<TextView>(R.id.tvProfileName).text = "John Doe"
		findViewById<TextView>(R.id.tvProfileEmail).text = "john.doe@example.com"
		findViewById<TextView>(R.id.tvPhoneValue).text = "N/A"
		findViewById<TextView>(R.id.tvLocationValue).text = "N/A"
		findViewById<TextView>(R.id.tvBioValue).text = "N/A"

		// Set a default profile image (if needed)
		val profileImageView = findViewById<ImageView>(R.id.profileImageView)
		Glide.with(this).load(R.drawable.ic_profile_placeholder).into(profileImageView)
	}
}
