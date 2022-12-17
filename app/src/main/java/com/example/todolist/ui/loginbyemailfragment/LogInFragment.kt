package com.example.todolist.ui.loginbyemailfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.todolist.R
import com.example.todolist.databinding.FragmentLogInBinding


class LogInFragment : Fragment() {
    lateinit var binding:FragmentLogInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLogInBinding.inflate(inflater,container,false)
        val adapter = LogInViewPagerAdapter(parentFragmentManager,lifecycle)
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.topNavMenu.menu.getItem(position).isChecked = true
                super.onPageSelected(position)
            }
        })
        binding.topNavMenu.setOnItemSelectedListener {
            when(it.itemId){
                R.id.sign_in ->{
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.sign_up ->{
                    binding.viewPager.currentItem = 1
                    true
                }
                else -> false
            }
        }
        return binding.root
    }

}