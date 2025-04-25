package com.example.budgiebudgettracking

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.example.budgiebudgettracking.entities.Category

class CategoryAdapter(
	private val categories: MutableList<Category> = mutableListOf()
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

	private var onItemClick: ((Category) -> Unit)? = null

	fun setOnItemClickListener(listener: (Category) -> Unit) {
		onItemClick = listener
	}

	inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val tvName: TextView = view.findViewById(R.id.tvCategoryName)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
		val view = LayoutInflater.from(parent.context)
		.inflate(R.layout.item_category, parent, false)
		return CategoryViewHolder(view)
	}

	override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
		val category = categories[position]
		holder.tvName.text = "${category.id}) ${category.categoryName}"

		category.hexColorCode?.let { hex ->
			try {
				// Create GradientDrawable for the background
				val parsedColor = android.graphics.Color.parseColor(hex)
				val gradientDrawable = GradientDrawable()
				gradientDrawable.setColor(parsedColor)
				gradientDrawable.cornerRadius = 8f // Matches your defined corner radius

				// Apply the background to the CardView
				(holder.itemView as CardView).setCardBackgroundColor(parsedColor)

				// Calculate luminance for text color
				val r = android.graphics.Color.red(parsedColor)
				val g = android.graphics.Color.green(parsedColor)
				val b = android.graphics.Color.blue(parsedColor)
				val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
				val textColor = if (luminance > 186) {
					android.graphics.Color.BLACK
				} else {
					android.graphics.Color.WHITE
				}
				holder.tvName.setTextColor(textColor)

			} catch (e: IllegalArgumentException) { }
		} ?: run { }

		holder.itemView.setOnClickListener {
			onItemClick?.invoke(category)
		}
	}

	override fun getItemCount(): Int = categories.size

	fun updateCategories(newList: List<Category>) {
		categories.clear()
		categories.addAll(newList)
		notifyDataSetChanged()
	}
}
