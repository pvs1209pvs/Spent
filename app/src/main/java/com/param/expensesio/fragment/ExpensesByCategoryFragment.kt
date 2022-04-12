package com.param.expensesio.fragment

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.param.expensesio.MyViewModel
import com.param.expensesio.R
import com.param.expensesio.adapter.AdapterExpenses
import com.param.expensesio.data.Expense
import com.param.expensesio.databinding.FragmentExpensesByCategoryBinding
import com.param.expensesio.viewbehavior.ViewBehavior
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator
import java.util.*


class ExpensesByCategoryFragment : Fragment() {

    private lateinit var binding: FragmentExpensesByCategoryBinding
    private val args: ExpensesByCategoryFragmentArgs by navArgs()

    private val adapterExpenses by lazy { AdapterExpenses(viewModel) }

    private val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExpensesByCategoryBinding.inflate(inflater, container, false)



        // Set no data image
        binding.noData.noDataImage.setImageResource(R.drawable.img_empty_box_color_2)

        // Set up recyclerview
        binding.allExpensesRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterExpenses
            itemAnimator = ScaleInBottomAnimator()
            addItemDecoration(ViewBehavior.addMarginToLastItem())
        }

        // Display all expenses by category on recyclerview
        viewModel.readAllExpense(
            args.categoryTitleArg,
            Calendar.getInstance(),
            viewModel.userEmail()
        )
            .observe(viewLifecycleOwner) { allExpenses ->

                val crntMonthYearExpenses = allExpenses.filter(Expense::isFromNow)
                adapterExpenses.setList(crntMonthYearExpenses)

                ViewBehavior.getNoDataViewVisibility(
                    crntMonthYearExpenses,
                    binding.noData.noDataImage,
                    binding.noData.noDataText
                )
            }

        // Quick-add expense
        binding.addExpense.setOnClickListener {
            val action =
                ExpensesByCategoryFragmentDirections.actionExpenseByCategoryFragmentToAddExpenseFragment(
                    null,
                    args.categoryTitleArg
                )
            findNavController().navigate(action)
        }

        // Callback for popup menu
        adapterExpenses.setDelOnClickListener(object : AdapterExpenses.PopUpMenuListener {

            override fun editListener(expense: Expense) {
                val action =
                    ExpensesByCategoryFragmentDirections.actionExpenseByCategoryFragmentToAddExpenseFragment(
                        expense,
                        null
                    )
                findNavController().navigate(action)
            }

        })


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_all_expenses, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.highestExpenseAmountFirst ->
                viewModel.orderExpenseAmountHighestFirst(
                    args.categoryTitleArg,
                    Calendar.getInstance(),
                    viewModel.userEmail()
                ).observe(viewLifecycleOwner) { adapterExpenses.setList(it) }

            R.id.lowestExpenseAmountFirst ->
                viewModel.orderExpenseAmountLowestFirst(
                    args.categoryTitleArg,
                    Calendar.getInstance(),
                    viewModel.userEmail()
                ).observe(viewLifecycleOwner) { adapterExpenses.setList(it) }

        }

        return super.onOptionsItemSelected(item)

    }

}