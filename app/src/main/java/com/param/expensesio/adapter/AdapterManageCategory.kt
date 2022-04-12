package com.param.expensesio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.param.expensesio.R
import com.param.expensesio.data.Category
import com.param.expensesio.databinding.ItemManagaeCategoryBinding
import com.param.expensesio.diffutils.CategoryDiffUtil

class AdapterManageCategory : RecyclerView.Adapter<AdapterManageCategory.MyViewHolder>() {

    private val list = mutableListOf<Category>()
    private lateinit var rv: RecyclerView


    inner class MyViewHolder(val binding: ItemManagaeCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.itemManageCategory.setOnClickListener {
                editOnClick.editListener(list[adapterPosition])
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(
            ItemManagaeCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            categoryIcon.setImageResource(list[position].icon)
            textView.text = list[position].title
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

    // Edit callback

    lateinit var editOnClick: EditOnClickListener

    interface EditOnClickListener {
        fun editListener(category: Category)
    }

    fun setEditOnClickListener(listener: EditOnClickListener) {
        this.editOnClick = listener
    }


}