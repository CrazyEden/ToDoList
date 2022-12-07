package com.example.todolist

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.FragmentMainBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
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


class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var database: FirebaseDatabase
    private lateinit var uid:String

    private var localData:DatabaseData? = null
    private var dataInFirebase:DatabaseData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        adapter = ToDoAdapter{ saveData() }
        binding.rcView.adapter = adapter

        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("history",null)
        localData = Gson().fromJson(json, DatabaseData::class.java) ?: null

        adapter.setData(localData?.listTodo)

        initApp()
        signIn()

        binding.buttonAdd.setOnClickListener {
            if(binding.textView.text.isNullOrEmpty()) return@setOnClickListener
            adapter.addData(Todo(binding.textView.text.toString()))
            saveData()
            binding.textView.text.clear()
        }
        return binding.root
    }

    private fun signIn() {
        val oneTapClient = Identity.getSignInClient(requireActivity())
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .build()
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener { it ->
            uid = sharedPreferences.getString("uid", null) ?: it.pendingIntent.creatorUid.toString()
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
//        Firebase.auth.signInAnonymously().addOnSuccessListener { it->
//            uid = sharedPreferences.getString("uid", null) ?: it.user?.uid!!
//            println("UID = $uid")
//            database.getReference("data").child(uid).get()
//                .addOnCompleteListener {
//                    dataInFirebase = it.result.getValue<DatabaseData>()
//                    checkForInternet()
//                    if (dataInFirebase?.dateLastEdit == null) return@addOnCompleteListener
//                    if (localData?.dateLastEdit == null)
//                        return@addOnCompleteListener adapter.setData(dataInFirebase?.listTodo)
//                    adapter.setData(dataInFirebase?.listTodo)
//                    if (localData?.dateLastEdit!! < dataInFirebase?.dateLastEdit!!)
//                        adapter.setData(dataInFirebase?.listTodo)
//                    else saveData()
//                }
//        }
    }

    private fun initApp() {
        FirebaseApp.initializeApp(requireContext())
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
        database = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")
        checkForInternet()
    }

    private fun saveData(){
        checkForInternet()
        database.getReference("data").child(uid).setValue(adapter.getDatabaseData(getCurrentTime())).addOnCompleteListener { checkForInternet() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
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
        fun setVisible(){ binding.imageNoEthernet.visibility = View.VISIBLE }
        // register activity with the connectivity manager service
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return setVisible()
        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?:return setVisible()
        if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            binding.imageNoEthernet.visibility = View.GONE
        else setVisible()
    }

    companion object{
        fun getCurrentTime() = Calendar.getInstance().time.time
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val clipBoardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        when (item.itemId){
            R.id.export_id->{
                val clip = ClipData.newPlainText("xdd", uid)
                clipBoardManager.setPrimaryClip(ClipData(clip))
                makeToast("Ключ скопирован в буфер обмена")
            }
            R.id.import_id->{
                runCatching {
                    makeToast("Поиск ключа в буфере обмена..")
                    val clipData = clipBoardManager.primaryClip?.getItemAt(0)?.text.toString()
                    uid = clipData
                    sharedPreferences.edit().putString("uid",clipData).apply()
                    database.getReference("data").child(uid).get()
                        .addOnCompleteListener {
                            val tempDataInFirebase = it.result.getValue<DatabaseData>()
                            checkForInternet()
                            if (tempDataInFirebase?.dateLastEdit == null) return@addOnCompleteListener
                            adapter.setData(tempDataInFirebase.listTodo)
                        }
                }.getOrElse { makeToast("Что-то пошло не так") }
            }
            R.id.reset_id->{
                sharedPreferences.edit().remove("uid").apply()
            }
        }
        return true
    }
    private fun makeToast(message:String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
    }
}
