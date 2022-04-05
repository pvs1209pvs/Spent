package com.param.expensesio.diffutils

import androidx.recyclerview.widget.DiffUtil

class CategoryIconDiffUtil(
    private val oldList: List<Int>,
    private val newList: List<Int>
) :

    DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}