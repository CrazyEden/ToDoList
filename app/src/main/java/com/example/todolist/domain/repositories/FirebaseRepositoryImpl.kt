package com.example.todolist.domain.repositories

import android.util.Log
import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.data.repositories.FirebaseRepository
import com.example.todolist.data.repositories.LocalDataRepository
import com.example.todolist.presentation.activity.TAG
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

typealias DataObserver = (data:Data?) -> Unit

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
        database.getReference("data").child(getAuthUserUid()).get().await()
            .getValue(Data::class.java)


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
        }

    private var obj2: ValueEventListener? = null
    private var pastId: String? = null
    override fun createToDoObserver(id:String, dataObserver: DataObserver){
        destroyToDoListener()
        pastId = id
        database.getReference("data").child(id).addValueEventListener(object : ValueEventListener {
            init { obj2 = this }
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(Data::class.java)
                dataObserver(data)
                if(id == getAuthUserUid()){
                    val json = Gson().toJson(data)
                    localDataRepository.setLocalData(json)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    override fun destroyToDoListener(){
        if (obj2 != null && pastId != null){
            Log.i(TAG,"data observer was removed for id \"$pastId\"")
            database.getReference("data").child(pastId!!).removeEventListener(obj2!!)
        }
    }

    override suspend fun signInByGoogle(credential: AuthCredential): Exception? {
        val authResult = auth.signInWithCredential(credential)
        authResult.await()
        if (authResult.isSuccessful){
            val uid = auth.currentUser!!.uid
            database.getReference("data").child(uid).child("userData")
                .child("userId").setValue(uid)
            if (database.getReference("data").child(uid).child("userData")
                    .child("nickname").get().await().getValue(String::class.java) == null){
                database.getReference("data").child(uid).child("userData")
                    .child("nickname").setValue(auth.currentUser?.displayName).await()
                return null
            }
            return null
        }

        return authResult.exception
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Exception? {
        val signIn = auth.signInWithEmailAndPassword(email, password)
        signIn.await()
        if (signIn.isSuccessful){
            val uid = auth.currentUser!!.uid
            database.getReference("data").child(uid).child("userData")
                .child("userId").setValue(uid)
            if (database.getReference("data").child(uid).child("userData")
                    .child("nickname").get().await().getValue(String::class.java) == null){
                database.getReference("data").child(uid).child("userData")
                    .child("nickname").setValue(uid).await()
                return null
            }
            return null
        }
        else return signIn.exception
    }

    override fun sendEmailToRestPassword(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    override suspend fun createNewUserByEmailAndPassword(email: String, password: String): Exception? {
        val user = auth.createUserWithEmailAndPassword(email, password)
        user.await()
        return if (user.isSuccessful) null
        else user.exception
    }

    override suspend fun updateToDo(todo: Todo, id: String,position:Int): Exception? {
        val res = database.getReference("data").child(id)
            .child("listTodo").child(position.toString())
            .setValue(todo)
        res.await()
        return if (res.isSuccessful) null
        else res.exception
    }

    private suspend fun getLightTodoList(id:String)=
        database.getReference("data").child(id).child("listTodo")
            .get().await().children.count()


    override suspend fun createNewToDo(todo: Todo,id:String): Exception? {
        val position = getLightTodoList(id)
        val res = database.getReference("data").child(id)
            .child("listTodo").child(position.toString()).setValue(todo)
        res.await()
        return if (res.isSuccessful) null
        else res.exception
    }

    private suspend fun getCountNotes()=
        database.getReference("data").child(getAuthUserUid()).child("listNotes")
            .get().await().children.count()

    override suspend fun createNewNote(note: Note): Exception? {
        val res = database.getReference("data").child(getAuthUserUid()).child("listNotes")
            .child(getCountNotes().toString()).setValue(note)
        res.await()
        return if (res.isSuccessful) null
        else res.exception
    }

    override suspend fun updateNote(note: Note, position: Int): Exception? {
        val res = database.getReference("data").child(getAuthUserUid()).child("listNotes")
            .child(position.toString()).setValue(note)
        res.await()
        return if (res.isSuccessful) null
        else res.exception
    }
}