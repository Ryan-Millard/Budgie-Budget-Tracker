package com.example.budgiebudgettracking.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.budgiebudgettracking.R

class NavigationSelector @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private lateinit var leftButton: ImageButton
	private lateinit var rightButton: ImageButton
	private lateinit var centerText: TextView

	private var onNavigationChangeListener: OnNavigationChangeListener? = null
	private var currentPosition = 0
	private var items: List<String> = emptyList()

	init {
		// Fix the layout inflation path - note the correct path format
		val inflater = LayoutInflater.from(context)
		inflater.inflate(R.layout.navigation_selector, this, true)

		// Initialize views after inflation
		leftButton = findViewById(R.id.button_previous)
		rightButton = findViewById(R.id.button_next)
		centerText = findViewById(R.id.text_center)

		setupAttributes(attrs)
		setupClickListeners()
	}

	private fun setupAttributes(attrs: AttributeSet?) {
		attrs?.let {
			val typedArray = context.obtainStyledAttributes(it, R.styleable.NavigationSelector)

			val textAppearance = typedArray.getResourceId(
				R.styleable.NavigationSelector_textAppearance,
				android.R.style.TextAppearance_Medium
			)
			centerText.setTextAppearance(context, textAppearance)

			val leftIcon = typedArray.getResourceId(
				R.styleable.NavigationSelector_leftButtonIcon,
				R.drawable.ic_arrow_left
			)
			leftButton.setImageResource(leftIcon)

			val rightIcon = typedArray.getResourceId(
				R.styleable.NavigationSelector_rightButtonIcon,
				R.drawable.ic_arrow_right
			)
			rightButton.setImageResource(rightIcon)

			typedArray.recycle()
		}
	}

	private fun setupClickListeners() {
		leftButton.setOnClickListener {
			if (currentPosition > 0) {
				currentPosition--
				updateUI()
				onNavigationChangeListener?.onNavigationChanged(currentPosition, items[currentPosition])
			}
		}

		rightButton.setOnClickListener {
			if (currentPosition < items.size - 1) {
				currentPosition++
				updateUI()
				onNavigationChangeListener?.onNavigationChanged(currentPosition, items[currentPosition])
			}
		}
	}

	fun setItems(items: List<String>) {
		this.items = items
		currentPosition = 0
		updateUI()
	}

	fun setCurrentPosition(position: Int) {
		if (items.isEmpty()) return

		currentPosition = when {
			position < 0 -> 0
			position >= items.size -> items.size - 1
			else -> position
		}

		updateUI()
	}

	fun getCurrentPosition(): Int = currentPosition

	fun getCurrentItem(): String? = items.getOrNull(currentPosition)

	private fun updateUI() {
		if (items.isEmpty()) {
			centerText.text = ""
			leftButton.isEnabled = false
			rightButton.isEnabled = false
			return
		}

		centerText.text = items[currentPosition]
		leftButton.isEnabled = currentPosition > 0
		rightButton.isEnabled = currentPosition < items.size - 1

		// Update button alpha for visual feedback when disabled
		leftButton.alpha = if (leftButton.isEnabled) 1.0f else 0.5f
		rightButton.alpha = if (rightButton.isEnabled) 1.0f else 0.5f
	}

	fun setOnNavigationChangeListener(listener: OnNavigationChangeListener) {
		onNavigationChangeListener = listener
	}

	interface OnNavigationChangeListener {
		fun onNavigationChanged(position: Int, value: String)
	}
}
