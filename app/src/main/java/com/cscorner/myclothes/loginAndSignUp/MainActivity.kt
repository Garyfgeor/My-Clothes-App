package com.cscorner.myclothes.loginAndSignUp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.databinding.DataBindingUtil
import com.cscorner.myclothes.databinding.ActivityMainBinding
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isEmpty
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.AppUser
import com.cscorner.myclothes.menu.NavigationDrawerActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

//This is the Log-in Activity (Screen) of the app
// which check if the email of the user is verified and also
// has a button to move on the Sign-up Screen
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var db = FirebaseFirestore.getInstance()

    //Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //When the "Log In" button is clicked hide the keyboard and the cursor and call function loginWithEmailPassword()
        binding.logInBtn.setOnClickListener{
            val view: View? = this.currentFocus
            if (view != null) {
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                clearFocusFromFields()
            }
            loginWithEmailPassword(
                binding.emailText.text.toString(),
                binding.passwordText.text.toString()
            )
        }

        //Auth Reference
        auth = Firebase.auth

        val createAccountBtn: Button = findViewById(R.id.create_account_btn)

        createAccountBtn.setOnClickListener{
            //Go to Sign Up Activity
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
        }

    }

    //Function that checks if all the fields are completed correctly (if not shows the corresponding error)
    // and continues with the authentication of the user with the Firebase
    @SuppressLint("ClickableViewAccessibility")
    private fun loginWithEmailPassword(email: String, password: String) {
        if(email.isEmpty()){
            val em: TextInputLayout = findViewById(R.id.email)
            em.isErrorEnabled = true
            em.error = "Email cannot be empty"

            em.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    em.isErrorEnabled = false
                }
                false
            }
        }
        else if(password.isEmpty()){
            val pas: TextInputLayout = findViewById(R.id.password)
            pas.isErrorEnabled = true
            pas.error = "Password cannot be empty"

            pas.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    pas.isErrorEnabled = false
                }
                false
            }
        }
        else{
            //Authenticate the user with firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //Check if the email of the user is verified
                        val verification = auth.currentUser?.isEmailVerified
                        if(verification == true){
                            var user: AppUser
                            val emailSearch = auth.currentUser?.email
                            db.collection("Users")
                                .whereEqualTo("userEmail", emailSearch)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    for (document in querySnapshot.documents) {
                                        //Initialize User instance with data from Firestore document
                                        user = document.toObject(AppUser::class.java)!!

                                        val userType = user.typeOfUser
                                        if (userType != null) {
                                            goToNavigationDrawer(userType)
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this,"An error occurred!", Toast.LENGTH_LONG).show()
                                }
                        }
                        else{
                            Toast.makeText(
                                this,
                                "Please check your emails to verify your email!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        //if log-in fails display a message to the user.
                        Toast.makeText(
                            this,
                            "ERROR! Check your credentials or your internet connection.",
                            Toast.LENGTH_LONG
                        ).show()

                        val exception = task.exception

                        if (exception is FirebaseAuthException) {
                            //Handle FirebaseAuthException
                            val em: TextInputLayout = findViewById(R.id.email)
                            em.isErrorEnabled = true
                            val pas: TextInputLayout = findViewById(R.id.password)
                            pas.isErrorEnabled = true

                            when (exception.errorCode) {
                                "ERROR_INVALID_EMAIL" -> {
                                    em.error = "The email address is not valid."
                                    em.editText?.setOnFocusChangeListener { _, clicked ->
                                        if (clicked) {
                                            em.isErrorEnabled = false
                                        }
                                    }
                                }

                                "ERROR_USER_NOT_FOUND" -> {
                                    em.error = "User not found."
                                    em.editText?.setOnFocusChangeListener { _, clicked ->
                                        if (clicked) {
                                            em.isErrorEnabled = false
                                        }
                                    }
                                }

                                else -> {
                                    em.error = "User not found."
                                    em.editText?.setOnFocusChangeListener { _, clicked ->
                                        if (clicked) {
                                            em.isErrorEnabled = false
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            if (exception != null) {
                                val pas: TextInputLayout = findViewById(R.id.password)
                                pas.isErrorEnabled = true
                                val passwordEditText: TextInputEditText = findViewById(R.id.passwordText)
                                if(!pas.isEmpty() && passwordEditText.text?.toString()?.trim()?.length!! < 6){
                                    pas.error = "The password should be at least 6 characters."
                                    pas.editText?.setOnFocusChangeListener { _, clicked ->
                                        if (clicked) {
                                            pas.isErrorEnabled = false
                                        }
                                    }
                                }
                                else if(exception.message?.contains("INVALID_LOGIN_CREDENTIALS") == true){
                                    val em: TextInputLayout = findViewById(R.id.email)
                                    em.isErrorEnabled = true
                                    em.error = "Invalid login credentials."
                                    em.editText?.setOnFocusChangeListener { _, clicked ->
                                        if (clicked) {
                                            em.isErrorEnabled = false
                                        }
                                    }
                                    pas.error = "Invalid login credentials."
                                    pas.editText?.setOnFocusChangeListener { _, clicked ->
                                        if (clicked) {
                                            pas.isErrorEnabled = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    //Function that navigates the user to the Home Page of the app
    private fun goToNavigationDrawer(userType: String) {
        val intent = Intent(this, NavigationDrawerActivity::class.java)
        intent.putExtra("userType", userType)
        finish()
        startActivity(intent)
    }

    //Function that hides the cursor in the text input fields
    private fun clearFocusFromFields() {
        val email: TextInputLayout = findViewById(R.id.email)
        val password: TextInputLayout = findViewById(R.id.password)

        email.clearFocus()
        password.clearFocus()
    }
}

