package com.pvs.spent

import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.history_expandable_header.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class HistoryHeader(private val yearMonth: Pair<Month, Int>, private val locale: Locale,private val historyTotal : String) : Item(), ExpandableItem {

    private lateinit var expandableGroup: ExpandableGroup

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val (month, year) = yearMonth
        viewHolder.crntMonth.text = "${month.getDisplayName(TextStyle.FULL, locale)} $year"

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