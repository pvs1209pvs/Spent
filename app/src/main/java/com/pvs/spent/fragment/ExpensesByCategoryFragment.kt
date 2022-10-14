package com.pvs.spent.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.pvs.spent.databinding.FragmentExpensesByCategoryBinding
import com.pvs.spent.MyViewModel
import com.pvs.spent.R
import com.pvs.spent.adapter.AdapterExpenses
import com.pvs.spent.data.Expense
import com.pvs.spent.data.LocalDate
import com.pvs.spent.encryption.AES
import com.pvs.spent.encryption.AES.decrypt
import com.pvs.spent.encryption.AES.encrypt
import com.pvs.spent.viewbehavior.ViewBehavior
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
            LocalDate.now(),
            viewModel.userEmail()
        )
            .observe(viewLifecycleOwner) { allExpenses ->

                adapterExpenses.setList(allExpenses)

                ViewBehavior.getNoDataViewVisibility(
                    allExpenses,
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