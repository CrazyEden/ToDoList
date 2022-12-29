package com.example.todolist.presentation.activity


import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.todolist.R
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.presentation.auth.SignInFragment
import com.example.todolist.presentation.notes.NotesFragment
import com.example.todolist.presentation.todos.ToDoListFragment
import com.example.todolist.presentation.userprofile.UserProfileFragment
import com.example.todolist.presentation.userprofile.settings.SettingsFragment
import com.example.todolist.utils.NetworkChangeReceiver
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import dagger.hilt.android.AndroidEntryPoint


const val TAG = "xdd"
@AndroidEntryPoint
class MainActivity : AppCompatActivity(){
    private val vModel: ActivityViewModel by viewModels()
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        initSafetyNet()
        iniAppColors()
        setContentView(binding.root)
        registerFragmentLifecycleListener()
        NetworkChangeReceiver(this, binding.imageNoEthernet)
        binding.bottomNavMenu.setOnItemSelectedListener {
            when(it.itemId){
                R.id.listTodo_menu->{ openFragment(ToDoListFragment()) }
                R.id.notes_menu->{ openFragment(NotesFragment()) }
                R.id.profile_menu->{ openFragment(UserProfileFragment()) }
            }
            true
        }
        if(savedInstanceState != null) return
        if (vModel.isCurrentUserNull()) openFragment(SignInFragment())
        else openFragment(ToDoListFragment())
    }


    private fun registerFragmentLifecycleListener() {
        //auto hide bottom navigation bar
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks(){
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f is ToDoListFragment ||f is NotesFragment ||f is UserProfileFragment || f is SettingsFragment) {//whitelist
                    binding.bottomNavMenu.visibility = View.VISIBLE
                    supportActionBar?.show()
                }
                else {
                    binding.bottomNavMenu.visibility = View.GONE
                    supportActionBar?.hide()
                }
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
            }

        },false)
    }

    private fun openFragment(fragment: Fragment){
        if (fragment is ToDoListFragment) binding.bottomNavMenu.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.container,fragment)
            .commit()
    }

    private fun iniAppColors() {
        supportActionBar?.setBackgroundDrawable(ColorDrawable(vModel.getToolbarColor()))
        binding.container.setBackgroundColor(vModel.getBackgroundColor())
        window.setBackgroundDrawable(ColorDrawable(vModel.getWindowColor()))

        vModel.toolbarColorLiveData.observe(this){
            supportActionBar?.setBackgroundDrawable(ColorDrawable(it))
        }
        vModel.backgroundColorLiveData.observe(this){
            binding.container.setBackgroundColor(it)
        }
        vModel.windowColorLiveData.observe(this){
            window.setBackgroundDrawable(ColorDrawable(it))
        }

    }

    private fun initSafetyNet(){
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }
}


