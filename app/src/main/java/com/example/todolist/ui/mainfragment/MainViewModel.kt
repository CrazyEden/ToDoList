package com.example.todolist.ui.mainfragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.data.repositories.FirebaseRepository
import com.example.todolist.data.repositories.LocalDataRepository
import com.example.todolist.ui.activity.TAG
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository,
    private val database:FirebaseDatabase,
    private val firebaseRepository: FirebaseRepository
):ViewModel() {

    private val _adminIdLiveData = MutableLiveData<String?>()
    val adminIdLiveData: LiveData<String?> = _adminIdLiveData
    private val _dataInFirebaseLiveData = MutableLiveData<Data?>()
    val dataInFirebaseLiveData: LiveData<Data?> = _dataInFirebaseLiveData
    private val _listCurrentUsers = MutableLiveData<List<UserData?>>()
    val listCurrentUsers: LiveData<List<UserData?>> = _listCurrentUsers

    fun coldLoad(id:String){
        viewModelScope.launch(Dispatchers.IO) {
            _adminIdLiveData.postValue(firebaseRepository.getAdminId())
            _listCurrentUsers.postValue(firebaseRepository.getListUsers())
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
                _dataInFirebaseLiveData.postValue(snapshot.getValue(Data::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG,"get data by id is Cancelled")
                _dataInFirebaseLiveData.postValue(localDataRepository.getLocalToDoList())
            }
        })
    }

    private val _userDataLiveData = MutableLiveData<UserData?>()
    val userDataLiveData : LiveData<UserData?> = _userDataLiveData
    private fun loadUserData(id:String) {
        viewModelScope.launch(Dispatchers.IO) {
            _userDataLiveData.postValue(firebaseRepository.getUserData(id))
        }
    }

    fun saveData(targetShowingId:String,dataForSave: List<Todo>){
        firebaseRepository.uploadToDoList(targetShowingId, dataForSave)
    }

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