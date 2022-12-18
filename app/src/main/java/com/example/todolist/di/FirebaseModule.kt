package com.example.todolist.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideDatabase() = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")

    @Provides
    @Singleton
    fun provideAuth() = Firebase.auth
}