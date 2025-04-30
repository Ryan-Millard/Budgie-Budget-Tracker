package com.example.budgiebudgettracking

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.budgiebudgettracking.components.ZoomableImageView

class FullScreenImageActivity : AppCompatActivity() {

	companion object {
		const val EXTRA_IMAGE_PATH = "IMAGE_PATH"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_full_screen_image)

		// Always import this custom view
		val zoomableView = findViewById<ZoomableImageView>(R.id.zoomableImageView)

		// Retrieve and null-check the path
		val path = intent.getStringExtra(EXTRA_IMAGE_PATH)
		if (path.isNullOrEmpty()) {
			Toast.makeText(this, "Invalid image path", Toast.LENGTH_SHORT).show()
			finish()
			return
		}

		// Load with Glide—using the correct variable, not an undefined 'uri' :contentReference[oaicite:5]{index=5}
		Glide.with(this)
		.load(path)
		.placeholder(R.drawable.feather)
		.into(zoomableView)
	}
}
