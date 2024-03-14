package com.cscorner.myclothes.menu.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.cscorner.myclothes.R
import com.google.firebase.auth.FirebaseAuth

//Adapter for RecyclerView used to display messages in a chat. It dynamically inflates different layouts based on the message type (text or image)
// and whether the message is sent or received.
class MessageAdapter(val context: android.content.Context, private val messageList: ArrayList<Message>): RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(viewType == 1){//sent photo
            val view = LayoutInflater.from(context).inflate(R.layout.sent_image, parent, false)
            return SentImageViewHolder(view)
        }
        else if(viewType == 2){//sent message
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent_message, parent, false)
            return SentViewHolder(view)
        }
        else if(viewType == 3){//receive text message
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive_message, parent, false)
            return ReceiveViewHolder(view)
        }
        else {//receive image message
            val view = LayoutInflater.from(context).inflate(R.layout.receive_image, parent, false)
            return ReceivedImageViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        when (holder) {
            is SentViewHolder -> {
                // Sent message view holder
                holder.sentMessage.text = currentMessage.message
                holder.datetimeSent.text = currentMessage.datetime
            }
            is SentImageViewHolder -> {
                // Sent image message view holder
                if (!currentMessage.message.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(currentMessage.message)
                        .into(holder.imageSent)
                }
                holder.datetimeSent.text = currentMessage.datetime
            }
            is ReceiveViewHolder -> {
                // Received message view holder
                holder.receiveMessage.text = currentMessage.message
                holder.datetimeReceive.text = currentMessage.datetime
            }
            is ReceivedImageViewHolder -> {
                // Received image message view holder
                if (!currentMessage.message.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(currentMessage.message)
                        .into(holder.imageReceived)
                }
                holder.datetimeReceive.text = currentMessage.datetime
            }
        }
    }

    //Get the type of the message (image or text and sent or received)
    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId) && isUrl(currentMessage.message!!)){
            return 1
        }
        else if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId) && !isUrl(currentMessage.message!!)){
            return 2
        }
        else if(!FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId) && !isUrl(currentMessage.message!!)){
            return 3
        }
        else{
            return 4
        }
    }

    //Sent text message view
    class SentViewHolder(itemView: View) : ViewHolder(itemView){
        val sentMessage: TextView = itemView.findViewById(R.id.txt_sent_message)
        val datetimeSent: TextView = itemView.findViewById(R.id.datetime)
    }

    //Received text message view
    class ReceiveViewHolder(itemView: View) : ViewHolder(itemView){
        val receiveMessage: TextView = itemView.findViewById(R.id.txt_receive_message)
        val datetimeReceive: TextView = itemView.findViewById(R.id.datetime)
    }

    //Sent image message view
    class SentImageViewHolder(imageView: View) : ViewHolder(imageView) {
        val imageSent: ImageView = imageView.findViewById(R.id.sent_image_view)
        val datetimeSent: TextView = imageView.findViewById(R.id.datetime)
    }

    //Received image message view
    class ReceivedImageViewHolder(imageView: View) : ViewHolder(imageView) {
        val imageReceived: ImageView = imageView.findViewById(R.id.receive_image_view)
        val datetimeReceive: TextView = imageView.findViewById(R.id.datetime)
    }

    //Check if the message is url (so it probably is an image)
    private fun isUrl(string: String): Boolean {
        val urlRegex = ("^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[a-zA-Z0-9-._?,'\\/+&%$#=]+)?$").toRegex()
        return urlRegex.matches(string)
    }
}