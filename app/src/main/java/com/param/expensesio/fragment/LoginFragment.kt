package com.param.expensesio.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import com.param.expensesio.MainActivity
import com.param.expensesio.MyViewModel
import com.param.expensesio.databinding.FragmentLoginBinding
import com.param.expensesio.viewbehavior.ViewBehavior
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.param.expensesio.R
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val viewModel: MyViewModel by viewModels()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val callbackManager = CallbackManager.Factory.create()


    companion object {
        private const val RC_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
    }


    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Email + Password sign-up
        binding.actionText.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }

        // Email + Password sign-in
        binding.login.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (viewModel.isEmailValid(email) && viewModel.isPassValid(password)) {
                signInUser(email, password)
            } else {
                Log.d("LoginFragment", "Email+Password Invalid email or password")
                ViewBehavior.tilErrorMsg(
                    binding.emailTIL,
                    viewModel.isEmailValid(email),
                    "Please enter a valid email"
                )
                ViewBehavior.tilErrorMsg(
                    binding.passwordTIL,
                    viewModel.isPassValid(password),
                    "Please enter a valid password"
                )
            }

        }

        // Google
        binding.googleLoginButton.setOnClickListener {
            googleSignIn()
        }

        // Facebook
        binding.facebookLoginButton.setOnClickListener {
            facebookSignIn()
        }

        // To avoid displaying error when the user is typing the email
        binding.emailTIL.editText!!.doOnTextChanged { _, _, _, _ ->
            emailTIL.error = null
        }

        // To avoid displaying error when the user is typing the password
        binding.passwordTIL.editText!!.doOnTextChanged { _, _, _, _ ->
            passwordTIL.error = null
        }

        return binding.root

    }

    private fun facebookSignIn() {

        Log.d("LoginFragment", "Facebook button clicked")

        LoginManager.getInstance()
            .logInWithReadPermissions(
                requireActivity(),
                callbackManager,
                listOf("email", "public_profile")
            )

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult) {
                Log.d("LoginFragment", "Facebook onSuccess()")
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d("LoginFragment", "Facebook onCancel()")
            }

            override fun onError(error: FacebookException) {
                Log.d("LoginFragment", "Facebook onError() $error")
            }

        })


    }

    private fun handleFacebookAccessToken(token: AccessToken) {

        firebaseAuth.signInWithCredential(FacebookAuthProvider.getCredential(token.token))
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginFragment", "Facebook login success")
                    if (firebaseAuth.currentUser != null) {
                        val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                        findNavController().navigate(action)
                    }
                } else {
                    (requireActivity() as MainActivity).buildSnackBar("An account already exists with the same email address")
                    FirebaseAuth.getInstance().signOut()
                    LoginManager.getInstance().logOut()
                    Log.w("LoginFragment", "Facebook login failure, ${task.exception}")
                }
            }
    }

    private val googleActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data

                val gEmail =
                    data?.getParcelableExtra<GoogleSignInAccount>("googleSignInAccount")?.email

                if (gEmail != null) {
                    FirebaseAuth.getInstance().fetchSignInMethodsForEmail(gEmail)
                        .addOnSuccessListener { signInMethod ->

                            val isGoogle =
                                signInMethod.signInMethods!!.run {
                                    contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD) || isEmpty()
                                }

                            if (isGoogle) {

                                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                                if (task.isSuccessful) {
                                    try {
                                        firebaseAuthWithGoogle(task.getResult(ApiException::class.java)!!.idToken!!)
                                    } catch (e: ApiException) {
                                        e.printStackTrace()
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
            callbackManager.onActivityResult(200, it.resultCode, it.data)

        }


    private fun googleSignIn() {
        Log.d("LoginFragment", "Google button clicked")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        googleSignInClient.signOut()
        googleActivityResultLauncher.launch(googleSignInClient.signInIntent)
    }


    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginFragment", "Google login success")
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    findNavController().navigate(action)

                } else {
                    Log.d("LoginFragment", "Google login failure, invalid credentials")

                }
            }

    }


    @SuppressLint("RestrictedApi")
    private fun signInUser(email: String, password: String) {

        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener {

                if (it.signInMethods!!.isEmpty()) {
                    (requireActivity() as MainActivity).buildSnackBar("No account found under this email")
                }
                else {

                    val hasEmailPasswordProvider =
                        it.signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)

                    println(hasEmailPasswordProvider)

                    if (hasEmailPasswordProvider) {
                        firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("LoginFragment", "Email+password login success")
                                    val action =
                                        LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                                    findNavController().navigate(action)
                                } else {
                                    Log.d(
                                        "LoginFragment",
                                        "Email+password failure, invalid credentials"
                                    )
                                    (requireActivity() as MainActivity).buildSnackBar("Please enter valid credentials")
                                }
                            }
                    }
                    else{
                        (requireActivity() as MainActivity).buildSnackBar("Account with this email already exists")
                    }
                }

            }
            .addOnFailureListener {
                (requireActivity() as MainActivity).buildSnackBar("An error occurred, please try again later")
            }

    }


    @SuppressLint("RestrictedApi")
    fun BS() = findNavController().backQueue
        .map { it.destination }
        .filterNot { it is NavGraph }
        .joinToString(" > ") { it.displayName.split('/')[1] }


}
///https://stackoverflow.com/questions/71717099/ideal-solution-for-firebase-google-provider-login-overriding-other-sign-in-provi/71730017#71730017