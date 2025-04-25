package com.example.budgiebudgettracking

import android.app.Activity
import android.content.Intent
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

interface FloatingActionButtonHandler {
	// Default position configuration
	open val fabGravity: Int get() = Gravity.BOTTOM or Gravity.END
	open val fabMarginDp: Int get() = 16

	fun Activity.createAndAttachFab(
		destination: Class<*>,
		iconResId: Int = android.R.drawable.ic_input_add,
		bgColor: Int = R.color.colorPrimary,
		iconColor: Int = R.color.colorBackground,
		onClickFun: (() -> Unit)? = null
	) {
		val fab = FloatingActionButton(this).apply {
			id = View.generateViewId()
			setImageResource(iconResId)
			backgroundTintList = getColorStateList(bgColor)
			imageTintList = getColorStateList(iconColor)
			layoutParams = createFabLayoutParams()

			setOnClickListener {
				onClickFun?.invoke() ?: startActivity(Intent(this@createAndAttachFab, destination))
			}
		}

		// Add to root content frame to ensure proper positioning
		val parent = findViewById<ViewGroup>(R.id.content_frame)
		?: findViewById(android.R.id.content) as ViewGroup

		parent.addView(fab)
	}

	private fun Activity.createFabLayoutParams(): ViewGroup.MarginLayoutParams {
		val margin = dpToPx(fabMarginDp)
		return when (val root = findViewById<ViewGroup>(R.id.content_frame)) {
			is CoordinatorLayout -> CoordinatorLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
			).apply {
				gravity = fabGravity
				setMargins(margin, margin, margin, margin)
			}
			else -> FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
			).apply {
				gravity = fabGravity
				setMargins(margin, margin, margin, margin)
			}
		}
	}

	private fun Activity.dpToPx(dp: Int): Int = TypedValue.applyDimension(
		TypedValue.COMPLEX_UNIT_DIP,
		dp.toFloat(),
		resources.displayMetrics
	).toInt()
}
