package com.param.expensesio

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import com.param.expensesio.fragment.*
import com.facebook.FacebookSdk
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.param.expensesio.databinding.ActivityMainBinding
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController }

    private val viewModel: MyViewModel by viewModels()

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_Expensery)

        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.root)

        // Init Facebook SDK
        FacebookSdk.sdkInitialize(this@MainActivity);

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.historyFragment,
                R.id.analysisFragment,
                R.id.settingsFragment
            )
        )

        binding.bottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        toggleBottomNavAndToolbarVisibility()

    }


    /**
     * Hides BottomNavigationView and Toolbar when on Login Fragment.
     */
    private fun toggleBottomNavAndToolbarVisibility() {

        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    supportActionBar!!.hide()
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    supportActionBar!!.show()
                }
            }

        }

    }

    override fun onStart() {

        super.onStart()

        val user = auth.currentUser

        if (user == null) {
            try {
                Log.d("MainActivity.onStart", "Home -> Login (from home) ${BS()}")
                val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                navController.navigate(action)
            } catch (e: IllegalArgumentException) {
                Log.d("MainActivity.onStart", "popUpToInclusive TrueFalse fix")
            }
        } else {

        }

    }

    @SuppressLint("RestrictedApi")
    fun BS() = navController.backStack
        .map { it.destination }
        .filterNot { it is NavGraph }
        .joinToString(" > ") { it.displayName.split('/')[1] }

    override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()

    fun buildSnackBar(msg: String) {
        Snackbar.make(
            binding.mainLayout,
            msg,
            Snackbar.LENGTH_SHORT
        ).show()
    }


    fun mockDB() {

//        val categoryList = listOf(
//            Category("Food"),
//            Category("Car"),
//            Category("Home"),
//            Category("Phone"),
//        )
//
//        val jan = Calendar.getInstance().apply { set(2022, 0, 9) }
//        val feb = Calendar.getInstance().apply { set(2022, 1, 25) }
////        val march = Calendar.getInstance().apply { set(2022, 2, 12) }
//
//
//        val expenseList = listOf(
//            Expense(title = "Bill", amount = 50f, ofCategory = "Phone", createdOn = jan),
//            Expense(title = "Pepper", amount = 5f, ofCategory = "Food", createdOn = jan),
//
//            Expense(
//                title = "Windshield washer",
//                amount = 5f,
//                ofCategory = "Car",
//                createdOn = feb
//            ),
//            Expense(title = "Rent", amount = 550f, ofCategory = "Home", createdOn = feb),
//            Expense(title = "Bed", amount = 700f, ofCategory = "Home", createdOn = feb),
//
////            Expense(title = "Gas", amount = 60f, ofCategory = "Car", createdOn = march),
////            Expense(title = "Cilantro", amount = 2f, ofCategory = "Food", createdOn = march),
////            Expense(title = "Rice", amount = 10f, ofCategory = "Food", createdOn = march),
////            Expense(title = "Chocolates", amount = 5f, ofCategory = "Food", createdOn = march),
//        )
//
//        GlobalScope.async {
//            categoryList.forEach { myViewModel.addCategory(it) }
//        }.invokeOnCompletion {
//            expenseList.forEach { myViewModel.addExpense(it) }
//        }


    }


}

