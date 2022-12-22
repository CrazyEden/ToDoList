package com.example.todolist.data.repositories

import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData


interface FirebaseRepository {
    fun getAuthUserUid(): String
    fun updateCurrentUserNickname(newNickname: String)
    suspend fun getMyData(): Data?
    suspend fun getUserData(id:String): UserData?
    fun uploadToDoList(targetShowingId:String,dataForSave:List<Todo>)
    fun signOut()
    fun uploadNotes(list:List<Note>)
    suspend fun getAdminId(): String?
    suspend fun getListUsers():List<UserData>
}