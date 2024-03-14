package com.cscorner.myclothes.data

import com.google.firebase.Timestamp

//Data class for the cloth item that keeps information to be uploaded in Firebase
data class Clothes(
    var donateTo: String = "",
    var imageUrl1: String = "",
    var imageUrl2: String = "",
    var heading: String = "",
    var description: String = "",
    var price: String = "",
    var size: String = "",
    var category: String = "",
    var userId: String = "",
    var clothId: String = "",
    var timeAdded: Timestamp = Timestamp.now()
)
