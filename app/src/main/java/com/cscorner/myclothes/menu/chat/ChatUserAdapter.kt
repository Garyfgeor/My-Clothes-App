package com.cscorner.myclothes.menu.chat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.AppUser
//This class is an adapter for a RecyclerView, creating a list of users displayed in the chat fragment.
//Also starts an activity to chat with the selected user.
class ChatUserAdapter(val context: Context, private val userList: ArrayList<AppUser>):
    RecyclerView.Adapter<ChatUserAdapter.UserViewHolder>() {

        //It creates and returns a new UserViewHolder instance, which represents an item view for a user in the RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.person_chat, parent, false)
        return UserViewHolder(view)
    }

    //Returns the number of the users in the chat list.
    override fun getItemCount(): Int {
        return userList.size
    }

    //It sets data to the views of a UserViewHolder instance from the user's position in the userList
    //and it defines a click listener to start a chat activity with the selected user.
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

        holder.name.text = currentUser.username

        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatWithPersonActivity::class.java)

            intent.putExtra("username", currentUser.username)
            intent.putExtra("userId", currentUser.userId)

            context.startActivity(intent)
        }
    }

    //This class is a ViewHolder for a user item in the RecyclerView
    class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.name)
    }
}