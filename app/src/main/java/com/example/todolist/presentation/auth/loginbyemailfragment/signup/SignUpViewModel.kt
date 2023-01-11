package com.example.todolist.presentation.auth.loginbyemailfragment.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repositories.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
):ViewModel() {
    private val _createUserLiveData = MutableLiveData<Exception?>()
    val createUserLiveData:LiveData<Exception?> = _createUserLiveData
    fun createUserByEmailAndPassword(email:String,password:String){
        viewModelScope.launch {
            val res =firebaseRepository.createNewUserByEmailAndPassword(email, password)
            _createUserLiveData.postValue(res)
        }
    }
}