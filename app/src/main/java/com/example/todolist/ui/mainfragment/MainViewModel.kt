package com.example.todolist.ui.mainfragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.DatabaseData
import com.example.todolist.ui.TAG
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainViewModel:ViewModel() {

    private val _adminIdLiveData = MutableLiveData<String?>()
    private val _dataInFirebaseLiveData = MutableLiveData<DatabaseData?>()
    val adminIdLiveData: LiveData<String?> = _adminIdLiveData
    val dataInFirebaseLiveData: LiveData<DatabaseData?> = _dataInFirebaseLiveData

    private var database = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")



    fun loadDataByUserId(id:String){
        viewModelScope.launch {
            database.getReference("adminId").ref.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val admId = snapshot.getValue(String::class.java).toString()
                    _adminIdLiveData.postValue(admId)

                    database.getReference("data").child(id).ref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.getValue(DatabaseData::class.java)
                            _dataInFirebaseLiveData.postValue(data)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.wtf(TAG,"get data by id is Cancelled")
                            _dataInFirebaseLiveData.postValue(null)
                        }
                    })
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.wtf(TAG,"get admin id is Cancelled")
                    _adminIdLiveData.postValue(null)
                }

            })

        }

    }
    fun saveData(targetShowingId:String,dataForSave: DatabaseData){
        database.getReference("data").child(targetShowingId)
            .setValue(dataForSave).addOnCompleteListener {
                Log.wtf(TAG,"data was uploaded to firebase for id $targetShowingId")
            }
    }
}