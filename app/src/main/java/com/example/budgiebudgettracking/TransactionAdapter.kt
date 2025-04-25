package com.example.budgiebudgettracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgiebudgettracking.entities.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(private var transactions: List<Transaction>) :
RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

	inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val amountText: TextView = itemView.findViewById(R.id.amountTextView)
		val descriptionText: TextView = itemView.findViewById(R.id.descriptionTextView)
		val dateText: TextView = itemView.findViewById(R.id.dateTextView)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
		val view = LayoutInflater.from(parent.context)
		.inflate(R.layout.item_transaction, parent, false)
		return TransactionViewHolder(view)
	}

	override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
		val transaction = transactions[position]
		val sign = if (transaction.isExpense) "-" else "+"
		holder.amountText.text = "$sign R ${"%.2f".format(transaction.amount)}"
		holder.descriptionText.text = transaction.description ?: "No description"

		val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
		holder.dateText.text = sdf.format(Date(transaction.date))
	}

	override fun getItemCount(): Int = transactions.size

	fun updateData(newTransactions: List<Transaction>) {
		transactions = newTransactions
		notifyDataSetChanged()
	}
}
