package com.pvs.spent.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pvs.spent.R
import com.pvs.spent.databinding.ItemExpenseBinding
import com.pvs.spent.MyViewModel
import com.pvs.spent.data.Expense
import com.pvs.spent.diffutils.ExpenseDiffUtil
import java.util.*

class AdapterExpenses(private val myViewModel: MyViewModel) :
    RecyclerView.Adapter<AdapterExpenses.MyViewHolder>() {

    val list = mutableListOf<Expense>()
    private lateinit var rv: RecyclerView


    inner class MyViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.itemExpenseCard.setOnClickListener {
                popUpMenuListener.editListener(list[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.binding.apply {

            expenseTitle.text = list[position].title

            val expenseAmountStrFormat =
                if (myViewModel.getCurrency().length == 1)
                    holder.itemView.context.resources.getString(R.string.monetary_amount)
                else
                    holder.itemView.context.resources.getString(R.string.monetary_amount_long)

            expenseAmount.text =
                String.format(
                    expenseAmountStrFormat,
                    myViewModel.formatNumber(list[position].amount),
                    myViewModel.getCurrency()
                )

            val createdOn = list[position].createdOn
            expenseCreatedOn.text = String.format(
                holder.itemView.context.resources.getString(R.string.creation_date),
                createdOn.year,
                createdOn.monthValue,
                createdOn.dayOfMonth,
            )

        }

    }

    override fun getItemCount() = list.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    fun setList(newList: List<Expense>) {
        val diffUtil = ExpenseDiffUtil(list, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
        rv.invalidateItemDecorations()
    }

    // Call back

    lateinit var popUpMenuListener: PopUpMenuListener

    interface PopUpMenuListener {
        fun editListener(expense: Expense)
    }

    fun setDelOnClickListener(listener: PopUpMenuListener) {
        this.popUpMenuListener = listener
    }

}