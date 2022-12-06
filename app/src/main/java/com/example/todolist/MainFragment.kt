package com.example.todolist

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.FragmentMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.*


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var database: FirebaseDatabase
    private lateinit var uid:String

    private var localData:DatabaseData? = null
    private var dataInFirebase:DatabaseData? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        adapter = ToDoAdapter{ saveData() }
        binding.rcView.adapter = adapter

        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("history",null)
        localData = Gson().fromJson(json, DatabaseData::class.java) ?: null

        adapter.setData(localData?.listTodo)

        initApp()

        database = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")

        checkForInternet()

        Firebase.auth.signInAnonymously().addOnSuccessListener { it->
            uid = sharedPreferences.getString("uid", null) ?: it.user?.uid!!
            println("UID = $uid")
            database.getReference("data").child(uid).get()
                .addOnCompleteListener {
                    dataInFirebase = it.result.getValue<DatabaseData>()
                    checkForInternet()
                    if (dataInFirebase?.dateLastEdit == null) return@addOnCompleteListener
                    if (localData?.dateLastEdit == null)
                        return@addOnCompleteListener adapter.setData(dataInFirebase?.listTodo)
                    adapter.setData(dataInFirebase?.listTodo)
                    if (localData?.dateLastEdit!! < dataInFirebase?.dateLastEdit!!)
                        adapter.setData(dataInFirebase?.listTodo)
                    else saveData()
                }
        }


        binding.buttonAdd.setOnClickListener {
            if(binding.textView.text.isNullOrEmpty()) return@setOnClickListener
            adapter.addData(Todo(binding.textView.text.toString()))
            saveData()
            binding.textView.text.clear()
        }
        return binding.root
    }

    private fun initApp() {
        FirebaseApp.initializeApp(requireContext())
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }

    private fun saveData(){
        checkForInternet()
        database.getReference("data").child(uid).setValue(adapter.getDatabaseData(getCurrentTime())).addOnCompleteListener { checkForInternet() }
    }
    override fun onPause() {
        sharedPreferences.edit().putString("history",adapter.toJson()).apply()
        runCatching {
            if (localData?.dateLastEdit!! > dataInFirebase?.dateLastEdit!!)
                saveData()
        }

        super.onPause()
    }

    @SuppressLint("ServiceCast")
    private fun checkForInternet() {
        // register activity with the connectivity manager service
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork
        if (network == null) {
            binding.imageNoEthernet.visibility = View.VISIBLE
            return
        }
        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)
        if (activeNetwork == null ) {
            binding.imageNoEthernet.visibility = View.VISIBLE
            return
        }
        if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            binding.imageNoEthernet.visibility = View.GONE
        else binding.imageNoEthernet.visibility = View.VISIBLE

    }

    companion object{
        fun getCurrentTime() = Calendar.getInstance().time.time
    }

}
