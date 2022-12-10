package com.example.todolist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DatabaseData(
    val dateLastEdit:Long? = null,
    var listTodo:List<Todo>? = null,
    var userId:String? = null
): Parcelable

@Parcelize
data class Todo(
    var string: String? = null,
    var comment: String? = null,
    var secretToDo: Boolean = false,
    var isCompleted:Boolean = false
):Parcelable
