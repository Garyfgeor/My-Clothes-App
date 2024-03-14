package com.cscorner.myclothes.menu.chatNotifications

import com.google.firebase.auth.FirebaseAuth

object FirebaseUtil {

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        if(currentUserId() != null){
            return true
        }
        return false
    }
}
