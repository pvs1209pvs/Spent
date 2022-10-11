package com.pvs.spent.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pvs.spent.databinding.CategoryIconBinding
import com.pvs.spent.diffutils.CategoryIconDiffUtil

class AdapterCategoryIconPicker :
    RecyclerView.Adapter<AdapterCategoryIconPicker.MyViewHolder>() {

    private val list = mutableListOf<Int>()

    inner class MyViewHolder(val binding: CategoryIconBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.categoryIconIV.setOnClickListener {
                categoryIconPickerListener.selectIcon(list[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(
            CategoryIconBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.categoryIconIV.setImageResource(list[position])
    }

    override fun getItemCount() = list.size

    fun setList(newList: List<Int>) {
        val diffUtil = CategoryIconDiffUtil(list, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    lateinit var categoryIconPickerListener: CategoryIconPickerListener

    interface CategoryIconPickerListener {
        fun selectIcon(iconID: Int)
    }

    fun setCategoryIconPickerSelector(listener: CategoryIconPickerListener) {
        this.categoryIconPickerListener = listener
    }

}