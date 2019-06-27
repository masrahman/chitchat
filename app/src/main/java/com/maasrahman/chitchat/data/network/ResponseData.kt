package com.maasrahman.chitchat.data.network

import com.google.gson.annotations.SerializedName

data class ResponseData(
    @SerializedName("rc")
    val rc: String,
    @SerializedName("message")
    val message: String
)