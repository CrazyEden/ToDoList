package com.example.todolist.domain.repositories

import android.content.SharedPreferences
import android.graphics.Color
import com.example.todolist.data.model.Data
import com.example.todolist.data.repositories.LocalDataRepository
import com.google.gson.Gson
import javax.inject.Inject

class LocalDataRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
): LocalDataRepository {

    override fun getBackgroundColor(): Int =
        sharedPreferences.getInt(sharedPreferencesBackgroundColorKey,Color.DKGRAY)
    override fun setBackgroundColor(color:Int): Unit =
        sharedPreferences.edit().putInt(sharedPreferencesBackgroundColorKey,color).apply()

    override fun getToolbarColor(): Int =
        sharedPreferences.getInt(sharedPreferencesToolbarColorKey,Color.GRAY)
    override fun setToolbarColor(color:Int): Unit =
        sharedPreferences.edit().putInt(sharedPreferencesToolbarColorKey,color).apply()

    override fun getWindowColor(): Int =
        sharedPreferences.getInt(sharedPreferencesWindowColorKey, Color.GRAY)
    override fun setWindowColor(color:Int): Unit =
        sharedPreferences.edit().putInt(sharedPreferencesWindowColorKey,color).apply()

    override fun getLocalData(): Data? {
        val json = sharedPreferences.getString(sharedPreferencesData,null)
        return Gson().fromJson(json, Data::class.java)
    }
    override fun setLocalData(json:String): Unit =
        sharedPreferences.edit().putString(sharedPreferencesData,json).apply()



    private val sharedPreferencesBackgroundColorKey = "BackgroundColor"
    private val sharedPreferencesToolbarColorKey = "ToolbarColor"
    private val sharedPreferencesWindowColorKey = "Window"
    private val sharedPreferencesData = "history"
}