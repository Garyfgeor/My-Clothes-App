package com.cscorner.myclothes.menu.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cscorner.myclothes.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

//Chat with a person: it sends automatically text message and image to the owner
// of the item that the user is interested in.
class ChatWithPersonActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var usersDocRef: CollectionReference

    private var receiverRoom: String? = null
    private var senderRoom: String? = null
    private var senderUserId: String? = null
    private var receiverUserId: String? = null
    private var formattedDate: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatwithperson)

        val receiverUsername = intent.getStringExtra("username")
        receiverUserId = intent.getStringExtra("userId")
        var inboxButtonClicked = intent.getStringExtra("inboxButtonClicked")
        val autoImageUrl = intent.getStringExtra("itemImage")
        var senderUsername = intent.getStringExtra("senderUsername")
        senderUserId = FirebaseAuth.getInstance().currentUser?.uid
        val calendar = Calendar.getInstance()
        val datetime = calendar.time
        val dateFormat = SimpleDateFormat("E MMM dd HH:mm", Locale.ENGLISH)
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Athens")
        formattedDate = dateFormat.format(datetime)
        val db = Firebase.firestore
        usersDocRef =  db.collection("Users")



        //createNotificationChannel()


        //Find username of the user
        if(senderUsername == null){
            usersDocRef.whereEqualTo("userId", senderUserId)
                .get()
                .addOnSuccessListener {  querySnapshot ->
                    // Check if there is any document returned
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val username = document.getString("username")
                        if (username != null) {
                            senderUsername = username
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    //Handle any errors that occurred during the query
                    Log.d("error item username", "Error getting documents: $exception")
                }
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        try {
            //Get a reference to the Firebase database
            mDbRef = FirebaseDatabase.getInstance().reference
        } catch (e: Exception) {
            Log.e("FirebaseError", "Error initializing Firebase: ${e.message}")
        }

        //create a unique room for this sender and receiver
        senderRoom = receiverUserId + senderUserId
        receiverRoom = senderUserId + receiverUserId

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverUsername

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        if (inboxButtonClicked == "true") {
            //Send the first message automatically
            val autoMessage = "Hello! I'm interested in this item. Is it still available?"
            var autoMessageObject = Message(autoMessage, senderUserId, formattedDate)

            //add users pair communication in firebase
            mDbRef.child("usersCommunicate")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var pairExists = false

                        //Iterate through the children of the "usersCommunicate" node
                        for (pairSnapshot in snapshot.children) {
                            val userId1 = pairSnapshot.child("userId1").getValue(String::class.java)
                            val userId2 = pairSnapshot.child("userId2").getValue(String::class.java)

                            //Check if the pair already exists
                            if ((userId1 == senderUserId && userId2 == receiverUserId) ||
                                (userId1 == receiverUserId && userId2 == senderUserId)) {
                                pairExists = true
                                break
                            }
                        }

                        //If the pair doesn't exist, add it
                        if (!pairExists) {
                            val newPairKey = mDbRef.child("usersCommunicate").push().key
                            val usersPair = hashMapOf(
                                "userId1" to senderUserId,
                                "username1" to senderUsername,
                                "userId2" to receiverUserId,
                                "username2" to receiverUsername
                            )
                            newPairKey?.let { key ->
                                mDbRef.child("usersCommunicate").child(key).setValue(usersPair)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("FIRE ERROR", "Firebase Database error: ${error.message}")
                    }
                })


            //Send message to the sender's room
            uploadMessageToFirebase(senderRoom!!, autoMessageObject, mDbRef)

            //Send message to the receiver's room
            uploadMessageToFirebase(receiverRoom!!, autoMessageObject, mDbRef)
            inboxButtonClicked = "false"

            //Send the image of the item
            autoMessageObject = Message(autoImageUrl, senderUserId, formattedDate)
            //Send message to the sender's room
            uploadMessageToFirebase(senderRoom!!, autoMessageObject, mDbRef)

            //Send message to the receiver's room
            uploadMessageToFirebase(receiverRoom!!, autoMessageObject, mDbRef)
            inboxButtonClicked = "false"
        }

        //get data from firebase and set them to chat recyclerView
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    // Scroll the RecyclerView to the position of the new message
                    val newPosition = messageAdapter.itemCount - 1
                    chatRecyclerView.smoothScrollToPosition(newPosition)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("FIRE ERROR", "Firebase Database error: ${error.message}")
                    //Show a Toast message with the error
                    Toast.makeText(this@ChatWithPersonActivity, "Firebase Database error: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }

            })

        //send the message from the message box
        sendButton.setOnClickListener {
            Log.d("FIRE ERROR", "send button pressed")
            val message = messageBox.text.toString().trim()
            if (message.isNotEmpty()) {
                val messageObject = Message(message, senderUserId, formattedDate)

                //Send message to the sender's room
                uploadMessageToFirebase(senderRoom!!, messageObject, mDbRef)

                //Send message to the receiver's room
                uploadMessageToFirebase(receiverRoom!!, messageObject, mDbRef)

                //Clear the message box
                messageBox.setText("")
                sendNotification(message)
            }
        }
    }

    //Function that sends notification to the user that haw received a message
    private fun sendNotification(message: String) {
        var userName: String
        //Get current user
        usersDocRef.whereEqualTo("userId", senderUserId)
            .get()
            .addOnSuccessListener {  querySnapshot ->
                //Check if there is any document returned
                if (!querySnapshot.isEmpty) {
                    //Create a notification json with the appropriate data
                    val document = querySnapshot.documents[0]
                    val username = document.getString("username")
                    if (username != null) {
                        userName = username

                        val jsonObject = JSONObject()
                        val notificationObject = JSONObject()
                        val dataObject = JSONObject()

                        notificationObject.put("title", userName)
                        notificationObject.put("body", message)

                        dataObject.put("userId", senderUserId)

                        jsonObject.put("notification", notificationObject)
                        jsonObject.put("data", dataObject)
                        getFCMToken(receiverUserId!!) { token ->
                            jsonObject.put("to", token)

                            callApi(jsonObject)
                        }

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("error item username", "Error getting documents: $exception")
            }

    }

    //Function that sends a POST request to the Firebase Cloud Messaging (FCM) API.
    // It sends a JSON payload to the FCM server containing data for a push notification.
    private fun callApi(jsonObject: JSONObject) {
        val json: MediaType = "application/json".toMediaType()

        val client = OkHttpClient()

        val url = "https://fcm.googleapis.com/fcm/send" //FCM API url
        val body = jsonObject.toString().toRequestBody(json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer bearer_key")
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Notification failed", e.toString())
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                Log.d("Notification response", responseBody!!)
            }
        })
    }

    //Function that sends message to the sender's room
    private fun uploadMessageToFirebase(room: String, message: Message, dbRef: DatabaseReference) {
        dbRef.child("chats").child(room).child("messages")
            .push().setValue(message)
            .addOnSuccessListener {
                Log.d("FIRE ERROR", "Message sent to room")
            }
            .addOnFailureListener { e ->
                Log.d("FIRE ERROR", "Error sending message to room: ${e.message}")
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    //Function that gets the FCM token for a given user ID and updates it in a Firestore database.
    private fun getFCMToken(userId: String, callback: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result

                usersDocRef.whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            document.reference.update("fcmToken", token)
                            callback(token.toString())
                        }
                    }
            }
        }
    }


//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelId = "my_clothes_app_notifications"
//            val channelName = "Your Channel Name"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(channelId, channelName, importance).apply {
//                description = "Your Channel Description"
//            }
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
}
