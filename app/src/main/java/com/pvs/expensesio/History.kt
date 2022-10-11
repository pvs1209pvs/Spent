package com.pvs.expensesio

import com.pvs.expensesio.R
import com.pvs.expensesio.data.Expense
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_history.*

class History(private val expense: Expense, private val currency: String, val icon: Int) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.imageView.setImageResource(icon)
        viewHolder.historyTitle.text = expense.title
        viewHolder.historyAmount.text = if (currency.length == 1)
            String.format(
                viewHolder.itemView.context.resources.getString(R.string.monetary_amount),
                expense.amount,
                currency
            )
        else
            String.format(
                viewHolder.itemView.context.resources.getString(R.string.monetary_amount_long),
                expense.amount,
                currency
            )

    }

    override fun getLayout() = R.layout.item_history

    override fun getSpanSize(spanCount: Int, position: Int) = spanCount

}