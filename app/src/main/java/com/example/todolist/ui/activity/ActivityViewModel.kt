package com.example.todolist.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.data.services.LocalDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val localDataService: LocalDataService
):ViewModel() {
    private val _backgroundColorLiveData = MutableLiveData<Int>()
    val backgroundColorLiveData : LiveData<Int> = _backgroundColorLiveData
    fun setBackgroundColor(color:Int){
        localDataService.setBackgroundColor(color)
        _backgroundColorLiveData.postValue(color)
    }
    fun getBackgroundColor() = localDataService.getBackgroundColor()

    private val _toolbarColorLiveData = MutableLiveData<Int>()
    var toolbarColorLiveData : LiveData<Int> = _toolbarColorLiveData
    fun setToolbarBackgroundColor(color:Int){
        localDataService.setToolbarColor(color)
        _toolbarColorLiveData.postValue(color)
    }
    fun getToolbarColor() = localDataService.getToolbarColor()

    private val _windowColorLiveData = MutableLiveData<Int>()
    val windowColorLiveData : LiveData<Int> = _windowColorLiveData
    fun setWindowColor(color:Int){
        localDataService.setWindowColor(color)
        _windowColorLiveData.postValue(color)
    }
    fun getWindowColor() = localDataService.getWindowColor()

}