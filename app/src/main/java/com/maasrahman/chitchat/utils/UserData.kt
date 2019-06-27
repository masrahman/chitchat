package com.maasrahman.chitchat.utils

import android.app.Activity
import android.content.Context

object UserData {
    val PREF_NAME = "APP_PREF"
    val MODE = Activity.MODE_PRIVATE


    fun saveString(context: Context, key:String, data: String){
        val pref = context.getSharedPreferences(PREF_NAME, MODE)
        val editor = pref.edit()
        editor.putString(key, data)
        editor.commit()
    }

    fun loadString(context: Context, key:String) : String {
        val pref = context.getSharedPreferences(PREF_NAME, MODE)
        return pref.getString(key, "")
    }

    fun clearString(context: Context, key:String) {
        val pref = context.getSharedPreferences(PREF_NAME, MODE)
        val editor = pref.edit()
        editor.remove(key)
        editor.apply()
        editor.commit()
    }

    fun saveBoolean(context: Context, key: String, data: Boolean){
        val pref = context.getSharedPreferences(PREF_NAME, MODE)
        val editor = pref.edit()
        editor.putBoolean(key, data)
        editor.commit()
    }

    fun loadBoolean(context: Context, key:String) : Boolean {
        val pref = context.getSharedPreferences(PREF_NAME, MODE)
        return pref.getBoolean(key, false)
    }
}