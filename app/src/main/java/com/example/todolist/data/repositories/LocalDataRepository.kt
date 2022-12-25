package com.example.todolist.data.repositories

import com.example.todolist.data.model.Data

interface LocalDataRepository {
    fun getBackgroundColor(): Int
    fun setBackgroundColor(color:Int)

    fun getToolbarColor(): Int
    fun setToolbarColor(color:Int)

    fun getWindowColor(): Int
    fun setWindowColor(color:Int)

    fun getLocalToDoList(): Data?
    fun setLocalToDoList(json:String)

    fun getIsDarkMode(): Boolean
    fun setIsDarkMode(isOn:Boolean)
}