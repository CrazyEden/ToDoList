package com.example.todolist.data.model


data class Data(
    var listTodo:List<Todo>? = null,
    var listNotes:List<Note>? = null,
    var userData: UserData? = null
)