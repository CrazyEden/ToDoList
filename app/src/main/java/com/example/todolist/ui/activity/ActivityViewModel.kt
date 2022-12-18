package com.example.todolist.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.data.repositories.LocalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository
):ViewModel() {
    private val _backgroundColorLiveData = MutableLiveData<Int>()
    val backgroundColorLiveData : LiveData<Int> = _backgroundColorLiveData
    fun setBackgroundColor(color:Int){
        localDataRepository.setBackgroundColor(color)
        _backgroundColorLiveData.postValue(color)
    }
    fun getBackgroundColor() = localDataRepository.getBackgroundColor()

    private val _toolbarColorLiveData = MutableLiveData<Int>()
    var toolbarColorLiveData : LiveData<Int> = _toolbarColorLiveData
    fun setToolbarBackgroundColor(color:Int){
        localDataRepository.setToolbarColor(color)
        _toolbarColorLiveData.postValue(color)
    }
    fun getToolbarColor() = localDataRepository.getToolbarColor()

    private val _windowColorLiveData = MutableLiveData<Int>()
    val windowColorLiveData : LiveData<Int> = _windowColorLiveData
    fun setWindowColor(color:Int){
        localDataRepository.setWindowColor(color)
        _windowColorLiveData.postValue(color)
    }
    fun getWindowColor() = localDataRepository.getWindowColor()

}