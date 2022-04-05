package com.param.expensesio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.param.expensesio.MyViewModel
import com.param.expensesio.databinding.CategoryIconBinding
import com.param.expensesio.diffutils.CategoryIconDiffUtil

class AdapterCategoryIconPicker(val vm : MyViewModel, val dialog: AlertDialog) : RecyclerView.Adapter<AdapterCategoryIconPicker.MyViewHolder>() {

    private var list = mutableListOf<Int>()

    inner class MyViewHolder(val binding: CategoryIconBinding) :
        RecyclerView.ViewHolder(binding.root) {

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
        holder.binding.categoryIconIV.setOnClickListener {
            vm.selectedIcon.value = list[position]
            dialog.dismiss()
        }
    }

    override fun getItemCount() = list.size
    fun setList(newList: List<Int>) {
        val diffUtil = CategoryIconDiffUtil(list, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}