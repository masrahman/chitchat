package com.maasrahman.chitchat.data.network

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class RequestData(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("messages")
    val message: String,
    @SerializedName("dataType")
    val dataType: String,
    @SerializedName("token")
    val token: String
)