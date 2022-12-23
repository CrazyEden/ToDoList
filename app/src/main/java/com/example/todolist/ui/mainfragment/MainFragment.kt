package com.example.todolist.ui.mainfragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.todolist.R
import com.example.todolist.data.model.Todo
import com.example.todolist.databinding.DialogCreateNewTodoBinding
import com.example.todolist.databinding.FragmentMainBinding
import com.example.todolist.ui.activity.TAG
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var targetShowingId:String
    private lateinit var targetShowingNick:String
    private lateinit var popupMenu:PopupMenu
    private val vModel: MainViewModel by viewModels()
    private var isCurrentUserAdmin = false
    private var isCurrentUserAtHerselfPageOrAdmin = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        initVars()
        inflateToolbarMenu()
        initViewModelObservers()
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
                if (menuItem.itemId == R.id.showListUsers){
                    popupMenu.show()
                    return true
                }
                return false
            }
        }
        requireActivity().addMenuProvider(menuProvider,viewLifecycleOwner,Lifecycle.State.RESUMED)
    }

    private fun initViewModelObservers() {

        vModel.adminIdLiveData.observe(viewLifecycleOwner){
            if (it==null) return@observe

            isCurrentUserAdmin =  vModel.authId == it
            Log.i(TAG,"adminIdLiveData observe string \"$it\" | your id \"${vModel.authId}\" \n" +
                    "isShowHidedTodo \"$isCurrentUserAtHerselfPageOrAdmin\" | isAdmin \"$isCurrentUserAdmin\"")
        }
        vModel.dataInFirebaseLiveData.observe(viewLifecycleOwner){
            val list = it?.listTodo
            isCurrentUserAtHerselfPageOrAdmin = (isCurrentUserAdmin || (vModel.authId == targetShowingId))
            adapter.setPermission(isCurrentUserAtHerselfPageOrAdmin)

            if (adapter.getRawList() != list)
                adapter.setData(list)
        }
        vModel.listCurrentUsers.observe(viewLifecycleOwner){ it ->
            if (it.isEmpty()) return@observe
            popupMenu.menu.clear()
            mapOfNicknamesAndIds.clear()
            it.forEach {
                if (it?.nickname == null) it?.nickname = it?.userId
                if(it?.userId==null) return@observe
                mapOfNicknamesAndIds[it.nickname!!] = it.userId!!
                popupMenu.menu.add(it.nickname)
            }
        }
        vModel.userDataLiveData.observe(viewLifecycleOwner){
            targetShowingNick = it?.nickname.toString()
            activity?.title = it?.nickname
        }
        vModel.coldLoad(targetShowingId)
    }
    private var mapOfNicknamesAndIds:MutableMap<String,String> = mutableMapOf()
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
            .setNegativeButton(getString(R.string.cancel)){ _, _ -> }
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
            dialogBinding.textViewDatetime.text = dateStr   // text will using for ToDо.deadlineString
            dialogBinding.textViewDatetime.tag = timeAsLong // tag will using for ToDо.deadlineLong
        },0,0,true).show()
    }



    private fun initVars(){
        adapter = ToDoAdapter(){ uploadDataToFirebase() }
        binding.rcView.adapter = adapter
        targetShowingId = vModel.authId

        popupMenu = PopupMenu(context,binding.buttonAddTodo)
        popupMenu.setOnMenuItemClickListener {
            val tempStr = it.title.toString()
            val key = mapOfNicknamesAndIds[tempStr]!!
            targetShowingId = key
            targetShowingNick = tempStr
            activity?.title = tempStr
            Log.i(TAG, "in popup menu was selected item \"$key\"")
            vModel.createToDoObserver(key)
            true
        }
    }

    private fun uploadDataToFirebase(){
        vModel.saveData(targetShowingId,adapter.getRawList())
    }

    companion object{
        fun getCurrentTime() = Calendar.getInstance().time.time
    }
}
