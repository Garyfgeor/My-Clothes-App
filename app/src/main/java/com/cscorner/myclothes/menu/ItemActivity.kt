package com.cscorner.myclothes.menu

import com.cscorner.myclothes.menu.adapters.ImageAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.cscorner.myclothes.R
import com.cscorner.myclothes.menu.chat.ChatWithPersonActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

//This class creates the screen of the item that the user selects, and initializes
// all the data from the firebase. Also contains a button that send message to the owner of the item.
@Suppress("NAME_SHADOWING")
class ItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val itemHeading: TextView = findViewById(R.id.heading)
        val itemPrice: TextView = findViewById(R.id.price)
        val itemDescription: TextView = findViewById(R.id.description)
        val itemSize: TextView = findViewById(R.id.size)
        val itemUserUploadedName: TextView = findViewById(R.id.userUploaded)
        val sendMessageButton: ImageView = findViewById(R.id.inbox)
        val bundle: Bundle? = intent.extras
        val heading = bundle?.getString("heading")
        val clothId = bundle?.getString("clothId")
        val category =  bundle?.getString("category")

        //**************FIRESTORE**********************
        //Initialize Cloud Firestore
        val db = Firebase.firestore
        val usersDocRef =  db.collection("Users")
        val senderUserId = FirebaseAuth.getInstance().currentUser?.uid
        var senderUsername: String? = null

        //Find username of the user
        usersDocRef.whereEqualTo("userId", senderUserId)
            .get()
            .addOnSuccessListener {  querySnapshot ->
                //Check if there is any document returned
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val username = document.getString("username")
                    if (username != null) {
                        senderUsername = username
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("error item username", "Error getting documents: $exception")
            }

        //Get the information for the item from Firebase
        if(category != null) {
            var finalHeading: String? = heading + "\n"
            val docRef = clothId?.let { db.collection(category).document(it) }
            val usersDocRef =  db.collection("Users")
            var userUploadedId: String? = null
            var imageUrl1 = ""
            var imageUrl2 = ""

            docRef?.get()?.addOnSuccessListener { document ->
                if (document != null) {
                    finalHeading = "${document.data?.get("heading")}"
                    imageUrl1 = "${document.data?.get("imageUrl1")}"
                    imageUrl2 = "${document.data?.get("imageUrl2")}"
                    itemPrice.text = "${document.data?.get("price")}€"
                    itemSize.text = "${document.data?.get("size")}"
                    itemDescription.text = "${document.data?.get("description")}"
                    userUploadedId = document.data?.get("userId").toString()

                    usersDocRef.whereEqualTo("userId", userUploadedId)
                        .get()
                        .addOnSuccessListener {  querySnapshot ->
                            //Check if there is any document returned
                            if (!querySnapshot.isEmpty) {
                                val document = querySnapshot.documents[0]
                                val username = document.getString("username")
                                if (username != null) {
                                    itemUserUploadedName.text = username
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("error item username", "Error getting documents: $exception")
                        }
                }
                itemHeading.text = finalHeading


                //Image adapter for the two images of the item
                val imageUrls = listOf(imageUrl1, imageUrl2)
                val adapter = ImageAdapter(imageUrls)

                //Viewpager to view the two images
                val viewPager: ViewPager2 = findViewById(R.id.viewPager)
                viewPager.adapter = adapter

                //Connect viewpager with the dots indicator tab for the images of the item
                val tabLayout = findViewById<TabLayout>(R.id.dots_tab)
                TabLayoutMediator(tabLayout, viewPager) { _, _ ->
                }.attach()

                //Button that send message to the owner of the item, specifying that
                //this is the item the user wants
                sendMessageButton.setOnClickListener{
                    val intent = Intent(this, ChatWithPersonActivity::class.java)
                    val inboxButtonClicked = "true"

                    intent.putExtra("username", itemUserUploadedName.text)
                    intent.putExtra("userId", userUploadedId)
                    intent.putExtra("inboxButtonClicked", inboxButtonClicked)
                    intent.putExtra("itemImage", imageUrl1)
                    intent.putExtra("senderUsername", senderUsername)
                    finish()
                    startActivity(intent)
                }

            }
        }
    }
}