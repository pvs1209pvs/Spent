package com.param.expensesio.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.param.expensesio.MyViewModel
import com.param.expensesio.R
import com.param.expensesio.adapter.AdapterCategory
import com.param.expensesio.data.Category
import com.param.expensesio.databinding.FragmentHomeBinding
import com.param.expensesio.viewbehavior.ViewBehavior
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDate

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MyViewModel by viewModels()
    private val adapterHome by lazy { AdapterCategory(viewModel) }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        Log.d("HomeFragment.onCreateView", "Welcome Home ${BS()}")

        // Set no data image
        binding.noData.noDataImage.setImageResource(R.drawable.img_empty_box_color_1)

        // Set up recyclerview
        binding.homeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterHome
            itemAnimator = ScaleInBottomAnimator()
            addItemDecoration(ViewBehavior.addMarginToLastItem())
        }

        // Display all categories with total
        val now = LocalDate.now()
        viewModel.categoryWithTotal(
            viewModel.userEmail(),
            now.year,
            now.monthValue,
        ).observe(viewLifecycleOwner) { universe ->

            val categories = universe
                .filter { !(it.title == "Misc" && it.total <= 0) }
                .map { Category(it.ofUser, it.title, it.total, it.budget, it.icon) }

            val monthTotal = String.format(
                resources.getString(R.string.monetary_amount),
                categories.sumOf { it.total.toDouble() },
                viewModel.getCurrency()
            )

            binding.monthlyTotal.text = "Total Spending\n$monthTotal"

            adapterHome.setList(categories)

            ViewBehavior.getNoDataViewVisibility(
                categories,
                binding.noData.noDataImage,
                binding.noData.noDataText
            )

        }

        // Add new expense fab
        binding.addExpenseFAB.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddExpenseFragment(null, null)
            Navigation.findNavController(binding.root).navigate(action)
        }

        // Add Expense
        adapterHome.setOnItemClickExpand(object : AdapterCategory.CategoryOnClickListener {

            override fun onItemClickExpand(category: Category) {
                val action =
                    HomeFragmentDirections.actionHomeFragmentTooExpensesByCategoryFragment(category.title)
                findNavController().navigate(action)
            }

        })


        return binding.root

    }


    @SuppressLint("RestrictedApi")
    fun BS() = findNavController().backStack
        .map { it.destination }
        .filterNot { it is NavGraph }
        .joinToString(" > ") { it.displayName.split('/')[1] }


}

