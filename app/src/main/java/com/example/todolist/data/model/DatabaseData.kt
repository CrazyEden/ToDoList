package com.example.todolist.data.model


data class DatabaseData(
    val dateLastEdit:Long? = null,
    var listTodo:List<Todo>? = null,
    var userId:String? = null
)