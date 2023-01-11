package com.example.todolist.presentation.todos.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Todo
import com.example.todolist.data.repositories.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoInfoViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
):ViewModel() {

    private val _updateLiveData = MutableLiveData<Exception?>()
    val updateLiveData:LiveData<Exception?> = _updateLiveData
    fun updateTodo(todo: Todo,id:String, position:Int){
        viewModelScope.launch {
            val res = firebaseRepository.updateToDo(todo,id,position)
            _updateLiveData.postValue(res)
        }
    }

    fun createNewTodo(todo:Todo,id:String) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = firebaseRepository.createNewToDo(todo,id)
            _updateLiveData.postValue(res)
        }
    }
}