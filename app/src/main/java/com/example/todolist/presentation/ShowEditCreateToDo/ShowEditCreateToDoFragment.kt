package com.example.todolist.presentation.ShowEditCreateToDo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.data.model.SubTodo
import com.example.todolist.data.model.Todo
import com.example.todolist.databinding.FragmentShowEditCreateToDoBinding
import com.example.todolist.presentation.activity.TAG
import com.example.todolist.presentation.mainfragment.MainFragment
import com.example.todolist.presentation.mainfragment.SubTodoAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ShowEditCreateToDoFragment : Fragment() {
    private val vModel: ShowEditCreateViewModel by viewModels()
    private lateinit var todo: Todo
    private lateinit var id: String
    private lateinit var popupMenu:PopupMenu
    private var position: Int? = null
    private lateinit var binding:FragmentShowEditCreateToDoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            todo = it.getParcelable(TODO_KEY) as? Todo ?:Todo()
            id = it.getString(ID_KEY) ?: throw NullPointerException("fragment require the id")
            position = it.getInt(TODO_POSITION_KEY,-1)
        }
        if (position == -1) position = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShowEditCreateToDoBinding.inflate(inflater,container,false)
        popupMenu = PopupMenu(context,binding.titleTodo)
        updatePopupMenu()
        val adapter = SubTodoAdapter{ it ->
            todo.isCompleted = it.all { it.isCompleted }
            todo.subTodo = it
        }
        binding.rcViewSubTodo.adapter = adapter
        adapter.setData(todo.subTodo)
        binding.titleTodo.setText(todo.titleToDo)
        if (todo.deadlineString.isNotEmpty()) {
            binding.deadline.text = todo.deadlineString
            updateDeadlineColor()
        }
        if (todo.secretToDo)
            binding.iconSecretTodo.visibility = View.VISIBLE
        if (position == null)
            binding.floatButton.setImageResource(R.drawable.ic_create)


        binding.buttonAddSubTodo.setOnClickListener {
            val text = binding.textSubTodo.text.toString()
            if (text.isEmpty()) return@setOnClickListener
            adapter.addData(SubTodo(text))
            todo.subTodo.add(SubTodo(text))
            binding.textSubTodo.text.clear()
            binding.textSubTodo.clearFocus()
        }

        binding.titleTodo.addTextChangedListener {
            todo.titleToDo = it.toString()
        }
        binding.deadline.setOnClickListener {
            openDialogToPickDatetime()
        }



        popupMenu.setOnMenuItemClickListener {
            todo.secretToDo = !todo.secretToDo
            updateSecretIcon()
            updatePopupMenu()
            true
        }
        binding.root.setOnLongClickListener {
            popupMenu.show()
            true
        }

        binding.floatButton.setOnClickListener {
            if (todo.titleToDo.isEmpty())
                return@setOnClickListener showToast("Название не может быть пустым")
            if (todo.deadlineString.isEmpty())
                return@setOnClickListener showToast("Установите дедлайн")

            if (position != null)
                vModel.updateTodo(todo,id,position!!)
            else
                vModel.createNewTodo(todo, id)
        }
        vModel.updateLiveData.observe(viewLifecycleOwner){
            if (it==null)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container,MainFragment())
                    .commit()
            else
                Log.wtf(TAG, "updateLiveData: ", it)
        }
        return binding.root
    }

    private fun updatePopupMenu() {
        popupMenu.menu.clear()
        if (todo.secretToDo) {
            binding.iconSecretTodo.visibility = View.VISIBLE
            popupMenu.menu.add("Сделать задачу общедоступной")
        }
        else {
            binding.iconSecretTodo.visibility = View.GONE
            popupMenu.menu.add("Сделать задачу секретной")
        }
    }
    private fun updateSecretIcon(){
        binding.iconSecretTodo.visibility =
            if (todo.secretToDo) View.VISIBLE else View.GONE
    }


    private fun openDialogToPickDatetime(){
        DatePickerDialog(requireContext(),{ _, year, monthOfYear, dayOfMonth ->
            val day = "$dayOfMonth.${monthOfYear + 1}.$year" // "day.month.year | 31.12.2022"
            openTimePickDialog(day)
        }, 2022, 11, 1).show()
    }
    private fun openTimePickDialog(day:String){
        TimePickerDialog(context,{_,hour,minutes->
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
            val dateStr = "$day $hour:$minutes" // 31.12.2022 23:59
            val timeAsLong = format.parse(dateStr)?.time!! //"31.12.2022 23:59" converting to Long "1672520340000"
            Log.wtf(TAG,"picked datetime \"$dateStr\" | Long \"$timeAsLong\"")
            binding.deadline.text = dateStr   // text will using for ToDо.deadlineString
            todo.deadlineString = dateStr
            todo.deadlineLong = timeAsLong
            updateDeadlineColor()
        },0,0,true).show()
    }

    private fun updateDeadlineColor() {
        val timeLeftBeforeDeadline = todo.deadlineLong - MainFragment.getCurrentTime()
        binding.deadline.setTextColor(when{
            timeLeftBeforeDeadline < 1 -> Color.BLACK                                //deadline was left
            timeLeftBeforeDeadline > 7889229000 -> Color.GREEN                       //3 month+
            timeLeftBeforeDeadline > 2629743000 -> Color.CYAN                        //month+
            timeLeftBeforeDeadline > 604800000 -> Color.YELLOW                       //week+
            timeLeftBeforeDeadline > 86400000 -> Color.parseColor("#FFA500")// > day && < week
            timeLeftBeforeDeadline < 86400000 -> Color.RED                           //day
            else -> Log.wtf(TAG, "unknown time $timeLeftBeforeDeadline")
        })
    }

    private fun showToast(text:String){
        Toast.makeText(context,text,Toast.LENGTH_LONG).show()
    }

    companion object{
        const val TODO_KEY = "TODO"
        const val TODO_POSITION_KEY = "TODO_POSITION"
        const val ID_KEY = "ID"
    }
}