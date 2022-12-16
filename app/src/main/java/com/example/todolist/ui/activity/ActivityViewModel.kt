package com.example.todolist.ui.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.data.services.LocalDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val localDataService: LocalDataService
):ViewModel() {
    val backgroundColorLiveData= MutableLiveData<Int>()
    fun setBackgroundColor(color:Int){
        localDataService.setBackgroundColor(color)
        backgroundColorLiveData.postValue(color)
    }

    val toolbarColorLiveData= MutableLiveData<Int>()
    fun setToolbarBackgroundColor(color:Int){
        localDataService.setToolbarColor(color)
        toolbarColorLiveData.postValue(color)
    }

    val windowColorLiveData = MutableLiveData<Int>()
    fun setWindowColor(color:Int){
        windowColorLiveData.postValue(color)
        localDataService.setWindowColor(color)
    }
    fun getWindowColor() = localDataService.getWindowColor()

    fun getBackgroundColor() = localDataService.getBackgroundColor()
    fun getToolbarColor() = localDataService.getToolbarColor()
}