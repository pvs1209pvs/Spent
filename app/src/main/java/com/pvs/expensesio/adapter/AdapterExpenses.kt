package com.pvs.expensesio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pvs.expensesio.R
import com.pvs.expensesio.databinding.ItemExpenseBinding
import com.pvs.expensesio.MyViewModel
import com.pvs.expensesio.data.Expense
import com.pvs.expensesio.diffutils.ExpenseDiffUtil
import java.util.*

class AdapterExpenses(private val myViewModel: MyViewModel) :
    RecyclerView.Adapter<AdapterExpenses.MyViewHolder>() {

    val list = mutableListOf<Expense>()
    private lateinit var rv: RecyclerView


    inner class MyViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {

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

            expenseAmount.text = if (myViewModel.getCurrency().length == 1)
                String.format(
                    holder.itemView.context.resources.getString(R.string.monetary_amount),
                    list[position].amount,
                    myViewModel.getCurrency()
                )
            else
                String.format(
                    holder.itemView.context.resources.getString(R.string.monetary_amount_long),
                    list[position].amount,
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