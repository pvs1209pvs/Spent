package com.param.expensesio.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.param.expensesio.MyViewModel
import com.param.expensesio.R
import com.param.expensesio.adapter.AdapterCategoryIconPicker
import com.param.expensesio.data.Category
import com.param.expensesio.data.CategoryIcon
import com.param.expensesio.databinding.FragmentAddCategoryBinding
import com.param.expensesio.viewbehavior.ViewBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.item_category.*


class AddCategoryFragment : Fragment() {

    private lateinit var binding: FragmentAddCategoryBinding
    private val viewModel: MyViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false)

        // Confirm add Category
        binding.confirmAddFAB.setOnClickListener {

            val title = binding.catTitle.editText!!.text.toString()
            val budget = binding.catBudget.editText!!.text.toString()

            if (viewModel.isTitleValid(title) && viewModel.isAmountValid(budget)) {
                val category = Category(
                    ofUser = viewModel.userEmail(),
                    title,
                    budget = budget.toFloat(),
                    icon = categoryIcon.tag as Int
                )
                Log.d("AddCategoryFragment.addCategory", "${viewModel.userEmail()} $category")
                viewModel.addCategory(category)
                viewModel.addCategoryIcon(CategoryIcon(title, categoryIcon.tag as Int))
                findNavController().popBackStack()
            } else {
                ViewBehavior.tilErrorMsg(
                    binding.catTitle,
                    viewModel.isTitleValid(title),
                    "Please enter a title"
                )
                ViewBehavior.tilErrorMsg(
                    binding.catTitle,
                    viewModel.isAmountValid(budget),
                    "Please enter a valid number greater than zero"
                )
            }
        }

        binding.catTitle.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.catTitle.error = null
        }

        binding.catBudget.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.catBudget.error = null
        }

        viewModel.selectedIcon.observe(viewLifecycleOwner) {
            binding.categoryIcon.setImageResource(it)
            binding.categoryIcon.tag = it
        }

        binding.changeCategoryIcon.setOnClickListener {
            selectIconDialogBox()
        }

        return binding.root

    }


    private fun selectIconDialogBox() {

        val list = listOf(
            R.drawable.cat_beer,
            R.drawable.cat_book,
            R.drawable.cat_car,
            R.drawable.cat_clothes,
            R.drawable.cat_coffee,
            R.drawable.cat_computer,
            R.drawable.cat_cosmetic,
            R.drawable.cat_drink,
            R.drawable.cat_entertainmmment,
            R.drawable.cat_fitness,
            R.drawable.cat_food,
            R.drawable.cat_fruit,
            R.drawable.cat_gift,
            R.drawable.cat_grocery,
            R.drawable.cat_home,
            R.drawable.cat_hotel,
            R.drawable.cat_icecream,
            R.drawable.cat_laundry,
            R.drawable.cat_medical,
            R.drawable.cat_noodle,
            R.drawable.cat_other,
            R.drawable.cat_people,
            R.drawable.cat_phone,
            R.drawable.cat_pill,
            R.drawable.cat_pizza,
            R.drawable.cat_plane,
            R.drawable.cat_restaurant,
            R.drawable.cat_shopping,
            R.drawable.cat_taxi,
            R.drawable.cat_train
        )

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.picker_category_icon, null, false)

        val selectIconDialogBox = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Pick category icon")
            .show()

        val adapterCategoryIconPicker = AdapterCategoryIconPicker(viewModel, selectIconDialogBox)

        dialogView.findViewById<RecyclerView>(R.id.categoryIconPickerRV).apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = adapterCategoryIconPicker
        }

        adapterCategoryIconPicker.setList(list)

    }


}