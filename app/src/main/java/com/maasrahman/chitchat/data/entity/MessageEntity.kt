package com.maasrahman.chitchat.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "messages")
data class MessageEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    @ColumnInfo(name = "userId")
    var userId: String,
    @ColumnInfo(name = "name")
    var nama: String,
    @ColumnInfo(name = "dateTime")
    var dateTime: String,
    @ColumnInfo(name = "message")
    var message: String,
    @ColumnInfo(name = "dataType")
    var dataType: String
) : Parcelable {
    constructor(userId: String, nama: String, dateTime: String, message: String, dataType: String) : this(0, userId, nama, dateTime, message, dataType)
}