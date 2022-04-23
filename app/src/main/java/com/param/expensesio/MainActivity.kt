package com.param.expensesio

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.facebook.FacebookSdk
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.param.expensesio.data.Expense
import com.param.expensesio.databinding.ActivityMainBinding
import com.param.expensesio.fragment.HomeFragmentDirections
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController }

    private val viewModel: MyViewModel by viewModels()

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_Expensery)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)

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

        bottomNavFragmentAnimation()

        toggleBottomNavAndToolbarVisibility()



    }

    private fun bottomNavFragmentAnimation() {
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.from_right)
            .setExitAnim(R.anim.to_left)
            .setPopEnterAnim(R.anim.from_left)
            .setPopExitAnim(R.anim.to_right)
            .setPopUpTo(navController.graph.startDestination, false)
            .build()

        binding.bottomNavigationView.setOnItemSelectedListener {
            if (navController.currentDestination?.id != it.itemId) {
                navController.navigate(it.itemId, null, options)
            }
            true
        }
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

    override fun onConfigurationChanged(newConfig: Configuration) {

        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                findViewById<CoordinatorLayout>(R.id.mainHome).setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_100
                    )
                )
                findViewById<CoordinatorLayout>(R.id.mainManageCategories).setBackgroundColor(
                    ContextCompat.getColor(this, R.color.gray_100)
                )
                findViewById<CoordinatorLayout>(R.id.mainBudget).setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_100
                    )
                )
                findViewById<CoordinatorLayout>(R.id.mainAllExpenses).setBackgroundColor(
                    ContextCompat.getColor(this, R.color.gray_100)
                )
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                findViewById<CoordinatorLayout>(R.id.mainHome).setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                findViewById<CoordinatorLayout>(R.id.mainManageCategories).setBackgroundColor(
                    ContextCompat.getColor(this, R.color.black)
                )
                findViewById<CoordinatorLayout>(R.id.mainBudget).setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                findViewById<CoordinatorLayout>(R.id.mainAllExpenses).setBackgroundColor(
                    ContextCompat.getColor(this, R.color.black)
                )
            } // Night mode is active, we're using dark theme
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

    override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()

    fun buildSnackBar(msg: String) {
        Snackbar.make(
            binding.mainLayout,
            msg,
            Snackbar.LENGTH_SHORT
        ).setAnchorView(binding.bottomNavigationView).show()
    }

    @SuppressLint("RestrictedApi")
    fun BS() = navController.backStack
        .map { it.destination }
        .filterNot { it is NavGraph }
        .joinToString(" > ") { it.displayName.split('/')[1] }


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

