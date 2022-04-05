package com.param.expensesio.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.param.expensesio.MyViewModel
import com.param.expensesio.R
import com.param.expensesio.adapter.AdapterCategory
import com.param.expensesio.data.Category
import com.param.expensesio.databinding.FragmentHomeBinding
import com.param.expensesio.viewbehavior.ViewBehavior
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MyViewModel by viewModels()
    private val adapterHome by lazy { AdapterCategory(viewModel) }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
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

        val now = Calendar.getInstance()

        // Display all categories with total
        viewModel.categoryWithTotal(
            viewModel.userEmail(),
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
        ).observe(viewLifecycleOwner) { universe ->

            val categories = universe
                .filter { !(it.title == "Misc" && it.total <= 0) }
                .map { Category(it.ofUser, it.title, it.total, it.budget, it.icon) }

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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_home_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.lowestTotalFirst -> {

            }
//            viewModel.orderTotalLowestFirst(viewModel.userEmail())
//                .observe(viewLifecycleOwner) {
//                    adapterHome.setList(it)
//                }

            R.id.highestTotalFirst -> viewModel.orderTotalHighestFirst(viewModel.userEmail())
                .observe(viewLifecycleOwner) {
                    adapterHome.setList(it)
                }

            R.id.lowestBudgetFirst -> viewModel.orderBudgetLowestFirst(viewModel.userEmail())
                .observe(viewLifecycleOwner) {
                    adapterHome.setList(it)
                }

            R.id.highestBudgetFirst -> viewModel.orderBudgetHighestFirst(viewModel.userEmail())
                .observe(viewLifecycleOwner) {
                    adapterHome.setList(it)
                }

        }

        return super.onOptionsItemSelected(item)

    }

    @SuppressLint("RestrictedApi")
    fun BS() = findNavController().backStack
        .map { it.destination }
        .filterNot { it is NavGraph }
        .joinToString(" > ") { it.displayName.split('/')[1] }


}

