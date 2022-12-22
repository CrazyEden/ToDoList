package com.example.todolist.ui.notefragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Data
import com.example.todolist.data.model.Note
import com.example.todolist.data.repositories.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
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
}