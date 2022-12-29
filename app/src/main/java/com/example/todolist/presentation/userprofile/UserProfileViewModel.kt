package com.example.todolist.presentation.userprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Data
import com.example.todolist.data.repositories.FirebaseRepository
import com.example.todolist.data.repositories.LocalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val localDataRepository: LocalDataRepository
) : ViewModel(){

    fun updateNickname(nickname:String){
        firebaseRepository.updateCurrentUserNickname(nickname)
    }
    fun signOut() = firebaseRepository.signOut()
    fun getLocalData(): Data? =
        localDataRepository.getLocalData()

    private val _myDataLiveData = MutableLiveData<Data>()
    val myDataLiveData:LiveData<Data> = _myDataLiveData
    init {
        viewModelScope.launch(Dispatchers.IO) {
            _myDataLiveData.postValue(firebaseRepository.getMyData())
        }
    }
}