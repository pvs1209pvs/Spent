package com.param.expensesio.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.param.expensesio.data.Category

class CategoryDiffUtil(private val oldList: List<Category>, private val newList: List<Category>) :

    DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].title == newList[newItemPosition].title

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}
