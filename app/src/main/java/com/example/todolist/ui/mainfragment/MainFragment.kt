package com.example.todolist.ui.mainfragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.data.model.DatabaseData
import com.example.todolist.data.model.Todo
import com.example.todolist.databinding.FragmentMainBinding
import com.example.todolist.ui.TAG
import com.google.gson.Gson
import java.util.*


class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var targetShowingId:String
    private lateinit var currentAuthId:String
    private lateinit var popupMenu:PopupMenu
    private val vModel: MainViewModel by viewModels()
    private var localData: DatabaseData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        initVars()
        checkInternetAccess()
        initViewModelObservers()
        vModel.loadDataByUserId(targetShowingId)

        binding.buttonAddTodo.setOnClickListener {
            openDialogToCreateNewTodo()
        }
        binding.buttonAddTodo.setOnLongClickListener {
            runCatching { popupMenu.show() }
                .getOrElse { Toast.makeText(requireContext(),"не удалось загрузить список",Toast.LENGTH_LONG).show()}
            true
        }
        return binding.root
    }

    private fun initViewModelObservers() {
        vModel.adminIdLiveData.observe(viewLifecycleOwner){
            if (it==null){
                binding.imageNoEthernet.visibility = View.VISIBLE
                return@observe
            }
            binding.imageNoEthernet.visibility = View.GONE
            Log.wtf(TAG,"adminIdLiveData observe string \"$it\" | your id \"$currentAuthId\"")
            val isAdmin =  currentAuthId == it
            val isShow = ( isAdmin || currentAuthId == targetShowingId)
            Log.wtf(TAG,"isShowHidedTodo \"$isShow\" | isAdmin \"$isAdmin\"")
            adapter = ToDoAdapter(listWasUpdated = { saveData() },
                isShowSecretTodo = isShow,
                isAdmin = isAdmin
            )

            binding.rcView.adapter = adapter
            adapter.setData(localData?.listTodo)
        }
        vModel.dataInFirebaseLiveData.observe(viewLifecycleOwner){
            if (it==null){
                binding.imageNoEthernet.visibility = View.VISIBLE
                return@observe
            }
            binding.imageNoEthernet.visibility = View.GONE
            Log.wtf(TAG,"dataInFirebaseLiveData observe for id  \"${it.userId}\"")
            activity?.title = it.userId

            if (localData == it) return@observe
            if (localData?.dateLastEdit == null) return@observe adapter.setData(it.listTodo)
            if (it.dateLastEdit == null) return@observe saveData()

            if (localData?.dateLastEdit!! < it.dateLastEdit)
                adapter.setData(it.listTodo)
            else saveData()
        }
        vModel.listCurrentUsers.observe(viewLifecycleOwner){ it ->
            if (it.isEmpty()) return@observe
            popupMenu.menu.clear()
            it.forEach { popupMenu.menu.add(it) }
        }
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
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("history",null)
        localData = Gson().fromJson(json, DatabaseData::class.java)
        currentAuthId = arguments?.getString("userId")!!
        targetShowingId = sharedPreferences.getString("uid", currentAuthId)!!
        popupMenu = PopupMenu(context,binding.buttonAddTodo)
        popupMenu.setOnMenuItemClickListener {
            val tempStr = it.title.toString()
            targetShowingId = tempStr
            Log.wtf(TAG, "in popup menu was selected item \"$tempStr\"")
            vModel.loadDataByUserId(tempStr)
            true
        }
    }

    private fun saveData(){
        val data = adapter.getDatabaseData(getCurrentTime(), userId = targetShowingId)
        vModel.saveData(targetShowingId,data)
    }

    override fun onPause() {
        sharedPreferences.edit().putString("history",adapter.toJson(currentAuthId)).apply()
        super.onPause()
    }
    
    private fun checkInternetAccess() {
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
}
