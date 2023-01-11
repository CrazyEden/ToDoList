package com.example.todolist.presentation.notes.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Note
import com.example.todolist.data.repositories.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteInfoViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
): ViewModel() {
    private val _noteUpdateLiveData= MutableLiveData<Exception?>()
    val noteUpdateLiveData: LiveData<Exception?> = _noteUpdateLiveData
    fun createNewNote(note: Note) {
        viewModelScope.launch {
            _noteUpdateLiveData.postValue(firebaseRepository.createNewNote(note))
        }
    }

    fun updateNote(note: Note, position: Int) {
        viewModelScope.launch {
            _noteUpdateLiveData.postValue(firebaseRepository.updateNote(note,position))
        }
    }

}
