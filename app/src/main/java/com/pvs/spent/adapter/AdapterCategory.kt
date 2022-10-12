package com.pvs.spent.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pvs.spent.databinding.ItemCategoryBinding
import com.pvs.spent.MyViewModel
import com.pvs.spent.R
import com.pvs.spent.data.Category
import com.pvs.spent.diffutils.CategoryDiffUtil


class AdapterCategory(private val myViewModel: MyViewModel) :
    RecyclerView.Adapter<AdapterCategory.MyViewHolder>() {

    private lateinit var rv: RecyclerView
    private val list = mutableListOf<Category>()

    inner class MyViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.categoryCV.setOnClickListener {
                categoryCategoryOnClickListener.onItemClickExpand(list[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(
            ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {

            categoryIcon.setImageResource(list[position].icon)

            titleTextView.text = list[position].title

            totalTextView.text =
                if (myViewModel.getCurrency().length == 1)
                    String.format(
                        holder.itemView.context.resources.getString(R.string.monetary_amount),
                        list[position].total,
                        myViewModel.getCurrency()
                    )
                else
                    String.format(
                        holder.itemView.context.resources.getString(R.string.monetary_amount_long),
                        list[position].total,
                        myViewModel.getCurrency()
                    )


            budgetTV.text = if (myViewModel.getCurrency().length == 1)
                String.format(
                    holder.itemView.context.resources.getString(R.string.monetary_amount),
                    list[position].budget,
                    myViewModel.getCurrency()
                )
            else
                String.format(
                    holder.itemView.context.resources.getString(R.string.monetary_amount_long),
                    list[position].budget,
                    myViewModel.getCurrency()
                )


            if (list[position].total > list[position].budget) {
                holder.binding.totalTextView.setTextColor(Color.RED)
            }

            if (list[position].title == "Misc") {
                holder.binding.totalTextView.gravity = Gravity.CENTER
                holder.binding.catItemBudget.visibility = View.GONE
                holder.binding.budgetTV.visibility = View.GONE
            }

        }


    }

    override fun getItemCount() = list.size

    fun setList(newList: List<Category>) {
        val diffUtil = CategoryDiffUtil(list, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
        rv.invalidateItemDecorations()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    // Callbacks

    lateinit var categoryCategoryOnClickListener: CategoryOnClickListener

    interface CategoryOnClickListener {
        fun onItemClickExpand(category: Category)
    }

    fun setOnItemClickExpand(listenerUICategory: CategoryOnClickListener) {
        this.categoryCategoryOnClickListener = listenerUICategory
    }

}
