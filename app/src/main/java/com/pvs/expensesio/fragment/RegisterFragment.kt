package com.pvs.expensesio.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pvs.expensesio.MainActivity
import com.pvs.expensesio.MyViewModel
import com.pvs.expensesio.viewbehavior.ViewBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.pvs.expensesio.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    private val viewModel: MyViewModel by viewModels()

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Email + Password Signup
        binding.signUp.setOnClickListener {

            Log.d("RegisterFragment.registerHere", "register here tapped")

            val firstName = binding.firstNameTIL.editText!!.text.toString()
            val lastname = binding.lastNameTIL.editText!!.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            val isFirstNameValid = viewModel.isTitleValid(firstName)
            val isLastNameValid = viewModel.isTitleValid(lastname)
            val isEmailValid = viewModel.isEmailValid(email)
            val isPassValid = viewModel.isPassValid(password)

            if (!isFirstNameValid) {
                ViewBehavior.tilErrorMsg(
                    binding.firstNameTIL,
                    false,
                    "Please enter first name"
                )
            }
            if (!isLastNameValid) {
                ViewBehavior.tilErrorMsg(
                    binding.lastNameTIL,
                    false,
                    "Please enter last name"
                )
            }
            if (!isEmailValid) {
                ViewBehavior.tilErrorMsg(
                    binding.emailTIL,
                    false,
                    "Please enter a valid email"
                )
            }
            if (!isPassValid) {
                ViewBehavior.tilErrorMsg(
                    binding.passwordTIL,
                    false,
                    "Please enter a valid password"
                )
            }

            if (isFirstNameValid && isLastNameValid && isEmailValid && isPassValid) {
                createUser(email, password, firstName, lastname)
            }

        }

        binding.firstNameTIL.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.firstNameTIL.error = null
        }

        binding.lastNameTIL.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.lastNameTIL.error = null
        }

        binding.emailTIL.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.emailTIL.error = null
        }

        binding.passwordTIL.editText!!.doOnTextChanged { _, _, _, _ ->
            binding.passwordTIL.error = null
        }

        binding.actionText.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root

    }

    private fun createUser(email: String, password: String, firstName: String, lastName: String) {

        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener {

                val hasSignInProvider = it.signInMethods!!.isEmpty()

                if (hasSignInProvider) {

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            when {
                                task.isSuccessful -> {
                                    Log.d("RegisterFragment", "Email+Password Success")
                                    val changeUserNameReq = UserProfileChangeRequest.Builder()
                                        .setDisplayName("$firstName $lastName").build()
                                    firebaseAuth.currentUser!!.updateProfile(changeUserNameReq)
                                    val action =
                                        RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
                                    findNavController().navigate(action)
                                }
                                else -> {
                                    Log.d("RegisterFragment", "Email+Password invalid credentials")
                                    (requireActivity() as MainActivity).buildSnackBar("Please enter valid credentials")
                                }
                            }

                        }
                } else {
                    (requireActivity() as MainActivity).buildSnackBar("Account with this email already exists")
                }

            }
            .addOnFailureListener {
                (requireActivity() as MainActivity).buildSnackBar("An error occurred, please try again later")
            }

    }

}