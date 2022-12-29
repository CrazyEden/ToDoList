package com.example.todolist.presentation.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Note
import com.example.todolist.data.repositories.FirebaseRepository
import com.example.todolist.data.repositories.LocalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val localDataRepository: LocalDataRepository
):ViewModel() {
    private val _myDataLiveData = MutableLiveData<Data?>()
    val myDataLiveData:LiveData<Data?> = _myDataLiveData
    init {
        viewModelScope.launch(Dispatchers.IO) {
            _myDataLiveData.postValue(firebaseRepository.getMyData())
        }
    }
    fun uploadNotesToFirebase(list:List<Note>){
        firebaseRepository.uploadNotes(list)
    }

    fun getLocalNotes(): List<Note>? =
        localDataRepository.getLocalData()?.listNotes
}