package com.param.expensesio

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.param.expensesio.data.*
import com.param.expensesio.db.LocalDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.*


class MyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LocalDB.getDatabase(application)
    private val categoryDAO = db.categoryDAO()
    private val expenseDAO = db.expenseDAO()
    private val categoryIconDAO = db.categoryIconDAO()

    var selectedIcon = MutableLiveData(R.drawable.cat_other)

    private val firestore = FirebaseFirestore.getInstance()

    // Category

    fun addCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDAO.addCategory(category)
        }
    }

    fun deleteCategory(categoryTitle: String, ofUser: String) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDAO.delCategory(categoryTitle, ofUser)
        }
    }

    fun readAllCategory(ofUser: String) = categoryDAO.readAllCategory(ofUser)

    fun categoryWithTotal(user: String, y: Int, m: Int) = categoryDAO.categoryWithTotal(user, y, m)

    fun modifyCategoryBudget(title: String, newBudget: Float, ofUser: String) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDAO.updateCategoryBudget(title, newBudget, ofUser)
        }
    }

    fun orderTotalLowestFirst(ofUser: String) = categoryDAO.orderTotalLowestFirst(ofUser)

    fun orderTotalHighestFirst(ofUser: String) = categoryDAO.orderTotalHighestFirst(ofUser)

    fun orderBudgetLowestFirst(ofUser: String) = categoryDAO.orderBudgetLowestFirst(ofUser)

    fun orderBudgetHighestFirst(ofUser: String) = categoryDAO.orderBudgetHighestFirst(ofUser)

    // Expense

    fun addExpense(expense: Expense) {

        viewModelScope.launch(Dispatchers.IO) {

            // already present?
            val e = expenseDAO.readExpense(expense.title, expense.ofUser)

            if (e == null) {
                println("new add")
                expenseDAO.addExpense(expense)
            } else {
                println("modify")
                // modifies the already present expense with new expense
                expenseDAO.updateTotal(e.title, expense.amount + e.amount, userEmail())
            }

        }

    }

    fun mergeExpense(editedExpense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {

            // Already existing in database
            val e = expenseDAO.readExpense(editedExpense.title, editedExpense.ofUser)

            if (e == null) {
                expenseDAO.updateExpense(editedExpense)
            } else {
                if (e.id != editedExpense.id) {
                    val newAmount = e.amount + editedExpense.amount
                    updateExpenseTotal(e.title, newAmount, editedExpense.ofUser)
                    delExpense(editedExpense)
                } else {
                    expenseDAO.updateExpense(editedExpense)
                }
            }

        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateExpense(expense)
        }
    }


    fun updateExpenseTotal(title: String, newAmount: Float, ofUser: String) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateTotal(title, newAmount, ofUser)
        }
    }

    fun delExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.delExpense(expense)
        }
    }

    fun delExpenseByTitle(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.delExpenseByTitle(title)
        }
    }

    fun readAllExpense(ofUser: String) = expenseDAO.readAllExpense(ofUser)

    fun readAllExpense(category: String, now: Calendar, ofUser: String) =
        expenseDAO.readAllExpense(category, now.get(Calendar.YEAR), now.get(Calendar.MONTH), ofUser)

    fun moveExpensesToMiscCategory(deletedCategory: String) {

        viewModelScope.launch(Dispatchers.IO) {
            categoryDAO.addCategory(Category(ofUser = userEmail(), title = "Misc"))
        }.invokeOnCompletion {
            viewModelScope.launch(Dispatchers.IO) {

                // All the expenses that belonged to deleted category
                val orphanExpenses =
                    expenseDAO.readAllExpenseList(deletedCategory, userEmail())
                        .filter(Expense::isFromNow)

                orphanExpenses.forEach { it.ofCategory = "Misc" }
                orphanExpenses.forEach {
                    expenseDAO.updateCategoryOf(
                        it.title,
                        "Misc",
                        userEmail()
                    )
                }

            }
        }

    }

    fun readAllExpenseFromNow(ofUser: String, now: Calendar) =
        expenseDAO.readAllExpenseFromNow(ofUser, now.get(Calendar.YEAR), now.get(Calendar.MONTH))

    fun orderExpenseAmountHighestFirst(category: String, period: Calendar, ofUser: String) =
        expenseDAO.orderExpenseAmountHighestFirst(
            category,
            period.get(Calendar.YEAR),
            period.get(Calendar.MONTH),
            ofUser
        )

    fun orderExpenseAmountLowestFirst(category: String, period: Calendar, ofUser: String) =
        expenseDAO.orderExpenseAmountLowestFirst(
            category,
            period.get(Calendar.YEAR),
            period.get(Calendar.MONTH),
            ofUser
        )

    // CategoryIcon

    fun addCategoryIcon(categoryIcon: CategoryIcon) {
        viewModelScope.launch {
            categoryIconDAO.addCategoryIcon(categoryIcon)
        }
    }

    fun readAllCategoryIcon() = categoryIconDAO.readAllIcons()

    // Settings

    fun getNameInit(fullName: String): String {
        val name = fullName.split(" ")
        return "${name[0][0]}${name[1][0]}"
    }

    fun getCurrency(): String {

        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplication())

        return if (preferenceManager.getBoolean("useLocalCurrency", true)) {
            getLocalCurrency()
        } else {
            preferenceManager.getString("currencySymbol", "dude")!!
        }

    }

    private fun getLocalCurrency() = Currency.getInstance(Locale.getDefault()).symbol!!

    fun getSortOrder() = PreferenceManager.getDefaultSharedPreferences(getApplication())
        .getString("sortHistoryBy", "-1")

    // FirebaseAuth

    fun userEmail(): String {
        val email = FirebaseAuth.getInstance().currentUser?.email
        return email ?: ""
    }

    // FirebaseFirestore

    fun backupUserCategories(categories: UserCategoryBackup) {
        firestore
            .collection("UsersBack")
            .document(userEmail())
            .collection("Categories")
            .document("cats")
            .set(categories)
            .addOnSuccessListener { Log.d("ViewModel", "user category back up success") }
            .addOnFailureListener { Log.d("ViewModel", "user category back up failure") }
    }

    fun backupUserExpenses(userExpenseBackup: UserExpenseBackup) {
        firestore
            .collection("UsersBack")
            .document(userEmail())
            .collection("Expenses")
            .document("exps")
            .set(userExpenseBackup)
            .addOnSuccessListener { Log.d("ViewModel", "user expense back up success") }
            .addOnFailureListener { Log.d("ViewModel", "user expense back up failure") }
    }

    fun restoreUserCategories() {
        firestore
            .collection("UsersBack")
            .document(userEmail())
            .collection("Categories")
            .document("cats")
            .get()
            .addOnSuccessListener { docSnapshot ->
                if (docSnapshot.exists()) {
                    val allCats =
                        docSnapshot.toObject(UserCategoryBackup::class.java)!!.allCategories
                    allCats.forEach { addCategory(it) }
                }
            }
            .addOnFailureListener {
                Log.d("ViewModel.FirestoreRestore", it.stackTraceToString())
            }
    }

    fun restoreUserExpenses() {
        firestore
            .collection("UsersBack")
            .document(userEmail())
            .collection("Expenses")
            .document("exps")
            .get()
            .addOnSuccessListener { docSnapshot ->
                if (docSnapshot.exists()) {
                    val allExps =
                        docSnapshot.toObject(UserExpenseBackup::class.java)!!.allExpense
                            .map {
                                Expense(
                                    it.ofUser,
                                    it.id,
                                    it.title,
                                    it.amount,
                                    it.ofCategory,
                                    Convertor().stringToCalendar(it.createdOn)
                                )
                            }
                            .forEach {addExpense(it)}
                }
            }
            .addOnFailureListener {
                Log.d("ViewModel.FirestoreRestore.Expenses", it.stackTraceToString())
            }
    }

    fun restoreUserData() {
        firestore
            .collection("UsersBack")
            .document("param")
            .get()
            .addOnSuccessListener {
                println("got the data ${it.data}")
            }
            .addOnFailureListener {
                Log.d("ViewModel", "user data restore failed")
            }
    }

    // Misc

    fun monthName(monthId: Int) = DateFormatSymbols().months[monthId]

    // UI input checks

    fun isTitleValid(text: String) = text.isNotEmpty() && text != ""

    fun isAmountValid(text: String) =
        text.matches("-?\\d+(\\.\\d+)?".toRegex()) && text.toFloat() > 0

    fun isEmailValid(email: CharSequence?): Boolean {
        if (email == null) return false
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPassValid(password: CharSequence?) =
        !password.isNullOrEmpty() && password.isNotBlank()

}