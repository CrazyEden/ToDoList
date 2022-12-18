package com.example.todolist.ui.mainfragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.data.repositories.LocalDataRepository
import com.example.todolist.ui.activity.TAG
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository
):ViewModel() {

    private val _adminIdLiveData = MutableLiveData<String?>()
    val adminIdLiveData: LiveData<String?> = _adminIdLiveData
    private val _dataInFirebaseLiveData = MutableLiveData<Data?>()
    val dataInFirebaseLiveData: LiveData<Data?> = _dataInFirebaseLiveData
    private val _listCurrentUsers = MutableLiveData<List<UserData?>>()
    val listCurrentUsers: LiveData<List<UserData?>> = _listCurrentUsers

    private var database = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")

    fun coldLoad(id:String){
        viewModelScope.launch(Dispatchers.IO) {
            val adminId =
                database.getReference("adminId").get().await().getValue(String::class.java)
            val listOfUsers:List<UserData> =
                database.getReference("data").get().await().children.map {
                UserData(
                    it.key,
                    it.child("userData").child("nickname").getValue(String::class.java)
                )
            }.toList()
            _adminIdLiveData.postValue(adminId)
            _listCurrentUsers.postValue(listOfUsers)
            createToDoObserver(id)
        }
    }
    private var obj2:ValueEventListener? = null
    private lateinit var pastId:String
    fun createToDoObserver(id:String){
        obj2?.let {
            Log.i(TAG,"data observer was removed for id \"$pastId\"")
            database.getReference("data").child(pastId).removeEventListener(it)
        }
        pastId = id
        loadUserData(id)
        database.getReference("data").child(id).addValueEventListener(object : ValueEventListener {
            init { obj2 = this }
            override fun onDataChange(snapshot: DataSnapshot) {

                val data = snapshot.getValue(Data::class.java)
                _dataInFirebaseLiveData.postValue(data)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG,"get data by id is Cancelled")
                _dataInFirebaseLiveData.postValue(null)
            }
        })
    }

    private val _userDataLiveData = MutableLiveData<UserData?>()
    val userDataLiveData : LiveData<UserData?> = _userDataLiveData
    private fun loadUserData(id:String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userdata = database.getReference("data").child(id)
                .child("userData").get().await().getValue(UserData::class.java)
            _userDataLiveData.postValue(userdata)
        }
    }

    fun saveData(targetShowingId:String,dataForSave: List<Todo>){
        database.getReference("data").child(targetShowingId).child("listTodo")
            .setValue(dataForSave).addOnCompleteListener {
                Log.i(TAG,"data was uploaded to firebase for id $targetShowingId")
            }
    }
    fun updateNickName(nickname: String,targetShowingId:String){
        database.getReference("data").child(targetShowingId).child("userData").child("nickname")
            .setValue(nickname).addOnCompleteListener {
                Log.i(TAG,"nickname was uploaded to firebase for id $targetShowingId")
            }
    }

    fun getUserData() = localDataRepository.getLocalToDoList()
    fun saveUserData(json:String){
        localDataRepository.setLocalToDoList(json)
    }

    override fun onCleared() {
        obj2?.let {
            Log.i(TAG,"data observer was removed for id \"$pastId\"")
            database.getReference("data").child(pastId).removeEventListener(it)
        }
        super.onCleared()
    }
}