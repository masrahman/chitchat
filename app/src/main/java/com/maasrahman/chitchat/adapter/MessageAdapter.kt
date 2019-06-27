package com.maasrahman.chitchat.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maasrahman.chitchat.R
import com.maasrahman.chitchat.data.entity.MessageEntity
import com.maasrahman.chitchat.utils.GlideApp
import com.maasrahman.chitchat.utils.UserData
import com.maasrahman.chitchat.utils.changeDateFormat
import kotlinx.android.synthetic.main.item_imgreceiver.view.*
import kotlinx.android.synthetic.main.item_imgsender.view.*
import kotlinx.android.synthetic.main.item_imgsender.view.imgMessage
import kotlinx.android.synthetic.main.item_textreceiver.view.txtName
import kotlinx.android.synthetic.main.item_textsender.view.txtDateTime
import kotlinx.android.synthetic.main.item_textsender.view.txtMessage
import org.jetbrains.anko.textColor
import kotlin.random.Random

class MessageAdapter(val context: Context, val list: List<MessageEntity>, val listener:(MessageEntity) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TXTSENDER = 0
    private val TXTRECEIVER = 1
    private val IMGSENDER = 2
    private val IMGRECEIVER = 3


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TXTSENDER -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_textsender, parent, false)
                return TextSenderHolder(v)
            }
            TXTRECEIVER -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_textreceiver, parent, false)
                return TextReceiverHolder(v)
            }
            IMGSENDER -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_imgsender, parent, false)
                return ImageSenderHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_imgreceiver, parent, false)
                return ImageReceiverHolder(v)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        when(getItemViewType(position)){
            TXTSENDER -> {
                (holder as TextSenderHolder).onBindData(model)
            }
            TXTRECEIVER -> {
                (holder as TextReceiverHolder).onBindData(model)
            }
            IMGSENDER -> {
                (holder as ImageSenderHolder).onBindData(model, listener)
            }
            else -> {
                (holder as ImageReceiverHolder).onBindData(model, listener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val model = list[position]
        if(model.userId == UserData.loadString(context, context.getString(R.string.emailvalue))){
            if(model.dataType == "text"){
                return TXTSENDER
            }
            return IMGSENDER
        }else{
            if(model.dataType == "text"){
                return TXTRECEIVER
            }
            return IMGRECEIVER
        }
    }

    inner class TextSenderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBindData(model: MessageEntity){
            itemView.txtMessage.text = model.message
            itemView.txtDateTime.text = changeDateFormat(model.dateTime, "dd-MMM-yy HH:mm")
        }
    }

    inner class TextReceiverHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBindData(model: MessageEntity){
            itemView.txtName.text = model.nama
            itemView.txtName.textColor = Color.rgb(Random.nextInt(255 - 0 + 1)+1, Random.nextInt(255 - 0 + 1)+1, Random.nextInt(255 - 0 + 1)+1)
            itemView.txtMessage.text = model.message
            itemView.txtDateTime.text = changeDateFormat(model.dateTime, "dd-MMM-yy HH:mm")
        }
    }

    inner class ImageSenderHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBindData(model: MessageEntity, listener: (MessageEntity) -> Unit){
            GlideApp.with(itemView)
                .load(model.message)
                .into(itemView.imgMessage)
            itemView.txtDateTime.text = changeDateFormat(model.dateTime, "dd-MMM-yy HH:mm")
            itemView.imgMessage.setOnClickListener {
                listener(model)
            }
        }
    }

    inner class ImageReceiverHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBindData(model: MessageEntity, listener: (MessageEntity) -> Unit){
            GlideApp.with(itemView)
                .load(model.message)
                .into(itemView.imgMessage)
            itemView.txtName.text = model.nama
            itemView.txtName.textColor = Color.rgb(Random.nextInt(255 - 0 + 1)+1, Random.nextInt(255 - 0 + 1)+1, Random.nextInt(255 - 0 + 1)+1)
            itemView.txtDateTime.text = changeDateFormat(model.dateTime, "dd-MMM-yy HH:mm")
            itemView.imgMessage.setOnClickListener {
                listener(model)
            }
        }
    }
}