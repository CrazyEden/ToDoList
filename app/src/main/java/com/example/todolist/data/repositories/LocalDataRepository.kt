package com.example.todolist.data.repositories

import android.content.SharedPreferences
import android.graphics.Color
import com.example.todolist.data.model.Data
import com.google.gson.Gson
import javax.inject.Inject

class LocalDataRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun getBackgroundColor() =
        sharedPreferences.getInt(sharedPreferencesBackgroundColorKey,0)
    fun setBackgroundColor(color:Int) =
        sharedPreferences.edit().putInt(sharedPreferencesBackgroundColorKey,color).apply()

    fun getToolbarColor() =
        sharedPreferences.getInt(sharedPreferencesToolbarColorKey,0)
    fun setToolbarColor(color:Int) =
        sharedPreferences.edit().putInt(sharedPreferencesToolbarColorKey,color).apply()

    fun getWindowColor() =
        sharedPreferences.getInt(sharedPreferencesWindowColorKey, Color.BLUE)
    fun setWindowColor(color:Int) =
        sharedPreferences.edit().putInt(sharedPreferencesWindowColorKey,color).apply()

    fun getLocalToDoList(): Data? {
        val json = sharedPreferences.getString("history",null)
        return Gson().fromJson(json, Data::class.java)
    }
    fun setLocalToDoList(json:String) = sharedPreferences.edit().putString("history",json).apply()

    private val sharedPreferencesBackgroundColorKey = "BackgroundColor"
    private val sharedPreferencesToolbarColorKey = "ToolbarColor"
    private val sharedPreferencesWindowColorKey = "Window"
}