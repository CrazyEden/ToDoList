package com.example.todolist.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubTodo(
    var string: String? = null,
    var isCompleted:Boolean = false
):Parcelable
