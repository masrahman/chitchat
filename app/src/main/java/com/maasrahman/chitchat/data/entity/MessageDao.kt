package com.maasrahman.chitchat.data.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages LIMIT 20 OFFSET (SELECT COUNT(*) FROM messages)-:page")
    fun getMessages(page: Int) : List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(vararg message: MessageEntity)

}