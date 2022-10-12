package com.pvs.spent

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pvs.spent.data.*
import com.pvs.spent.db.Convertor
import com.pvs.spent.db.LocalDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.*


class MyViewModel(application: Application) : AndroidViewModel(application) {

    private val FIRESTORE_DB = "UserBackup"
    private val CATEGORY_COLLECTION = "Categories"
    private val EXPENSE_COLLECTION = "Expenses"
    private val CATEGORY_DOC = "cats"
    private val EXPENSE_DOC = "exps"

    private val db = LocalDB.getDatabase(application)
    private val categoryDAO = db.categoryDAO()
    private val expenseDAO = db.expenseDAO()
    private val categoryIconDAO = db.categoryIconDAO()

    private val firestore = FirebaseFirestore.getInstance()

    val restoreStat = MutableLiveData(0)

    val backupStat = MutableLiveData(false)

    // Category

    fun addCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDAO.addCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDAO.updateCategory(category)
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

    // Expense

    fun addExpense(expense: Expense) {

        viewModelScope.launch(Dispatchers.IO) {

            // already present?
            val e = expenseDAO.readExpense(
                expense.title,
                expense.ofUser,
                expense.createdOn.year,
                expense.createdOn.monthValue
            )

            if (e == null) {
                Log.d("MyViewModel.addExpense", "Add")
                expenseDAO.addExpense(expense)
            } else {
                Log.d("MyViewMode.addExpense", "modify")
                // modifies the already present expense with new expense
                expenseDAO.updateTotal(e.title, expense.amount + e.amount, userEmail())
            }

        }

    }

