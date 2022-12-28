package com.example.todolist.data.model


data class Data(
    var listTodo:List<Todo> = listOf(),
    var listNotes:List<Note> = listOf(),
    var userData: UserData? = null
)