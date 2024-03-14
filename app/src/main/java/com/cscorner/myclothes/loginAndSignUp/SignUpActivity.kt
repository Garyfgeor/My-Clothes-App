package com.cscorner.myclothes.loginAndSignUp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.AppUser
import com.cscorner.myclothes.databinding.ActivitySignUpBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

//This is the Sign-up Activity (Screen) of the app
// in which the user gives some personal information to create an account
// and sends an email verification link to verify users email.
//Also has a button to move on the Log-in Screen
class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        auth = Firebase.auth

        //When the "Sign Up" button is clicked hide the keyboard and the cursor and call function createUser()
        binding.signUpBtn.setOnClickListener(){
            val view: View? = this.currentFocus
            if (view != null) {
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                clearFocusFromFields()
            }
            createUser()
        }

        val accountType = resources.getStringArray(R.array.account)
        val arrayAdapterAccountType = ArrayAdapter(this, R.layout.dropdown_item, accountType)
        binding.accountType.setAdapter(arrayAdapterAccountType)

        val goBackBtn: Button = findViewById(R.id.go_back_btn)

        goBackBtn.setOnClickListener() {
            //Go to Main Activity (Log-in)
            var i = Intent(
                this,
                MainActivity::class.java
            )
            finish()
            startActivity(i)
        }

    }

    //Function that checks if all the fields are completed correctly (if not shows the corresponding error),
    // sends a verification link in user's email and
    // if the user verify his email it creates an account with the given credentials
    @SuppressLint("ClickableViewAccessibility")
    private fun createUser() {
        val email = binding.emailTextSignUp.text.toString()
        val password = binding.passwordTextSignUp.text.toString()
        val confirmPassword = binding.confirmPasswordTextSignUp.text.toString()
        val fullName = binding.nameText.text.toString()
        val type = binding.accountType.text.toString()

        //check if the fields are empty or wrong
        if(fullName.isEmpty()){
            val name: TextInputLayout = findViewById(R.id.name)
            name.isErrorEnabled = true
            name.error = "Full Name/Organization Name cannot be empty"

            name.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    name.isErrorEnabled = false
                }
                false
            }
        }
        else if(type.isEmpty()){
            val typ: TextInputLayout = findViewById(R.id.type)
            typ.isErrorEnabled = true
            typ.error = "Account type cannot be empty"

            typ.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    typ.isErrorEnabled = false
                }
                false
            }
        }
        else if(email.isEmpty()){
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
        else if(confirmPassword.isEmpty()){
            val con: TextInputLayout = findViewById(R.id.confirmPassword)
            con.isErrorEnabled = true
            con.error = "Confirm Password cannot be empty"

            con.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    con.isErrorEnabled = false
                }
                false
            }
        }
        else {
            if(password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            //Send email verification link
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnSuccessListener {
                                    Toast.makeText(this, "Please check your emails to verify your email!", Toast.LENGTH_LONG).show()
                                }
                                ?.addOnFailureListener {
                                    Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                                }

                            val user = AppUser(
                                username = fullName,
                                userId = auth.currentUser?.uid!!,
                                userEmail = email,
                                typeOfUser = type,
                                fcmToken = null
                            )
                            var collectionReference: CollectionReference = db.collection("Users")
                            collectionReference.add(user)

                            //Go to Login Screen
                            var intent = Intent(this, MainActivity::class.java)
                            finish()
                            startActivity(intent)

                        } else {
                            //if sign up fails display a message to the user.
                            val exception = task.exception
                            if (exception is FirebaseAuthException) {
                                // Handle FirebaseAuthException
                                val em: TextInputLayout = findViewById(R.id.email)
                                val pas: TextInputLayout = findViewById(R.id.password)
                                when (exception.errorCode) {
                                    "ERROR_WEAK_PASSWORD" -> {
                                        pas.isErrorEnabled = true
                                        pas.error = "The password should be at least 6 characters."
                                        pas.editText?.setOnFocusChangeListener { _, clicked ->
                                            if (clicked) {
                                                pas.isErrorEnabled = false
                                            }
                                        }
                                    }

                                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                        em.isErrorEnabled = true
                                        em.error = "The email address is already in use."
                                        em.editText?.setOnFocusChangeListener { _, clicked ->
                                            if (clicked) {
                                                em.isErrorEnabled = false
                                            }
                                        }
                                    }

                                    "ERROR_INVALID_EMAIL" -> {
                                        em.error = "The email address is not valid."
                                        em.editText?.setOnFocusChangeListener { _, clicked ->
                                            if (clicked) {
                                                em.isErrorEnabled = false
                                            }
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
            }
            else{
                val conf: TextInputLayout = findViewById(R.id.confirmPassword)
                conf.isErrorEnabled = true
                conf.error = "Passwords do not match."
                conf.editText?.setOnTouchListener {_, event->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        conf.isErrorEnabled = false
                    }
                    false
                }
            }
        }

    }

    //Function that hides the cursor in the text input fields
    private fun clearFocusFromFields() {
        val email: TextInputLayout = findViewById(R.id.email)
        val password: TextInputLayout = findViewById(R.id.password)
        val confPassword: TextInputLayout = findViewById(R.id.confirmPassword)
        val name: TextInputLayout = findViewById(R.id.name)
        val type: TextInputLayout = findViewById(R.id.type)

        email.clearFocus()
        password.clearFocus()
        confPassword.clearFocus()
        name.clearFocus()
        type.clearFocus()
    }
}