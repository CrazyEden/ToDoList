package com.example.todolist.data.model

data class Todo(
    var string: String? = null,
    var notes: String? = null,
    var secretToDo: Boolean = false,
    var isCompleted:Boolean = false,
    var subTodo: MutableList<SubTodo>? = mutableListOf(),
    var duration: String = "День"
)
