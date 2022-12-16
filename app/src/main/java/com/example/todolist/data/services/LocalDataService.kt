package com.example.todolist.data.services

import android.content.SharedPreferences
import android.graphics.Color
import com.example.todolist.data.model.UserData
import com.google.gson.Gson
import javax.inject.Inject

class LocalDataService @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun getBackgroundColor() = sharedPreferences.getInt("BackgroundColor",0)
    fun setBackgroundColor(color:Int) = sharedPreferences.edit().putInt("BackgroundColor",color).apply()

    fun getToolbarColor() = sharedPreferences.getInt("ToolbarColor",0)
    fun setToolbarColor(color:Int) = sharedPreferences.edit().putInt("ToolbarColor",color).apply()

    fun getWindowColor() = sharedPreferences.getInt("Window", Color.BLUE)
    fun setWindowColor(color:Int) = sharedPreferences.edit().putInt("Window",color).apply()

    fun getLocalToDoList(): UserData? {
        val json = sharedPreferences.getString("history",null)
        return Gson().fromJson(json, UserData::class.java)
    }
    fun setLocalToDoList(json:String) =
        sharedPreferences.edit().putString("history",json).apply()
}