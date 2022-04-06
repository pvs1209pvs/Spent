package com.param.expensesio.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.param.expensesio.MyViewModel
import com.param.expensesio.R
import com.param.expensesio.data.Expense
import com.param.expensesio.databinding.FragmentAddExpenseBinding
import com.param.expensesio.viewbehavior.ViewBehavior
import java.util.*


class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding
    private val viewModel: MyViewModel by viewModels()
    private val args: AddExpenseFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false)

        // Nav-args
        val expenseToEdit = args.expenseToEdit
        val quickFillCategory = args.quickFillCategory

        // Automatically fill Expense fields
        if (expenseToEdit != null) {
            binding.expenseTitle.editText!!.setText(expenseToEdit.title)
            binding.expenseAmount.editText!!.setText(expenseToEdit.amount.toString())
            binding.categoryDropDownTitle.setText(expenseToEdit.ofCategory)
        }

        // Automatically fill Category field
        if (quickFillCategory != null) {
            binding.categoryDropDownTitle.setText(quickFillCategory)
        }

        // Add or update Expense
        binding.expenseAddConfirmFAB.setOnClickListener {

            val title = binding.expenseTitle.editText!!.text.toString()
            val amount = binding.expenseAmount.editText!!.text.toString()
            val ofCategory = binding.categoryDropDownTitle.text.toString()

            if (title.isNotEmpty() && amount.isNotEmpty()) {

                when (expenseToEdit) {
                    null -> { // add
                        val expense = Expense(
                            ofUser = viewModel.userEmail(),
                            title = title,
                            amount = amount.toFloat(),
                            ofCategory = ofCategory,
                            createdOn = Calendar.getInstance()
                        )
                        viewModel.addExpense(expense)
                    }
                    else -> { // edit
                        expenseToEdit.title = title
                        expenseToEdit.amount = amount.toFloat()
                        expenseToEdit.ofCategory = ofCategory
                        viewModel.mergeExpense(expenseToEdit)
                    }

                }


                findNavController().popBackStack()

            } else {

                ViewBehavior.tilErrorMsg(
                    binding.expenseTitle,
                    viewModel.isTitleValid(title),
                    "Please enter a valid title"
                )

                ViewBehavior.tilErrorMsg(
                    binding.expenseAmount,
                    viewModel.isAmountValid(amount),
                    "Please enter a valid number greater than zero"
                )

                ViewBehavior.tilErrorMsg(
                    binding.categoryDropDown,
                    viewModel.isTitleValid(ofCategory),
                    "Please select a category"
                )

            }

        }

        binding.expenseTitle.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.expenseTitle.error = null
        }

        binding.expenseAmount.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.expenseAmount.error = null
        }

        binding.categoryDropDownTitle.doOnTextChanged { _, _, _, _ ->
            binding.categoryDropDown.error = null
        }

        return binding.root

    }


    override fun onResume() {
        super.onResume()

        viewModel.readAllCategory(viewModel.userEmail()).observe(viewLifecycleOwner) { allCats ->

            binding.categoryDropDownTitle.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.item_dropdown_category,
                    allCats.map { it.title }.filter { it != "Misc" }.toList()
                )
            )
        }

    }

}