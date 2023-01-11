package com.example.todolist.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Todo(
    var titleToDo: String = "",
    var secretToDo: Boolean = false,
    var isCompleted:Boolean = false,
    var subTodo: MutableList<SubTodo> = mutableListOf(),
    var comments: MutableList<String> = mutableListOf(),
    var deadlineLong: Long = 0,
    var deadlineString: String = ""
): Parcelable
