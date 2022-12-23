package com.example.todolist.ui.mainfragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.data.repositories.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
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

    fun createToDoObserver(id:String){
        loadUserData(id)
        firebaseRepository.createToDoObserver(id){
            _dataInFirebaseLiveData.postValue(it)
        }
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

    val authId = firebaseRepository.getAuthUserUid()

    override fun onCleared() {
        firebaseRepository.destroyToDoListener()
        super.onCleared()
    }
}