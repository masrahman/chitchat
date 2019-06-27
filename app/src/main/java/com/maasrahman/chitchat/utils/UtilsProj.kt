package com.maasrahman.chitchat.utils

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import com.maasrahman.chitchat.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat

object UtilsProj {

}

fun showAlert(context: Context, errorString: String){
    context.alert(errorString) {
        positiveButton(context.getString(R.string.ok)) {

        }
    }.show().apply {
        getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = Color.WHITE }
    }
}

fun changeDateFormat(strAwal: String, frmt: String) : String{
    var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val dt = sdf.parse(strAwal)
    sdf = SimpleDateFormat(frmt)
    return sdf.format(dt)
}