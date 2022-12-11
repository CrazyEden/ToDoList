package com.example.todolist

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.FragmentMainBinding
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var targetShowingId:String
    private lateinit var currentAuthId:String
    private lateinit var adminAuthId:String

    private var localData:DatabaseData? = null
    private var dataInFirebase:DatabaseData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        initVars()
        database.getReference("data").child(targetShowingId).get()
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

        checkForInternet()

        binding.buttonAddTodo.setOnClickListener {
            openDialogToCreateNewTodo()
        }
        return binding.root
    }

    private fun openDialogToCreateNewTodo() {
        AlertDialog.Builder(context)
            .setView(R.layout.dialog_create_new_todo)
            .setPositiveButton("Create") { dialog, _ ->
                val alertDialog = dialog as AlertDialog
                val alertText = alertDialog.findViewById<EditText>(R.id.dialogTextView).text.toString()
                val alertIsTodoSecret = alertDialog.findViewById<CheckBox>(R.id.dialogCheckbox).isChecked
                adapter.addData(Todo(string = alertText, secretToDo = alertIsTodoSecret))
                saveData()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun initVars(){

        database = Firebase.database("https://todo-b94ed-default-rtdb.firebaseio.com")
        database.getReference("adminId").get().addOnSuccessListener {
            adminAuthId = it.getValue(String::class.java).toString()
            val isShow = (currentAuthId == adminAuthId || currentAuthId == localData?.userId)
            val isAdmin =  currentAuthId == adminAuthId
            adapter = ToDoAdapter(listWasUpdated = { saveData() }, isShowSecretTodo = isShow, isAdmin = isAdmin)
            binding.rcView.adapter = adapter
            adapter.setData(localData?.listTodo)
        }
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("history",null)
        localData = Gson().fromJson(json, DatabaseData::class.java)



        currentAuthId = arguments?.getString("userId")!!
        targetShowingId = sharedPreferences.getString("uid", arguments?.getString("userId"))!!


    }

    private fun saveData(){
        checkForInternet()
        database.getReference("data").child(targetShowingId).setValue(adapter.getDatabaseData(getCurrentTime(), userId = targetShowingId)).addOnCompleteListener { checkForInternet() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPause() {
        sharedPreferences.edit().putString("history",adapter.toJson(currentAuthId)).apply()
        runCatching {
            if (localData?.dateLastEdit!! > dataInFirebase?.dateLastEdit!!)
                saveData()
        }
        super.onPause()
    }
    
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
                val clip = ClipData.newPlainText("xdd", targetShowingId)
                clipBoardManager.setPrimaryClip(ClipData(clip))
                makeToast("Ключ скопирован в буфер обмена")
            }
            R.id.import_id->{
                runCatching {
                    makeToast("Поиск ключа в буфере обмена..")
                    val clipData = clipBoardManager.primaryClip?.getItemAt(0)?.text.toString()
                    targetShowingId = clipData
                    sharedPreferences.edit().putString("uid",clipData).apply()
                    database.getReference("data").child(targetShowingId).get()
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
