package com.pvs.expensesio.fragment

import android.os.Bundle
import android.view.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pvs.expensesio.MyViewModel
import com.pvs.expensesio.R
import com.pvs.expensesio.adapter.AdapterCategoryIconPicker
import com.pvs.expensesio.data.Category
import com.pvs.expensesio.data.CategoryIcon
import com.pvs.expensesio.databinding.FragmentAddCategoryBinding
import com.pvs.expensesio.viewbehavior.ViewBehavior
import kotlinx.android.synthetic.main.item_category.*

class AddCategoryFragment : Fragment() {

    private lateinit var binding: FragmentAddCategoryBinding
    private val viewModel: MyViewModel by viewModels()
    private val args: AddCategoryFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(args.categoryToEdit != null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAddCategoryBinding.inflate(inflater, container, false)

        val categoryToEdit = args.categoryToEdit

        if (categoryToEdit != null) {
            binding.categoryIcon.setImageResource(categoryToEdit.icon) // Set image
            binding.categoryIcon.tag = categoryToEdit.icon // Set image tag
            binding.catTitle.isEnabled = false
            binding.catTitle.editText!!.setText(categoryToEdit.title)
            binding.catBudget.editText!!.setText(categoryToEdit.budget.toString())
        } else {
            binding.categoryIcon.tag = R.drawable.cat_other
        }

        // Confirm add Category
        binding.confirmAddFAB.setOnClickListener {

            val title = binding.catTitle.editText!!.text.toString()
            val budget = binding.catBudget.editText!!.text.toString()

            if (viewModel.isTitleValid(title) && viewModel.isAmountValid(budget) && title.lowercase() != "misc") {

                when (categoryToEdit) {
                    null -> {
                        val category = Category(
                            ofUser = viewModel.userEmail(),
                            title = title,
                            budget = budget.toFloat(),
                            icon = categoryIcon.tag as Int
                        )

                        viewModel.addCategory(category)
                        viewModel.addCategoryIcon(CategoryIcon(title, categoryIcon.tag as Int))
                    }
                    else -> {
                        categoryToEdit.budget = budget.toFloat()
                        categoryToEdit.icon = binding.categoryIcon.tag as Int
                        viewModel.updateCategory(categoryToEdit)
                    }
                }

                findNavController().popBackStack()

            } else {

                ViewBehavior.tilErrorMsg(
                    binding.catTitle,
                    title.lowercase() != "misc",
                    "Misc category name is reserved"
                )

                ViewBehavior.tilErrorMsg(
                    binding.catTitle,
                    viewModel.isTitleValid(title),
                    "Please enter a title"
                )
                ViewBehavior.tilErrorMsg(
                    binding.catBudget,
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

        val adapterCategoryIconPicker = AdapterCategoryIconPicker()

        dialogView.findViewById<RecyclerView>(R.id.categoryIconPickerRV).apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = adapterCategoryIconPicker
        }

        adapterCategoryIconPicker.setList(list)

        // Change category profile icon
        adapterCategoryIconPicker.setCategoryIconPickerSelector(object :
            AdapterCategoryIconPicker.CategoryIconPickerListener {
            override fun selectIcon(iconID: Int) {
                setCategoryDisplayIcon(iconID)
                selectIconDialogBox.dismiss()
            }
        })

    }

    private fun setCategoryDisplayIcon(iconId: Int) {
        binding.categoryIcon.setImageResource(iconId)
        binding.categoryIcon.tag = iconId
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.pop_up_del, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.delManageCategory -> {
                val deletedCategoryTitle = args.categoryToEdit!!.title
                viewModel.deleteCategory(deletedCategoryTitle, viewModel.userEmail())
                viewModel.moveExpensesToMiscCategory(deletedCategoryTitle)
                findNavController().popBackStack()
            }

        }

        return super.onOptionsItemSelected(item)

    }

}