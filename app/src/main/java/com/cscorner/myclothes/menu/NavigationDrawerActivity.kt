package com.cscorner.myclothes.menu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.cscorner.myclothes.loginAndSignUp.MainActivity
import com.cscorner.myclothes.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging

//This class creates the Navigation Drawer that contains the users profile and a menu of options and for each option selected is shows
// the appropriate screen (fragment)
class NavigationDrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var userType: String = ""
    private lateinit var mAuth: FirebaseAuth

    //Create the navigation drawer and the users profile in the top of the drawer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        userType = intent.getStringExtra("userType").toString()
        if(userType == "Individual"){
            setContentView(R.layout.activity_navigation_drawer_individual)
        }
        else{//Charity
            setContentView(R.layout.activity_navigation_drawer_charity)
        }
        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //Initialize users personal information
        val header: View = navigationView.getHeaderView(0)
        var userName: TextView = header.findViewById(R.id.userName)
        val userEmail: TextView = header.findViewById(R.id.userEmail)

        userEmail.text = mAuth.currentUser?.email

        val db = Firebase.firestore
        val usersDocRef =  db.collection("Users")

        usersDocRef.whereEqualTo("userId", mAuth.currentUser?.uid)
            .get()
            .addOnSuccessListener {  querySnapshot ->
                //Check if there is any document returned
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val username = document.getString("username")
                    if (username != null) {
                        userName.text = username
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("error username", "Error getting documents: $exception")
            }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open_nav_drawer,
            R.string.close_nav_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            if(userType == "Individual") {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeIndividualsFragment()).commit()
            }
            else{
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeCharitiesFragment()).commit()
            }
            navigationView.setCheckedItem(R.id.nav_home)
        }
    }

    //Navigate the user to the selected fragment (screen)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction().apply {
                    if (userType == "Individual") {
                        replace(R.id.fragment_container, HomeIndividualsFragment())
                    } else {
                        replace(R.id.fragment_container, HomeCharitiesFragment())
                    }
                    commit()
                }
            }
            R.id.nav_my_uploads -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyUploadsFragment()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment()).commit()
            R.id.nav_upload_item -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UploadItemFragment()).commit()
            R.id.nav_chat -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatFragment()).commit()
            R.id.nav_log_out -> logOutUser()

        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //When back button is pressed close the drawer if it is open or exit the app
    override fun onBackPressed() {
        super.onBackPressed()
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            //it will exit the app if the drawer is not opened
            onBackPressedDispatcher.onBackPressed()
        }
    }

    //Log out user and transfer him to the log in page
    private fun logOutUser() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mAuth.signOut()
                val intent = Intent(this@NavigationDrawerActivity, MainActivity::class.java)
                finish()
                startActivity(intent)
            } else {
                Log.d("Logout", "Failed to delete FCM token: ${task.exception}")
            }
        }
    }

}