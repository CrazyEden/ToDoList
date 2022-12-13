package com.example.todolist.data.model

data class Todo(
    var titleToDo: String? = null,
    var notes: String? = null,
    var secretToDo: Boolean = false,
    var isCompleted:Boolean = false,
    var subTodo: MutableList<SubTodo>? = mutableListOf(),
    var deadlineLong: Long = 0,
    var deadlineString: String? = null,
    )
