package com.pvs.spent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pvs.spent.databinding.FragmentBudgetBinding
import com.pvs.spent.MainActivity
import com.pvs.spent.MyViewModel
import com.pvs.spent.R
import com.pvs.spent.adapter.AdapterBudget
import com.pvs.spent.viewbehavior.ViewBehavior
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator


class BudgetFragment : Fragment() {

    private lateinit var binding: FragmentBudgetBinding
    private val viewModel: MyViewModel by viewModels()
    private val adapterBudget by lazy { AdapterBudget(viewModel) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBudgetBinding.inflate(inflater, container, false)

        // Set no data image
        binding.noData.noDataImage.setImageResource(R.drawable.img_empty_box_color_5)

        // Set up recyclerview
        binding.budgetRV.apply {
            adapter = adapterBudget
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = ScaleInBottomAnimator()
            addItemDecoration(ViewBehavior.addMarginToLastItem())
        }

        // Display all categories on recyclerview
        viewModel.readAllCategory(viewModel.userEmail()).observe(viewLifecycleOwner) { allCats ->

            val x = allCats.filter { it.title != "Misc" }

            adapterBudget.setList(x)

            // Saves new budget to ROOM
            binding.checkFAB.setOnClickListener { _ ->

                val zeroBudgetCount = x.count {
                    it.budget <= 0f
                }

                if (zeroBudgetCount == 0) {
                    x.forEach { viewModel.modifyCategoryBudget(it.title, it.budget, viewModel.userEmail()) }
                    findNavController().popBackStack()
                } else {
                    (requireActivity() as MainActivity).buildSnackBar("Category budget cannot be set to zero")
                }

            }

            ViewBehavior.getNoDataViewVisibility(
                x,
                binding.noData.noDataImage,
                binding.noData.noDataText
            )

        }

        return binding.root

    }





}