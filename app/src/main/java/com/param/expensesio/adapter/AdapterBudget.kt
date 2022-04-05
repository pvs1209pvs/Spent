package com.param.expensesio.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.param.expensesio.MyViewModel
import com.param.expensesio.data.Category
import com.param.expensesio.databinding.ItemBudgetBinding
import com.param.expensesio.diffutils.CategoryDiffUtil


class AdapterBudget(private val myViewModel: MyViewModel) :
    RecyclerView.Adapter<AdapterBudget.MyViewHolder>() {

    val list = mutableListOf<Category>()
    private lateinit var rv: RecyclerView


    inner class MyViewHolder(val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(
            ItemBudgetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            categoryIcon.setImageResource(list[position].icon)
            categoryTitle.text = list[position].title
            budgetAmount.setText(list[position].budget.toString())

            budgetAmount.doOnTextChanged { text, _, _, _ ->

                if (text != null && text.isNotEmpty() && text.matches("-?\\d+(\\.\\d+)?".toRegex())) {

                    list[position].budget = text.toString().toFloat()

                    val budgetColor = if (list[position].budget <= 0) Color.RED
                    else Color.BLACK

                    holder.binding.budgetAmount.setTextColor(budgetColor)

                }

            }
        }


    }

    override fun getItemCount() = list.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    fun setList(newList: List<Category>) {
        val diffUtil = CategoryDiffUtil(list, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
        rv.invalidateItemDecorations()
    }

    fun getTitles() = list.map { it.title }.toList()


}
