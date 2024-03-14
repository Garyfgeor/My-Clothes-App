package com.cscorner.myclothes.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cscorner.myclothes.R
import com.cscorner.myclothes.menu.chat.ChatUserAdapter
import com.cscorner.myclothes.data.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//This class represents the list with a user's chats when the "Chat" option from
//the menu is clicked
class ChatFragment : Fragment() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<AppUser>
    private lateinit var adapter: ChatUserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var noChatsText: TextView

    //Creates the RecyclerView List of persons a user has chatted with
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        noChatsText = view.findViewById(R.id.noChatsText)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        userList = ArrayList()
        adapter = ChatUserAdapter(requireContext(), userList)

        userRecyclerView = view.findViewById(R.id.userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter

        return view
    }

    //Initializes the list of users that have communicated from Firebase
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        mDbRef.child("usersCommunicate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (userSnapshot in snapshot.children) {
                    val userId1 = userSnapshot.child("userId1").getValue(String::class.java)
                    val userId2 = userSnapshot.child("userId2").getValue(String::class.java)
                    val username1 = userSnapshot.child("username1").getValue(String::class.java)
                    val username2 = userSnapshot.child("username2").getValue(String::class.java)

                    if (userId1 == currentUserId || userId2 == currentUserId) {
                        if (userId1 != currentUserId) {
                            userList.add(
                                AppUser(
                                    username = username1,
                                    userId = userId1,
                                    userEmail = null,
                                    typeOfUser = null,
                                    fcmToken = null
                                )
                            )
                        } else if (userId2 != currentUserId) {
                            userList.add(
                                AppUser(
                                    username = username2,
                                    userId = userId2,
                                    userEmail = null,
                                    typeOfUser = null,
                                    fcmToken = null
                                )
                            )
                        }
                    }
                }

                adapter.notifyDataSetChanged()

                if(userList.isEmpty()){
                    noChatsText.visibility = View.VISIBLE
                }
                else{
                    noChatsText.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Failed to retrieve data", error.toException())
            }
        })
    }
}
