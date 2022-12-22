package com.example.todolist.di

import com.example.todolist.data.repositories.FirebaseRepository
import com.example.todolist.data.repositories.FirebaseRepositoryImpl
import com.example.todolist.data.repositories.LocalDataRepository
import com.example.todolist.data.repositories.LocalDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface BindsModule{
    @Binds
    @Singleton
    fun bindFirebaseRepository(firebaseRepositoryImpl: FirebaseRepositoryImpl): FirebaseRepository

    @Binds
    @Singleton
    fun bindLocalDataRepository(localDataRepositoryImpl: LocalDataRepositoryImpl): LocalDataRepository
}