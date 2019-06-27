package com.maasrahman.chitchat.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("email")
    val email: String,
    @SerializedName("nama")
    val nama: String,
    @ServerTimestamp
    val dateTime: Timestamp
){
    constructor(email: String, nama: String) : this(email, nama, Timestamp.now())
}