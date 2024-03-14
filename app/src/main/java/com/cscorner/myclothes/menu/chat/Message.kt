package com.cscorner.myclothes.menu.chat

//Class that keeps the message data
class Message {
    var message: String? = null
    var senderId: String? = null
    var datetime: String? = null

    constructor(){}

    constructor(message: String?, senderId: String?, datetime: String?){
        this.message = message
        this.senderId = senderId
        this.datetime = datetime
    }
}
