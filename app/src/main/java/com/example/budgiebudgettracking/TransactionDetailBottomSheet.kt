package com.example.budgiebudgettracking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

import com.example.budgiebudgettracking.entities.TransactionWithCategory
import com.example.budgiebudgettracking.viewmodels.TransactionViewModel

class TransactionDetailBottomSheet : BottomSheetDialogFragment() {

	private lateinit var viewModel: TransactionViewModel

	companion object {
		private const val ARG_TX_ID = "arg_transaction_id"
		fun newInstance(txId: Int) = TransactionDetailBottomSheet().apply {
			arguments = Bundle().apply {
				putInt(ARG_TX_ID, txId)  // pass only primitive ID&#8203;:contentReference[oaicite:3]{index=3}
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = inflater.inflate(
		R.layout.fragment_transaction_detail, container, false
	)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// 1. Initialize shared ViewModel scoped to the activity&#8203;:contentReference[oaicite:4]{index=4}
		viewModel = ViewModelProvider(
			requireActivity(),
			TransactionViewModel.Factory(requireActivity().application)
		).get(TransactionViewModel::class.java)

		// 2. Retrieve TX ID from arguments&#8203;:contentReference[oaicite:5]{index=5}
		val txId = requireArguments().getInt(ARG_TX_ID)

		// 3. Observe the LiveData for this single transaction with category
		viewModel.getTransactionWithCategoryById(txId)
		.observe(viewLifecycleOwner) { item ->
			bindTransaction(item, view)
		}
	}

	/** Populate UI and wire up actions */
	private fun bindTransaction(item: TransactionWithCategory, root: View) {
		// Receipt thumbnail
		val receiptView = root.findViewById<ImageView>(R.id.receiptDetailImage)
		if (!item.transaction.receiptImagePath.isNullOrEmpty()) {
			Glide.with(this).load(item.transaction.receiptImagePath)
			.placeholder(R.drawable.feather).into(receiptView)
		} else {
			receiptView.setImageResource(R.drawable.feather)
		}
		receiptView.setOnClickListener {
			startActivity(
				Intent(requireContext(), FullScreenImageActivity::class.java)
				.putExtra(FullScreenImageActivity.EXTRA_IMAGE_PATH, item.transaction.receiptImagePath ?: "")
			)
		}

		// Amount, category, date, description
		val amountDetailText = root.findViewById<TextView>(R.id.amountDetailText)
		amountDetailText.text = "${if (item.transaction.isExpense) "-" else "+"} R %.2f".format(abs(item.transaction.amount))
		if(item.transaction.amount < 0) {
			amountDetailText.setTextColor(android.graphics.Color.parseColor("#E76F51"))
		} else if (item.transaction.amount > 0) {
			amountDetailText.setTextColor(android.graphics.Color.parseColor("#2A9D8F"))
		}
		root.findViewById<TextView>(R.id.categoryDetailText).text = item.category?.categoryName ?: "Uncategorized"
		root.findViewById<TextView>(R.id.dateDetailText).text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) .format(item.transaction.date)
		root.findViewById<TextView>(R.id.descriptionDetailText).text = item.transaction.description ?: "No description"

		// 4. Edit button → launch AddExpenseActivity with ID&#8203;:contentReference[oaicite:6]{index=6}
		root.findViewById<Button>(R.id.editButton).setOnClickListener {
			startActivity(
				Intent(requireContext(), AddExpenseActivity::class.java).apply {
					putExtra(AddExpenseActivity.EXTRA_TX_ID, item.transaction.id)
				}
			)
			dismiss()
		}

		// 5. Delete button → ask ViewModel to delete, then dismiss
		root.findViewById<Button>(R.id.deleteButton).setOnClickListener {
			viewModel.deleteTransaction(item.transaction)
			dismiss()
		}
	}
}

