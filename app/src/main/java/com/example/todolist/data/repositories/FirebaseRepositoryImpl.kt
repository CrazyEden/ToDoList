package com.example.todolist.data.repositories

import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val localDataRepository: LocalDataRepository
): FirebaseRepository {
    override fun getAuthUserUid(): String = auth.currentUser!!.uid

    override fun updateCurrentUserNickname(newNickname: String){
        database.getReference("data").child(getAuthUserUid()).child("userData").child("nickname")
            .setValue(newNickname)
    }
    override suspend fun getMyData(): Data? =
        database.getReference("data").child(getAuthUserUid()).get().await().getValue(Data::class.java)
            ?:localDataRepository.getLocalToDoList()
    override suspend fun getUserData(id:String): UserData? =
        database.getReference("data").child(id)
            .child("userData").get().await().getValue(UserData::class.java)

    override fun uploadToDoList(targetShowingId:String,dataForSave:List<Todo>){
        database.getReference("data").child(targetShowingId).child("listTodo")
            .setValue(dataForSave)
    }

    override fun signOut() = auth.signOut()

    override fun uploadNotes(list:List<Note>){
        database.getReference("data").child(getAuthUserUid()).child("listNotes")
            .setValue(list)
    }
    override suspend fun getAdminId(): String? =
        database.getReference("adminId").get().await().getValue(String::class.java)

    override suspend fun getListUsers():List<UserData> =
        database.getReference("data").get().await().children.map {
            UserData(
                it.key,
                it.child("userData").child("nickname").getValue(String::class.java)
            )
        }.toList()
}