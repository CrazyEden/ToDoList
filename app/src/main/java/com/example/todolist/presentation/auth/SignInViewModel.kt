package com.example.todolist.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repositories.FirebaseRepository
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    private val _googleSignInResult = MutableLiveData<Exception?>()
    val googleSignInResult:LiveData<Exception?> = _googleSignInResult
    fun googleSignIn(credential: AuthCredential) {
        viewModelScope.launch {
            val result = firebaseRepository.signInByGoogle(credential)
            _googleSignInResult.postValue(result)
        }
    }
}