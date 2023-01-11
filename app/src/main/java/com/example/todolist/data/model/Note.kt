package com.example.todolist.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    var title:String = "",
    var body:String = ""
):Parcelable
