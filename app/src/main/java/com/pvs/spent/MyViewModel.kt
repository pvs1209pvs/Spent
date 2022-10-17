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
import com.google.gson.Gson
import com.pvs.spent.data.*
import com.pvs.spent.db.Convertor
import com.pvs.spent.db.LocalDB
import com.pvs.spent.encryption.AES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import com.pvs.spent.data.CreationPeriod


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
                Log.d("MyViewModel.addExpense", "Add $expense")
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
        Log.d(javaClass.canonicalName, "Updating $expense")
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateExpense(expense)
        }
    }

    private fun updateExpenseTotal(title: String, newAmount: Float, ofUser: String) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateTotal(title, newAmount, ofUser)
        }
    }

    private fun updateAll(list: List<Expense>) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDAO.updateExpenseBatch(list)
        }.invokeOnCompletion {
            backupStat.postValue(true)
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

    fun readAllExpenseFromNow(ofUser: String, now: CreationPeriod) =
        expenseDAO.readAllExpenseFromNow(ofUser, now.year, now.monthValue)

    fun readAllExpense(category: String, now: CreationPeriod, ofUser: String) =
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

    fun expenseCount(ofUser: String, now: CreationPeriod) =
        expenseDAO.expenseCount(ofUser, now.year, now.monthValue)

    fun expenseCountOld(ofUser: String, now: CreationPeriod) =
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

    fun userEmail() = FirebaseAuth.getInstance().currentUser?.email ?: ""


    // Firebase FireStore


    fun backupUserCategories(unwarranted: List<Category>) {

        Log.d(javaClass.canonicalName, "Backing up encrypted category (${unwarranted.size} items)")

        val categoryDocRef = firestore
            .collection(FIRESTORE_DB)
            .document(userEmail())
            .collection(CATEGORY_COLLECTION)
            .document(CATEGORY_DOC)

        categoryDocRef.get().addOnSuccessListener {
            if (it.exists()) {
                Log.d(javaClass.canonicalName, "Adding to existing doc snapshot.")
                unwarranted.forEach { category ->
                    backupCategoryEncrypted(category, categoryDocRef)
                }
            } else {
                Log.d(javaClass.canonicalName, "Creating new doc snapshot + adding")
                categoryDocRef
                    .set(mapOf("values" to listOf<ExpenseFirestore>()))
                    .addOnSuccessListener {
                        unwarranted.forEach { category ->
                            backupCategoryEncrypted(category, categoryDocRef)
                        }
                    }
            }

        }

    }

    private fun backupCategoryEncrypted(category: Category, docRef: DocumentReference){

        Log.d(javaClass.canonicalName, "Backing up encrypted expense $category.")

        val sk = AES.generateKey(userEmail(), userEmail())
        val iv = AES.generateIV()
        val plainText = Gson().toJson(category)
        val cipher = AES.encrypt(plainText, sk, iv)
        val categoryEncrypted =  AESCategory(cipher, Convertor().ivToString(iv))

        docRef.update("values", FieldValue.arrayUnion(categoryEncrypted))
            .addOnSuccessListener {
                Log.d(javaClass.canonicalName, "$category backup succeed")
            }
            .addOnFailureListener {
                Log.d(javaClass.canonicalName, "$category backup failed $it")
            }

    }

    fun restoreUserCategories() {

        Log.d(javaClass.canonicalName, "Restoring categories")

        val firestore = firestore
            .collection(FIRESTORE_DB)
            .document(userEmail())


        val categoryDocRef = firestore
            .collection(CATEGORY_COLLECTION)
            .document(CATEGORY_DOC)


        categoryDocRef
            .get()
            .addOnSuccessListener { docSnapshot ->
                if (docSnapshot.exists()) {

                    Log.d(javaClass.canonicalName, "DocSnapshot exists")

                    val values =
                        (docSnapshot.get("values") as List<*>).filterIsInstance<Map<String, String>>()

                    val category : List<Category> = values.map {
                        val cipher = it.values.first()
                        val sk = AES.generateKey(userEmail(),userEmail())
                        val iv = Convertor().stringToIv(it.values.last())
                        val plainText = AES.decrypt(cipher,sk, iv)
                        Gson().fromJson(plainText,Category::class.java)
                    }

                    category.forEach {
                        addCategory(it)
                    }

//                    restoreStat.value = restoreStat.value!! + 1
                }
                else{
                    Log.d(javaClass.canonicalName, "DocSnapshot DOESN'T exists")

                }
            }
            .addOnFailureListener {
                Log.d(javaClass.canonicalName, "$categoryDAO doc ref get failed due to ${it.stackTraceToString()}")
//                restoreStat.value = restoreStat.value!! - 2
            }
    }

    fun backupExpenseEncrypted(unwarranted: List<Expense>) {

        Log.d(javaClass.canonicalName, "Starting backup (${unwarranted.size} items)")

        val userEmailDocRef = firestore
            .collection(FIRESTORE_DB)
            .document(userEmail())

        val expenseDoc = userEmailDocRef
            .collection(EXPENSE_COLLECTION)
            .document(EXPENSE_DOC)

        expenseDoc.get().addOnSuccessListener {
            if (it.exists()) {
                Log.d(javaClass.canonicalName, "Adding to existing doc snapshot.")
                unwarranted.forEach { exp ->
                    backupExpenseEncrypted(expenseDoc, exp)
                }
            } else {
                Log.d(javaClass.canonicalName, "Creating new doc snapshot + adding")
                expenseDoc
                    .set(mapOf("values" to listOf<ExpenseFirestore>()))
                    .addOnSuccessListener {
                        unwarranted.forEach { exp ->
                            backupExpenseEncrypted(expenseDoc, exp)
                        }
                    }
            }

        }
    }

    private fun backupExpenseEncrypted(docRef: DocumentReference, expense: Expense) {

        Log.d(javaClass.canonicalName, "Backing up encrypted expense $expense.")

        val sk = AES.generateKey(userEmail(), userEmail())
        val iv = AES.generateIV()
        val plainText = Gson().toJson(expense)
        val cipher = AES.encrypt(plainText, sk, iv)

        Log.d(javaClass.canonicalName, "toJSON $plainText")

        val encryptedExpense = EncryptedExpense(cipher, Convertor().ivToString(iv))

        docRef.update("values", FieldValue.arrayUnion(encryptedExpense))
            .addOnSuccessListener {
                Log.d(javaClass.canonicalName, "$expense backup succeed")
                expense.backedUp = 1
                updateExpense(expense)
            }
            .addOnFailureListener {
                Log.d(javaClass.canonicalName, "$expense backup failed $it")
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

                    Log.d(javaClass.canonicalName, "Expense docSnapshot exists")

                    val values =
                        (docSnapshot.get("values") as List<*>).filterIsInstance<Map<String, String>>()

                    val exps: List<Expense> = values.map {
                        Log.d(
                            javaClass.canonicalName,
                            "Decryption -> JSON -> Object conversion ${it.values}"
                        )
                        val cipher = it.values.first()
                        val aesIv = Convertor().stringToIv(it.values.last())
                        val sk = AES.generateKey(userEmail(), userEmail())
                        val plainText = AES.decrypt(cipher, sk, aesIv)
                        Log.d(javaClass.canonicalName, "JSON $plainText")
                        Gson().fromJson(plainText, Expense::class.java)
                    }

                    exps.forEach {
                        Log.d(javaClass.canonicalName, "obj list $it")
                        addExpense(it)
                    }

//                    restoreStat.value = restoreStat.value!! + 1
                }
            }
            .addOnFailureListener {
                Log.d(javaClass.canonicalName, it.stackTraceToString())
//                restoreStat.value = restoreStat.value!! - 2
            }
    }

    // UI input checks

    fun isTitleValid(text: String) = text.isNotEmpty() && text != ""

    fun isAmountValid(text: String) =
        text.matches("-?\\d+(\\.\\d+)?".toRegex()) && text.toFloat() > 0

    fun isEmailValid(email: CharSequence?) =
        if (email == null) false else Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isPassValid(password: CharSequence?) =
        !password.isNullOrEmpty() && password.isNotBlank()

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