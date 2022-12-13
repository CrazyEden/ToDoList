package com.example.todolist.ui.mainfragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.UserData
import com.example.todolist.ui.TAG
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MainViewModel:ViewModel() {

    private val _adminIdLiveData = MutableLiveData<String?>()
    val adminIdLiveData: LiveData<String?> = _adminIdLiveData
    private val _dataInFirebaseLiveData = MutableLiveData<UserData?>()
    val dataInFirebaseLiveData: LiveData<UserData?> = _dataInFirebaseLiveData
    private val _listCurrentUsers = MutableLiveData<List<String?>>()
    val listCurrentUsers: LiveData<List<String?>> = _listCurrentUsers

    private var database = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")

    private var obj1:ValueEventListener? = null
    private var obj2:ValueEventListener? = null
    private lateinit var pastId:String
    fun loadDataByUserId(id:String){
        obj1?.let {
            Log.i(TAG,"adminId observer was removed")
            database.getReference("adminId").removeEventListener(it)
        }
        obj2?.let {
            Log.i(TAG,"data observer was removed for id \"$pastId\"")
            database.getReference("data").child(pastId).removeEventListener(it)
        }
        pastId = id
        viewModelScope.launch(Dispatchers.IO) {

            database.getReference("adminId").addValueEventListener(object :ValueEventListener{
                init { obj1 = this }
                override fun onDataChange(snapshot: DataSnapshot) {
                    val admId = snapshot.getValue(String::class.java).toString()
                    _adminIdLiveData.postValue(admId)
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
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG,"get admin id is Cancelled")
                    _adminIdLiveData.postValue(null)
                }

            })
            runCatching {
                val b = database.getReference("data").get().await().children.map { it.key }.toList()//list of users with data
                _listCurrentUsers.postValue(b)
            }.getOrElse { Log.e(TAG,"loading list users is failed") }

        }

    }
    fun saveData(targetShowingId:String,dataForSave: UserData){
        database.getReference("data").child(targetShowingId)
            .setValue(dataForSave).addOnCompleteListener {
                Log.i(TAG,"data was uploaded to firebase for id $targetShowingId")
            }
    }

    override fun onCleared() {
        obj1?.let {
            Log.i(TAG,"adminId observer was removed")
            database.getReference("adminId").removeEventListener(it)
        }
        obj2?.let {
            Log.i(TAG,"data observer was removed for id \"$pastId\"")
            database.getReference("data").child(pastId).removeEventListener(it)
        }
        super.onCleared()
    }
}