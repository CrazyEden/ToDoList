package com.example.todolist.presentation.todos

import com.example.todolist.data.model.Todo

interface ToDoArgs {
    fun itemWasUpdated(todo: Todo, position:Int)
    fun listWasUpdated(list:MutableList<Todo>)
    fun openToDoItem(todo: Todo, position:Int)
}