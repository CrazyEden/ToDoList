package com.example.todolist.data.repositories

import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val localDataRepository: LocalDataRepository
){
    fun getAuthUserUid() = auth.currentUser!!.uid

    fun updateAuthUserNickname(newNickname: String){
        database.getReference("data").child(getAuthUserUid()).child("userData").child("nickname")
            .setValue(newNickname)
    }
    suspend fun getMyData(): Data? =
        database.getReference("data").child(getAuthUserUid()).get().await().getValue(Data::class.java)
            ?:localDataRepository.getLocalToDoList()
    suspend fun getUserData(id:String): UserData? =
        database.getReference("data").child(id)
            .child("userData").get().await().getValue(UserData::class.java)

    fun uploadToDoList(targetShowingId:String,dataForSave:List<Todo>){
        database.getReference("data").child(targetShowingId).child("listTodo")
            .setValue(dataForSave)
    }

    fun signOut() = auth.signOut()

    fun uploadNotes(list:List<Note>){
        database.getReference("data").child(getAuthUserUid()).child("listNotes")
            .setValue(list)
    }
    suspend fun getAdminId() = database.getReference("adminId").get().await().getValue(String::class.java)

    suspend fun getListUsers():List<UserData> =
        database.getReference("data").get().await().children.map {
            UserData(
                it.key,
                it.child("userData").child("nickname").getValue(String::class.java)
            )
        }.toList()
}