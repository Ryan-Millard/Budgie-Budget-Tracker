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

class MonthlyGoalsAdapter :
ListAdapter<MonthlyGoal, MonthlyGoalsAdapter.GoalViewHolder>(GoalDiffCallback()) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
		val view = LayoutInflater.from(parent.context)
		.inflate(R.layout.item_monthly_goal, parent, false)
		return GoalViewHolder(view)
	}

	override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val monthText: TextView = itemView.findViewById(R.id.goalMonth)
		private val minGoalText: TextView = itemView.findViewById(R.id.minGoalText)
		private val maxGoalText: TextView = itemView.findViewById(R.id.maxGoalText)
		private val spentText: TextView = itemView.findViewById(R.id.spentText)

		fun bind(goal: MonthlyGoal) {
			val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
			monthText.text = goal.yearMonth
			minGoalText.text = currencyFormatter.format(goal.minGoal)
			maxGoalText.text = currencyFormatter.format(goal.maxGoal)
			// spentText.text = currencyFormatter.format(goal.spentAmount)
		}
	}

	class GoalDiffCallback : DiffUtil.ItemCallback<MonthlyGoal>() {
		override fun areItemsTheSame(oldItem: MonthlyGoal, newItem: MonthlyGoal): Boolean {
			return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: MonthlyGoal, newItem: MonthlyGoal): Boolean {
			return oldItem == newItem
		}
	}
}
