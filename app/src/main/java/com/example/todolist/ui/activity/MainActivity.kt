package com.example.todolist.ui.activity


import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import dagger.hilt.android.AndroidEntryPoint


const val TAG = "xdd"
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val vModel: ActivityViewModel by viewModels()
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

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
}