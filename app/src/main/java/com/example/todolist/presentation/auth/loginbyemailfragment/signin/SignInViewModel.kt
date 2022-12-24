package com.example.todolist.presentation.auth.loginbyemailfragment.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repositories.FirebaseRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
):ViewModel() {

    private val _signInWithEmailAndPassword = MutableLiveData<Exception?>()
    val signInWithEmailAndPassword:LiveData<Exception?> = _signInWithEmailAndPassword

    fun signInWithEmailAndPassword(email:String,password:String){
        viewModelScope.launch {
            _signInWithEmailAndPassword.postValue(firebaseRepository.signInWithEmailAndPassword(email,password))
        }
    }

    fun sendEmailToResetPassword(email:String){
        firebaseRepository.sendEmailToRestPassword(email)
    }
}