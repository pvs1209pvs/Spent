package com.param.expensesio.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.param.expensesio.MyViewModel
import com.param.expensesio.R
import com.param.expensesio.data.Expense
import com.param.expensesio.databinding.FragmentAnalysisBinding
import com.param.expensesio.viewbehavior.ViewBehavior
import java.util.*


class AnalysisFragment : Fragment() {

    private lateinit var binding: FragmentAnalysisBinding

    private val viewModel: MyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAnalysisBinding.inflate(inflater, container, false)

        // Set no data image
        binding.noData.noDataImage.setImageResource(R.drawable.img_empty_box_color_7)

        val monthNameToInt = listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December",
        )

        autoSetMonthYearSelector(monthNameToInt)

        viewModel.readAllExpenseFromNow(viewModel.userEmail(), Calendar.getInstance())
            .observe(viewLifecycleOwner) {

                ViewBehavior.getNoDataViewVisibility(
                    it,
                    binding.noData.noDataImage,
                    binding.noData.noDataText
                )

                if (it.isNotEmpty()) {
                    makePieChart(it)
                    binding.pieChart.visibility = View.VISIBLE
                }
                else{
                    binding.pieChart.visibility = View.GONE
                }
            }

        binding.monthYearDropDown.setOnItemClickListener { _, _, _, _ ->

            val selectPeriod = Calendar.getInstance().apply {
                set(Calendar.YEAR, getDropDownField(Calendar.YEAR, monthNameToInt))
                set(Calendar.MONTH, getDropDownField(Calendar.MONTH, monthNameToInt))
            }

            viewModel.readAllExpenseFromNow(viewModel.userEmail(), selectPeriod)
                .observe(viewLifecycleOwner) {

                    ViewBehavior.getNoDataViewVisibility(
                        it,
                        binding.noData.noDataImage,
                        binding.noData.noDataText
                    )

                    if (it.isNotEmpty()) {
                        makePieChart(it)
                        binding.pieChart.visibility = View.VISIBLE
                    }
                    else{
                        binding.pieChart.visibility = View.GONE
                    }
                }

        }



        return binding.root

    }


    private fun autoSetMonthYearSelector(monthName: List<String>) {

        val monthYear = Calendar.getInstance()

        val month = monthName[monthYear.get(Calendar.MONTH)]
        val year = monthYear.get(Calendar.YEAR)

        binding.monthYearDropDown.setText("$month $year")

    }

    private fun makePieChart(expenses: List<Expense>) {

        val totalMoneySpend = expenses.sumOf { it.amount.toDouble() }.toFloat()

        val entries = expenses.groupBy { it.ofCategory }.map { groupedExp ->
            val total = groupedExp.value.sumOf { it.amount.toDouble() }.toFloat()
            PieEntry(total / totalMoneySpend, groupedExp.key)
        }

        val availColors = mutableListOf<Int>().apply {
            addAll(ColorTemplate.MATERIAL_COLORS.toList())
            addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
            addAll(ColorTemplate.JOYFUL_COLORS.toList())
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = availColors
        }

        val pieData = PieData(dataSet).apply {
            setDrawValues(true)
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(12f)
            setValueTextColor(Color.BLACK)
        }

        binding.pieChart.apply {
            data = pieData
            invalidate()
            animateY(1000, Easing.EaseInQuad)
            setEntryLabelColor(Color.BLACK)
            description.isEnabled = false
            setDrawSlicesUnderHole(true)
            setUsePercentValues(true)
            dragDecelerationFrictionCoef = 0.15f
            setEntryLabelTextSize(12f)
            setCenterTextSize(24f)
            isDrawHoleEnabled = true
            setHoleColor(ContextCompat.getColor(requireContext(), R.color.theme_color))
        }

        binding.pieChart.legend.apply {
            form = Legend.LegendForm.CIRCLE
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            textSize = 10f
            formSize = 10f
            formToTextSpace = 2f
            isWordWrapEnabled = true
            textColor = ContextCompat.getColor(requireContext(), R.color.theme_opposite_color)
        }

    }

    private fun getDropDownField(field: Int, monthName: List<String>): Int {

        val dropDownText = binding.monthYearDropDown.text.toString().split(" ")

        return when (field) {
            Calendar.YEAR -> dropDownText[1].toInt()
            Calendar.MONTH -> monthName.indexOf(dropDownText[0])
            else -> -1
        }

    }

    override fun onResume() {
        super.onResume()

        viewModel.readAllExpense(viewModel.userEmail()).observe(viewLifecycleOwner) { allExpenses ->

            val allMonthYear = allExpenses.map {
                "${viewModel.monthName(it.createdOn.get(Calendar.MONTH))} " +
                        "${it.createdOn.get(Calendar.YEAR)}"
            }.distinct()

            binding.monthYearDropDown.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.item_dropdown_category,
                    allMonthYear
                )
            )

        }

    }


}