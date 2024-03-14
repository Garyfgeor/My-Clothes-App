package com.cscorner.myclothes.data

//Class that keeps users information
class AppUser{
    var username: String? = null
    var userId: String? = null
    var userEmail: String? = null
    var typeOfUser: String? = null
    var fcmToken: String? = null

    constructor(){}

    constructor(username: String?, userId: String?, userEmail: String?, typeOfUser: String?, fcmToken: String?){
        this.username = username
        this.userId = userId
        this.userEmail = userEmail
        this.typeOfUser = typeOfUser
        this.fcmToken = fcmToken
    }


    companion object {
        var instance: AppUser? = null
            get(){
                if(field == null){
                    //Create new instance from app user
                    field = AppUser()
                }
                return field
            }
            private set
    }
}