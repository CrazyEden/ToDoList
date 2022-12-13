package com.example.todolist.data.model


data class UserData(
    val dateLastEdit:Long? = null,
    var listTodo:List<Todo>? = null,
    var userId:String? = null
)