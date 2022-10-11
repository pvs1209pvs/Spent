package com.pvs.expensesio.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pvs.expensesio.databinding.FragmentManageCategoryBinding
import com.pvs.expensesio.MyViewModel
import com.pvs.expensesio.R
import com.pvs.expensesio.adapter.AdapterManageCategory
import com.pvs.expensesio.data.Category
import com.pvs.expensesio.viewbehavior.ViewBehavior
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator


class ManageCategoryFragment : Fragment() {

    private lateinit var binding: FragmentManageCategoryBinding
    private val adapterManageCategory by lazy { AdapterManageCategory() }
    private val viewModel: MyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentManageCategoryBinding.inflate(inflater, container, false)

        // Set no data image
        binding.noData.noDataImage.setImageResource(R.drawable.img_empty_box_color_5)

        // Set up RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterManageCategory
            itemAnimator = ScaleInBottomAnimator()
            addItemDecoration(ViewBehavior.addMarginToLastItem())
        }

        // Display all categories (except Misc if exists) on RecyclerView
        viewModel.readAllCategory(viewModel.userEmail()).observe(viewLifecycleOwner) {
            it.removeIf { cat -> cat.title == "Misc" }
            adapterManageCategory.setList(it)
            ViewBehavior.getNoDataViewVisibility(
                it,
                binding.noData.noDataImage,
                binding.noData.noDataText
            )
            Log.d("ManageCategoryFragment.readAllCategory", "${viewModel.userEmail()} $it")
        }

        // Add new category
        binding.floatingActionButton.setOnClickListener {
            val action =
                ManageCategoryFragmentDirections.actionManageCategoryFragmentToAddCategoryFragment(
                    null
                )
            Navigation.findNavController(binding.root).navigate(action)
        }

        // Edit manage category callback
        adapterManageCategory.setEditOnClickListener(object :
            AdapterManageCategory.EditOnClickListener {
            override fun editListener(category: Category) {
                val action =
                    ManageCategoryFragmentDirections.actionManageCategoryFragmentToAddCategoryFragment(
                        category
                    )
                findNavController().navigate(action)
            }

        })

        return binding.root

    }

}