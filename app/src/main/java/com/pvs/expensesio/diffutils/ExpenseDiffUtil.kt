package com.pvs.expensesio.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.pvs.expensesio.data.Expense

class ExpenseDiffUtil(private val oldList: List<Expense>, private val newList: List<Expense>) :

    DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}