package com.example.budgiebudgettracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgiebudgettracking.entities.MonthlyGoal
import java.text.NumberFormat
import java.util.*
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat

class MonthlyGoalsAdapter(
	private val transactionViewModel: TransactionViewModel,
	private val userId: Int
) : ListAdapter<MonthlyGoal, MonthlyGoalsAdapter.GoalViewHolder>(GoalDiffCallback()) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
		val view = LayoutInflater.from(parent.context)
		.inflate(R.layout.item_monthly_goal, parent, false)
		return GoalViewHolder(view)
	}

	override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
		holder.bind(getItem(position), transactionViewModel, userId)
	}

	class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val monthText: TextView = itemView.findViewById(R.id.goalMonth)
		private val minGoalText: TextView = itemView.findViewById(R.id.minGoalText)
		private val maxGoalText: TextView = itemView.findViewById(R.id.maxGoalText)
		private val spentText: TextView = itemView.findViewById(R.id.spentText)
		private val remainingText: TextView = itemView.findViewById(R.id.remainingText)

		fun bind(goal: MonthlyGoal, transactionViewModel: TransactionViewModel, userId: Int) {
			val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
			val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault()) // Input format
			val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault()) // Desired output format

			// Parse the yearMonth and format it
			val date = dateFormat.parse(goal.yearMonth)
			val formattedMonth = displayFormat.format(date)

			monthText.text = formattedMonth // Set the formatted month

			minGoalText.text = "${currencyFormatter.format(goal.minGoal)}"
			maxGoalText.text = "${currencyFormatter.format(goal.maxGoal)}"

			// Default display before data loads
			spentText.text = "Spent: Loading..."
			remainingText.text = "Remaining: ..."

			// Query spent amount for that month
			transactionViewModel.getTotalExpensesForMonth(userId, goal.yearMonth) { spent ->
				val clampedSpent = spent.coerceAtLeast(0.0)
				val remaining = (goal.maxGoal - clampedSpent).coerceAtLeast(0.0)

				spentText.text = "${currencyFormatter.format(clampedSpent)}"
				remainingText.text = "${currencyFormatter.format(remaining)}"
			}
		}
	}

	class GoalDiffCallback : DiffUtil.ItemCallback<MonthlyGoal>() {
		override fun areItemsTheSame(oldItem: MonthlyGoal, newItem: MonthlyGoal) = oldItem.id == newItem.id
		override fun areContentsTheSame(oldItem: MonthlyGoal, newItem: MonthlyGoal) = oldItem == newItem
	}
}
