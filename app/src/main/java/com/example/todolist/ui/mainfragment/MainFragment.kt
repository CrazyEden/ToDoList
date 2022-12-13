package com.example.todolist.ui.mainfragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.databinding.DialogCreateNewTodoBinding
import com.example.todolist.databinding.FragmentMainBinding
import com.example.todolist.ui.TAG
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var targetShowingId:String
    private lateinit var currentAuthId:String
    private lateinit var popupMenu:PopupMenu
    private val vModel: MainViewModel by viewModels()
    private var localData: UserData? = null
    private var isCurrentUserAdmin = false
    private var isCurrentUserAtHerselfPageOrAdmin = false

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
            if (it==null) return@observe showEthernetErrorIcon()

            binding.imageNoEthernet.visibility = View.GONE
            isCurrentUserAdmin =  currentAuthId == it
            isCurrentUserAtHerselfPageOrAdmin = (isCurrentUserAdmin || currentAuthId == targetShowingId)
            Log.i(TAG,"adminIdLiveData observe string \"$it\" | your id \"$currentAuthId\" \n" +
                    "isShowHidedTodo \"$isCurrentUserAtHerselfPageOrAdmin\" | isAdmin \"$isCurrentUserAdmin\"")
            adapter = ToDoAdapter(listWasUpdated = { uploadDataToFirebase() },
                isShowSecretTodo = isCurrentUserAtHerselfPageOrAdmin,
                isAdmin = isCurrentUserAdmin
            )

            binding.rcView.adapter = adapter
            adapter.setData(localData?.listTodo)
        }
        vModel.dataInFirebaseLiveData.observe(viewLifecycleOwner){
            if (it==null) return@observe showEthernetErrorIcon()

            binding.imageNoEthernet.visibility = View.GONE
            Log.i(TAG,"dataInFirebaseLiveData observe for id  \"${it.userId}\"")
            if(localData==null) return@observe adapter.setData(it.listTodo)
            if (localData == it) return@observe
            activity?.title = it.userId

            if (localData?.dateLastEdit!! < it.dateLastEdit!!)
                adapter.setData(it.listTodo)
            else uploadDataToFirebase()
        }
        vModel.listCurrentUsers.observe(viewLifecycleOwner){ it ->
            if (it.isEmpty()) return@observe
            popupMenu.menu.clear()
            it.forEach { popupMenu.menu.add(it) }
        }
    }

    private fun openDialogToCreateNewTodo() {
        val dialogBinding = DialogCreateNewTodoBinding.inflate(layoutInflater)
        if (isCurrentUserAdmin) dialogBinding.dialogCheckbox.visibility = View.VISIBLE
        dialogBinding.buttonPickData.setOnClickListener {
            DatePickerDialog(requireContext(),{ _, year, monthOfYear, dayOfMonth ->
                val day = "$dayOfMonth.${monthOfYear + 1}.$year"
                TimePickerDialog(context,{_,h,minutes->

                    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
                    val time = format.parse("$day $h:$minutes")?.time!!
                    Log.wtf(TAG,"picked datetime \"$time\"")
                    val dateStr = format.format(Date(time))
                    dialogBinding.textViewDatetime.text = dateStr
                    dialogBinding.textViewDatetime.tag = time
                },0,0,true).show()
            }, 2022, 11, 1).show()
        }
        AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.create) { _, _ ->
                val alertText = dialogBinding.dialogTextView.text.toString()
                if (alertText.isEmpty()) return@setPositiveButton
                val timeStr = dialogBinding.textViewDatetime.text.toString()
                if (timeStr.isEmpty()) return@setPositiveButton
                val alertIsTodoSecret = dialogBinding.dialogCheckbox.isChecked
                val time = dialogBinding.textViewDatetime.tag as Long

                Log.i(TAG,"created new todo, String = \"$alertText\" | isToDoSecret = \"$alertIsTodoSecret\" | duration todo = \"$\"")
                adapter.addData(Todo(
                    titleToDo = alertText,
                    secretToDo = alertIsTodoSecret,
                    deadlineLong = time,
                    deadlineString = timeStr
                ))
                uploadDataToFirebase()
            }
            .setNegativeButton(getString(R.string.cancel)){ dialog, _ ->
                dialog.dismiss()
            }
            .setTitle(R.string.create_new_todo)
            .show()
    }

    private fun initVars(){
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("history",null)
        localData = Gson().fromJson(json, UserData::class.java)
        currentAuthId = arguments?.getString("userId")!!
        targetShowingId = sharedPreferences.getString("uid", currentAuthId)!!
        popupMenu = PopupMenu(context,binding.buttonAddTodo)
        popupMenu.setOnMenuItemClickListener {
            val tempStr = it.title.toString()
            targetShowingId = tempStr
            Log.i(TAG, "in popup menu was selected item \"$tempStr\"")
            vModel.loadDataByUserId(tempStr)
            true
        }
    }

    private fun uploadDataToFirebase(){
        val data = adapter.getDatabaseData(getCurrentTime(), userId = targetShowingId)
        vModel.saveData(targetShowingId,data)
    }

    override fun onPause() {
        sharedPreferences.edit().putString("history",adapter.toJson(currentAuthId)).apply()
        super.onPause()
    }
    
    private fun checkInternetAccess() {
        // register activity with the connectivity manager service
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return showEthernetErrorIcon()
        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?:return showEthernetErrorIcon()
        if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            binding.imageNoEthernet.visibility = View.GONE
        else showEthernetErrorIcon()
    }
    private fun showEthernetErrorIcon(){ binding.imageNoEthernet.visibility = View.VISIBLE }

    companion object{
        fun getCurrentTime() = Calendar.getInstance().time.time
    }
}
