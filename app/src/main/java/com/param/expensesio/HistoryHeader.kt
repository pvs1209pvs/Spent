package com.param.expensesio

import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.history_expandable_header.*
import java.text.DateFormatSymbols

class HistoryHeader(private val yearMonth: Pair<Int, Int>, private val historyTotal : String) : Item(), ExpandableItem {

    private lateinit var expandableGroup: ExpandableGroup

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val month = DateFormatSymbols().months[yearMonth.first]
        val year = yearMonth.second
        viewHolder.crntMonth.text = "$month $year"

        viewHolder.historyTotal.text = historyTotal

        viewHolder.imageButton.setImageResource(getIconResId())

        viewHolder.imageButton.setOnClickListener {
            expandableGroup.onToggleExpanded()
            viewHolder.imageButton.setImageResource(getIconResId())
        }
    }

    override fun getLayout() = R.layout.history_expandable_header

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        expandableGroup = onToggleListener
    }

    private fun getIconResId() =
        if (expandableGroup.isExpanded) R.drawable.ic_baseline_keyboard_arrow_down_24 else R.drawable.ic_baseline_keyboard_arrow_right_24

}