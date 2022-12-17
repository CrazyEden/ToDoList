package com.example.todolist.ui.loginbyemailfragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.todolist.ui.loginbyemailfragment.signin.SignInByLoginFragment
import com.example.todolist.ui.loginbyemailfragment.signup.SignUpByLoginFragment

class LogInViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    :FragmentStateAdapter(fragmentManager,lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) SignInByLoginFragment()
        else SignUpByLoginFragment()
    }
}