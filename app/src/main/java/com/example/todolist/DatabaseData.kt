package com.example.todolist


data class DatabaseData(
    val dateLastEdit:Long? = null,
    var listTodo:List<Todo>? = null,
    var userId:String? = null
)

data class Todo(
    var string: String? = null,
    var comment: String? = null,
    var secretToDo: Boolean = false,
    var isCompleted:Boolean = false,
    var subTodo: MutableList<SubTodo>? = mutableListOf()
)

data class SubTodo(
    var string: String? = null,
    var isCompleted:Boolean = false
)