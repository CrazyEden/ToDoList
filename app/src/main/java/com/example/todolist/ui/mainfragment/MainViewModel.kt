package com.example.todolist.ui.mainfragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.data.model.UserData
import com.example.todolist.data.services.LocalDataService
import com.example.todolist.ui.activity.TAG
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDataService: LocalDataService
):ViewModel() {

    private val _adminIdLiveData = MutableLiveData<String?>()
    val adminIdLiveData: LiveData<String?> = _adminIdLiveData
    private val _dataInFirebaseLiveData = MutableLiveData<UserData?>()
    val dataInFirebaseLiveData: LiveData<UserData?> = _dataInFirebaseLiveData
    private val _listCurrentUsers = MutableLiveData<List<String?>>()
    val listCurrentUsers: LiveData<List<String?>> = _listCurrentUsers

    private var database = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")

    private var obj2:ValueEventListener? = null
    private lateinit var pastId:String
    fun loadTodo(id:String){
        obj2?.let {
            Log.i(TAG,"data observer was removed for id \"$pastId\"")
            database.getReference("data").child(pastId).removeEventListener(it)
        }
        pastId = id
        database.getReference("data").child(id).addValueEventListener(object : ValueEventListener {
            init { obj2 = this }
            override fun onDataChange(snapshot: DataSnapshot) {

                val data = snapshot.getValue(UserData::class.java)
                _dataInFirebaseLiveData.postValue(data)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG,"get data by id is Cancelled")
                _dataInFirebaseLiveData.postValue(null)
            }
        })
    }
    fun loadAdminId(id:String){
        database.getReference("adminId").get().addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            val admId = it.result.getValue(String::class.java).toString()
            _adminIdLiveData.postValue(admId)
            loadTodo(id)
        }
        database.getReference("data").get().addOnCompleteListener { task->
            val b = task.result.children.map { it.key }.toList()//list of users with data
            _listCurrentUsers.postValue(b)
        }
    }
    fun saveData(targetShowingId:String,dataForSave: UserData){
        database.getReference("data").child(targetShowingId)
            .setValue(dataForSave).addOnCompleteListener {
                Log.i(TAG,"data was uploaded to firebase for id $targetShowingId")
            }
    }

    fun getUserData() = localDataService.getLocalToDoList()
    fun saveUserData(json:String){
        localDataService.setLocalToDoList(json)
    }

    override fun onCleared() {
        obj2?.let {
            Log.i(TAG,"data observer was removed for id \"$pastId\"")
            database.getReference("data").child(pastId).removeEventListener(it)
        }
        super.onCleared()
    }
}