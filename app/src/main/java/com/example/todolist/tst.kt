package com.example.todolist

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

fun main(){
    val database = Firebase.database
    val myRef = database.getReference("message")

    myRef.setValue("Hello, World!")
}