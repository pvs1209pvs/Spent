package com.pvs.spent.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AlertDialog.BUTTON_POSITIVE
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.pvs.spent.MyViewModel
import com.pvs.spent.data.CreationPeriod
import com.pvs.spent.db.Convertor
import com.pvs.spent.ui.AlertBackup
import com.pvs.spent.ui.ProfileViewPreference
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: MyViewModel by viewModels()
    private val firebaseAuth = FirebaseAuth.getInstance()

    @SuppressLint("RestrictedApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(com.pvs.spent.R.xml.root_preferences, rootKey)

        Log.d(javaClass.canonicalName, "other ${com.pvs.spent.R.drawable.cat_other}")
        Log.d(javaClass.canonicalName, "home ${com.pvs.spent.R.drawable.cat_home}")
        Log.d(javaClass.canonicalName, "car ${com.pvs.spent.R.drawable.cat_car}")
        Log.d(javaClass.canonicalName, "grocery ${com.pvs.spent.R.drawable.cat_food}")
        Log.d(javaClass.canonicalName, "snacks ${com.pvs.spent.R.drawable.cat_pizza}")
        Log.d(javaClass.canonicalName, "clothes ${com.pvs.spent.R.drawable.cat_clothes}")

        // Display name, email and profile pic
        val user = firebaseAuth.currentUser!!

        val userProfilePref = findPreference<ProfileViewPreference>("profileImage")!!
            .setUserEmail(user.email!!)
            .setUserName(user.displayName!!)

        val url = user.photoUrl

        if (url != null) {
            userProfilePref.setImage(Objects.requireNonNull(url).toString())
        } else {
            firebaseAuth.getAccessToken(false).addOnSuccessListener {
                val nameInitials = viewModel.getNameInit(user.displayName!!)
                userProfilePref.setInit(nameInitials)
            }
        }

        // Category
        findPreference<Preference>("categories")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToManageCategoryFragment()
                findNavController().navigate(action)
                true
            }


        // Currency
        val useLocalCurrency = findPreference<SwitchPreferenceCompat>("useLocalCurrency")!!
        val currencySymbol = findPreference<EditTextPreference>("currencySymbol")!!

        // To remember previous state
        currencySymbol.isEnabled = !useLocalCurrency.isChecked

        useLocalCurrency.setOnPreferenceChangeListener { preference, _ ->
            val oldState = (preference as SwitchPreferenceCompat).isChecked
            currencySymbol.isEnabled = oldState
            true
        }

        // Sort History
        val sortHistoryBy = findPreference<ListPreference>("sortHistoryBy")!!
        sortHistoryBy.setOnPreferenceChangeListener { _, newValue ->
            sortHistoryBy.value = newValue.toString()
            true
        }

        // Backup Data
        findPreference<Preference>("backup")!!.setOnPreferenceClickListener {

            viewModel.backupCategory()
            viewModel.backupExpense()

            val backupDialog = AlertBackup(requireActivity())
            backupDialog.show()

            viewModel.expenseCount(viewModel.userEmail(), 0).observe(viewLifecycleOwner){ expenseCount ->
                viewModel.categoryCount(viewModel.userEmail(), 0).observe(viewLifecycleOwner) { categoryCount ->
                    if (expenseCount == 0 && categoryCount == 0) {
                        backupDialog.dismiss()
                    }
                }
            }

            true
        }

        // Restore data
        findPreference<Preference>("restore")!!.setOnPreferenceClickListener {
            viewModel.restoreCategory()
            viewModel.restoreExpense()
            true
        }

        // Account management
        findPreference<Preference>("logout")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {

                // Check sing in method to properly log out
                firebaseAuth.getAccessToken(false)
                    .addOnSuccessListener(object : OnSuccessListener<GetTokenResult> {

                        override fun onSuccess(p0: GetTokenResult?) {
                            Log.d("SettingsFragment", "Logout ${p0!!.signInProvider!!}")
                            when (p0.signInProvider) {
                                "password" -> emailPasswordSignOut()
                                "google.com" -> googleSignOut()
                                "facebook.com" -> facebookSignOut()
                                else -> throw IllegalArgumentException("Invalid signInProvider, error occurred while logging out using firebase")

                            }
                        }

                    })

                true
            }

        findPreference<Preference>("del_user_data")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            AlertDialog.Builder(requireContext()).apply {
                setTitle("Delete User Data")
                setMessage("Are you sure you want to erase all your data permantelly? This action cannot be reversed.")

                setPositiveButton("YES") { _, _ ->
                    viewModel.delCategory()
                    viewModel.delExpense()
                    viewModel.delFirestoreData()
                }

                setNegativeButton("NO", null)

                show()
            }

            true
        }

        // About
        findPreference<Preference>("about")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                AlertDialog.Builder(requireContext()).create().apply {
                    setTitle("About")
                    setMessage("Design and created by Paramvir Singh")
                    setButton(BUTTON_POSITIVE, "Ok") { it, _ -> it.dismiss() }
                    show()
                }
                true
            }
    }


    private fun emailPasswordSignOut() {
        FirebaseAuth.getInstance().signOut()
        navigateToLogin()
    }

    private fun googleSignOut() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("723215061151-qlki6jm95m6kishu2s68cjh5nujeqfv6.apps.googleusercontent.com")
            .requestEmail()
            .build()

        GoogleSignIn.getClient(requireActivity(), gso).signOut().addOnCompleteListener {
            FirebaseAuth.getInstance().signOut()
            navigateToLogin()
        }

    }

    private fun facebookSignOut() {
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        findNavController().navigate(action)
    }

}