package com.example.budgiebudgettracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.budgiebudgettracking.entities.TransactionWithCategory
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class TransactionAdapter(
	private var transactions: List<TransactionWithCategory>,
	private val listener: OnItemClickListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

	interface OnItemClickListener {
		fun onItemClick(item: TransactionWithCategory)
	}

	inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val receiptImage: ImageView   = itemView.findViewById(R.id.receiptImageView)
		val amountText: TextView      = itemView.findViewById(R.id.amountTextView)
		val categoryText: TextView    = itemView.findViewById(R.id.categoryTextView)
		val dateText: TextView        = itemView.findViewById(R.id.dateTextView)
		val descriptionText: TextView = itemView.findViewById(R.id.descriptionTextView)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
		val view = LayoutInflater.from(parent.context)
		.inflate(R.layout.item_transaction, parent, false)
		return TransactionViewHolder(view)
	}

	override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
		val wrapper = transactions[position]

		holder.itemView.setOnClickListener {
			listener.onItemClick(wrapper)
		}

		val tx = wrapper.transaction
		val sign = if (tx.isExpense) "-" else "+"
		holder.amountText.text = "$sign R ${"%.2f".format(abs(tx.amount))}"

		holder.categoryText.text = wrapper.category?.categoryName ?: "Uncategorized"
		holder.descriptionText.text = tx.description ?: "No description"

		val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
		holder.dateText.text = sdf.format(tx.date)

		// Load receipt thumbnail (if path is non-null)
		if (!tx.receiptImagePath.isNullOrEmpty()) {
			Glide.with(holder.itemView)
				.load(tx.receiptImagePath)
				.placeholder(R.drawable.ic_camera)
				.into(holder.receiptImage)
		} else {
			holder.receiptImage.setImageResource(R.drawable.ic_camera)
		}


		if (tx.isExpense) {
			holder.amountText.setTextColor(android.graphics.Color.parseColor("#E76F51")) // Expense (negative)
		} else {
			holder.amountText.setTextColor(android.graphics.Color.parseColor("#2A9D8F")) // Income (positive)
		}
	}

	override fun getItemCount(): Int = transactions.size

	fun updateData(newList: List<TransactionWithCategory>) {
		transactions = newList
		notifyDataSetChanged()
	}
}
