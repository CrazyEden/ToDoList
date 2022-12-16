package com.example.todolist.ui.mainfragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.todolist.R
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.databinding.DialogCreateNewTodoBinding
import com.example.todolist.databinding.FragmentMainBinding
import com.example.todolist.ui.SettingsFragment
import com.example.todolist.ui.activity.TAG
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var targetShowingId:String
    private lateinit var currentAuthId:String
    private lateinit var popupMenu:PopupMenu
    private val vModel: MainViewModel by viewModels()
    private var localData: UserData? = null
    private var isCurrentUserAdmin = false
    private var isCurrentUserAtHerselfPageOrAdmin = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (savedInstanceState !=null) return binding.root
        binding = FragmentMainBinding.inflate(inflater, container, false)
        initVars()
        checkInternetAccess()
        initViewModelObservers()
        inflateToolbarMenu()
        vModel.loadAdminId(targetShowingId)
        binding.rcView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY == 0) return@setOnScrollChangeListener binding.buttonAddTodo.show()
            if (scrollY > oldScrollY) binding.buttonAddTodo.hide()
            else binding.buttonAddTodo.show()
        }
        binding.buttonAddTodo.setOnClickListener {
            openDialogToCreateNewTodo()
        }
        return binding.root
    }

    private fun inflateToolbarMenu() {
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
                menuInflater.inflate(R.menu.main_menu, menu)

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.showListUsers -> {
                        runCatching { popupMenu.show() }
                            .getOrElse {
                                Log.e(TAG,"popupmenu isn't inflated")
                                Toast.makeText(requireContext(),getString(R.string.error_load_user_list),Toast.LENGTH_LONG).show()
                            }
                        true
                    }
                    R.id.settings -> {
                        parentFragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.container,SettingsFragment())
                            .commit()
                        true
                    }

                    else -> false
                }
            }
        }
        requireActivity().addMenuProvider(menuProvider,viewLifecycleOwner,Lifecycle.State.RESUMED)
    }

    private fun initViewModelObservers() {
        vModel.adminIdLiveData.observe(viewLifecycleOwner){
            if (it==null) return@observe checkInternetAccess()

            binding.imageNoEthernet.visibility = View.GONE
            isCurrentUserAdmin =  currentAuthId == it
            isCurrentUserAtHerselfPageOrAdmin = (isCurrentUserAdmin || currentAuthId == targetShowingId)
            Log.i(TAG,"adminIdLiveData observe string \"$it\" | your id \"$currentAuthId\" \n" +
                    "isShowHidedTodo \"$isCurrentUserAtHerselfPageOrAdmin\" | isAdmin \"$isCurrentUserAdmin\"")
            adapter = ToDoAdapter(isCurrentUserAtHerselfPageOrAdmin){ uploadDataToFirebase() }

            binding.rcView.adapter = adapter
            activity?.title = localData?.userId
            adapter.setData(localData?.listTodo)
        }
        vModel.dataInFirebaseLiveData.observe(viewLifecycleOwner){
            if (it==null) return@observe checkInternetAccess()

            binding.imageNoEthernet.visibility = View.GONE
            Log.i(TAG,"dataInFirebaseLiveData observe for id  \"${it.userId}\"")
            activity?.title = it.userId
            if(localData==null)
                return@observe adapter.setData(it.listTodo)
            if (localData == it)
                return@observe
            if (adapter.getRawList() != it.listTodo)
                adapter.setData(it.listTodo)
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
            openDialogPickDatetimeForDialogCreateNewTodo(dialogBinding)
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

                Log.i(TAG,"created new todo, String = \"$alertText\" | isToDoSecret = \"$alertIsTodoSecret\" | deadline todo = \"$timeStr\"")
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
    private fun openDialogPickDatetimeForDialogCreateNewTodo(dialogBinding:DialogCreateNewTodoBinding){
        DatePickerDialog(requireContext(),{ _, year, monthOfYear, dayOfMonth ->
            val day = "$dayOfMonth.${monthOfYear + 1}.$year" // "day.month.year | 31.12.2022"
            openTimePickDialog(dialogBinding,day)
        }, 2022, 11, 1).show()
    }
    private fun openTimePickDialog(dialogBinding:DialogCreateNewTodoBinding,day:String){
        TimePickerDialog(context,{_,hour,minutes->
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
            val dateStr = "$day $hour:$minutes" // 31.12.2022 23:59
            val timeAsLong = format.parse(dateStr)?.time!! //"31.12.2022 23:59" converting to Long "1672520340000"
            Log.wtf(TAG,"picked datetime \"$dateStr\" | Long \"$timeAsLong\"")
            dialogBinding.textViewDatetime.text = dateStr // text will using for ToDо.deadlineString
            dialogBinding.textViewDatetime.tag = timeAsLong // tag will using for ToDо.deadlineLong
        },0,0,true).show()
    }

    private fun initVars(){

        localData = vModel.getUserData()
        currentAuthId = arguments?.getString("userId")!!
        targetShowingId = currentAuthId
        popupMenu = PopupMenu(context,binding.buttonAddTodo)
        popupMenu.setOnMenuItemClickListener {
            val tempStr = it.title.toString()
            targetShowingId = tempStr
            Log.i(TAG, "in popup menu was selected item \"$tempStr\"")
            vModel.loadTodo(tempStr)
            true
        }
    }

    private fun uploadDataToFirebase(){
        checkInternetAccess()
        val data = adapter.getDatabaseData(getCurrentTime(), userId = targetShowingId)
        vModel.saveData(targetShowingId,data)
    }

    override fun onPause() {
        vModel.saveUserData(adapter.toJson(currentAuthId))
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
