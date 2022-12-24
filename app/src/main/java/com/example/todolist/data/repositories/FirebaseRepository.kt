package com.example.todolist.data.repositories

import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.domain.repositories.DataObserver
import com.google.firebase.auth.AuthCredential


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
    fun createToDoObserver(id:String, dataObserver: DataObserver)
    fun destroyToDoListener()
    suspend fun signInByGoogle(credential: AuthCredential): Exception?
    suspend fun signInWithEmailAndPassword(email: String, password: String): Exception?
    fun sendEmailToRestPassword(email:String)
    suspend fun createNewUserByEmailAndPassword(email: String, password: String): Exception?
}