    fun mergeExpense(editedExpense: Expense) {

        viewModelScope.launch(Dispatchers.IO) {

            // Already existing in database
            val e = expenseDAO.readExpense(
                editedExpense.title,
                editedExpense.ofUser,
                editedExpense.createdOn.year,
                editedExpense.createdOn.monthValue
            )

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

    private fun updateExpense(expense: Expense) {
        Log.d(javaClass.canonicalName, "updating $expense")
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateExpense(expense)
        }
    }

    private fun updateExpenseTotal(title: String, newAmount: Float, ofUser: String) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateTotal(title, newAmount, ofUser)
        }
    }

    fun delExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) { expenseDAO.delExpense(expense) }
    }

    fun delExpenseByTitle(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.delExpenseByTitle(title)
        }
    }

    fun readAllExpense(ofUser: String) = expenseDAO.readAllExpense(ofUser)

    fun readAllExpenseFromNow(ofUser: String, now: LocalDate) =
        expenseDAO.readAllExpenseFromNow(ofUser, now.year, now.monthValue)

    fun readAllExpense(category: String, now: LocalDate, ofUser: String) =
        expenseDAO.readAllExpense(category, now.year, now.monthValue, ofUser)

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

    fun getUnbackedUpExpenses(ofUser: String) = expenseDAO.getUnbackedUpExpenses(ofUser)

    fun expenseCount(ofUser: String, now: LocalDate) =
        expenseDAO.expenseCount(ofUser, now.year, now.monthValue)

    fun expenseCountOld(ofUser: String, now: LocalDate) =
        expenseDAO.expenseCountOld(ofUser, now.year, now.monthValue)

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

    fun <P, R> CoroutineScope.executeAsyncTask(
        doInBackground: suspend (suspend (P) -> Unit) -> R,
        onPostExecute: (R) -> Unit,
    ) = launch {

        val result = withContext(Dispatchers.IO) {
            doInBackground {
            }
        }
        onPostExecute(result)
    }

    // FirebaseFirestore

    // Backup

    fun backupUserCategories(categories: UserCategoryBackup) {

        firestore
            .collection(FIRESTORE_DB)
            .document(userEmail())
            .collection(CATEGORY_COLLECTION)
            .document(CATEGORY_DOC)
            .set(categories)
            .addOnSuccessListener {
                Log.d("ViewModel", "user category back up success")
            }
            .addOnFailureListener {
                Log.d("ViewModel", "user category back up failure")
            }

    }

    fun backupUserExpenses(unwarranted: List<Expense>) {

        backupStat.value = false


        val userEmailDocRef = firestore
            .collection(FIRESTORE_DB)
            .document(userEmail())

        val expenseDoc = userEmailDocRef
            .collection(EXPENSE_COLLECTION)
            .document(EXPENSE_DOC)

        expenseDoc.get().addOnSuccessListener {

            if (it.exists()) {
                Log.d(javaClass.canonicalName, "adding to existing doc snapshot")
                backupAllExpenses(unwarranted, expenseDoc)
            } else {
                Log.d(javaClass.canonicalName, "creating new doc snapshot and adding")
                expenseDoc
                    .set(mapOf("values" to listOf<ExpenseFirestore>()))
                    .addOnSuccessListener { backupAllExpenses(unwarranted, expenseDoc) }
            }

            Log.d(javaClass.canonicalName, "start marking expenses as backed up")
            markAsBackedUp(unwarranted)
            Log.d(javaClass.canonicalName, "finish marking expenses as backed up")


        }.addOnFailureListener {
            println(it.toString())
        }

    }

    private fun backupAllExpenses(unwarranted: List<Expense>, expenseDoc: DocumentReference) {
        Log.d(javaClass.canonicalName, "starting backing up up all expenses")
        unwarranted.map { exp ->
            ExpenseFirestore(
                exp.ofUser,
                exp.id,
                exp.title,
                exp.amount,
                exp.ofCategory,
                Convertor().calendarToString(exp.createdOn),
                exp.backedUp
            )
        }.forEach { expF -> backUpExpense(expenseDoc, expF) }
        Log.d(javaClass.canonicalName, "finished backing up all expenses")
    }

    // TODO Backup status should be moved to backupUserExpenses
    private fun backUpExpense(dr: DocumentReference, p: ExpenseFirestore) {

        Log.d(javaClass.canonicalName, "backing up $p")

        dr.update("values", FieldValue.arrayUnion(p))
            .addOnSuccessListener {
                Log.d(javaClass.canonicalName, "$p backup success")
            }
            .addOnFailureListener {
                Log.d(javaClass.canonicalName, "$p backup fail $it")
            }

    }


    private fun updateAll(list: List<Expense>) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateExpenseBatch(list)
        }.invokeOnCompletion {
            backupStat.postValue(true)
        }
    }

    private fun markAsBackedUp(expense: List<Expense>) {
        expense.forEach { it.backedUp = 1 }
        updateAll(expense)
    }

    // Restore

    fun restoreUserCategories() {
        firestore
            .collection(FIRESTORE_DB)
            .document(userEmail())
            .collection(CATEGORY_COLLECTION)
            .document(CATEGORY_DOC)
            .get()
            .addOnSuccessListener { docSnapshot ->
                if (docSnapshot.exists()) {
                    val allCats =
                        docSnapshot.toObject(UserCategoryBackup::class.java)!!.allCategories
                    allCats.forEach {
                        println("category restore ${it.title}")
                        addCategory(it)
                    }
                    restoreStat.value = restoreStat.value!! + 1
                }
            }
            .addOnFailureListener {
                Log.d("ViewModel.FirestoreRestore", it.stackTraceToString())
                restoreStat.value = restoreStat.value!! - 2
            }
    }

    fun restoreUserExpenses() {

        firestore
            .collection(FIRESTORE_DB)
            .document(userEmail())
            .collection(EXPENSE_COLLECTION)
            .document(EXPENSE_DOC)
            .get()
            .addOnSuccessListener { docSnapshot ->
                if (docSnapshot.exists()) {
                    println("nice tits")
                    docSnapshot.toObject(UserExpenseBackup::class.java)!!.values
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
                        .forEach {
                            println("expense restore ${it.title}")
                            addExpense(it)
                        }
                    restoreStat.value = restoreStat.value!! + 1
                }
            }
            .addOnFailureListener {
                Log.d("ViewModel.FirestoreRestore.Expenses", it.stackTraceToString())
                restoreStat.value = restoreStat.value!! - 2
            }
    }

    // UI input checks

    fun isTitleValid(text: String) = text.isNotEmpty() && text != ""

    fun isAmountValid(text: String) =
        text.matches("-?\\d+(\\.\\d+)?".toRegex()) && text.toFloat() > 0

    fun isEmailValid(email: CharSequence?): Boolean {
        if (email == null) return false
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPassValid(password: CharSequence?) = !password.isNullOrEmpty() && password.isNotBlank()

    /**
     * Format number of adding locale's digit separator and limit number of  decimal places to 2.
     * @param number Number to format
     * @return Returns formatted string.
     */
    fun formatNumber(number: Number): String {

        Log.d("MyViewModel -> formatNumber", number.toString())

        val nf = java.text.NumberFormat.getInstance()

        nf.maximumFractionDigits = 2

        val formattedNumber = nf.format(number)

        Log.d("MyViewModel -> formatNumber", formattedNumber)

        return formattedNumber

    }

}