package com.example.todolist.ui.mainfragment

import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.model.Data
import com.example.todolist.data.model.SubTodo
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.databinding.ItemTodoBinding
import com.example.todolist.ui.activity.TAG
import com.google.gson.Gson

typealias ListWasUpdated = (list:MutableList<Todo>) -> Unit

class ToDoAdapter(private val listWasUpdated: ListWasUpdated):RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding:ItemTodoBinding):RecyclerView.ViewHolder(binding.root)

    private var listToDo = mutableListOf<Todo>()
    fun setData(list:List<Todo>?){
        if (list == null) return
        listToDo = list.sortedBy { it.deadlineLong }.toMutableList()
        notifyDataSetChanged()
    }

    fun addData(item: Todo){
        listToDo.add(0,item)
        notifyItemInserted(0)
    }

    private var isShowSecretTodo:Boolean = false
    fun setPermission(isShowSecretTodo:Boolean) {
        this.isShowSecretTodo = isShowSecretTodo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTodoBinding.inflate(inflater,parent,false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        Log.i(TAG,"starting inflate item, position \"$position\"")
        val item= listToDo[position]

        if (!isShowSecretTodo && item.secretToDo){ //hide item if no access
            holder.binding.root.visibility = View.GONE
            holder.binding.root.layoutParams = LayoutParams(0,0)
            Log.i(TAG,"item was hide & wasn't inflated, position \"$position\"")
            return
        }

        if(item.secretToDo) holder.binding.iconSecretTodo.visibility = View.VISIBLE

        val subTodoAdapter = SubTodoAdapter{
            listToDo[position].subTodo = it
            Log.i(TAG, "list was updated. cause:subTodo")
            listWasUpdated(listToDo)
        }
        holder.binding.rcViewSubTodo.adapter = subTodoAdapter
        subTodoAdapter.setData(item.subTodo)

        holder.binding.buttonAddSubTodo.setOnClickListener {
            val subStr = holder.binding.textSubTodo.text.toString()
            if (subStr.isEmpty()) return@setOnClickListener
            holder.binding.textSubTodo.apply {
                text.clear()
                onEditorAction(EditorInfo.IME_ACTION_DONE)
                clearFocus()
            }
            item.subTodo?.add(SubTodo(string = subStr))
            listWasUpdated(listToDo)
            subTodoAdapter.addData(SubTodo(string = subStr))
        }

        holder.binding.titleTodo.text = item.titleToDo

        if (!isShowSecretTodo)holder.binding.titleTodo.visibility = View.GONE

        holder.binding.titleTodo.setOnLongClickListener { view ->
            val popupMenu = PopupMenu(holder.itemView.context,view)
            popupMenu.menu.add(holder.itemView.context.getString(R.string.delete))
            popupMenu.setOnMenuItemClickListener {
                if (it.itemId == 0) {
                    Log.i(TAG, "removed item ToDo at position $position")
                    listToDo.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, listToDo.size)
                    listWasUpdated(listToDo)
                }
                true
            }
            popupMenu.show()
            true
        }

        holder.binding.notes.apply {
            if (isShowSecretTodo) visibility = View.VISIBLE
            else return@apply
            setText(item.notes)
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    view.clearFocus()
                    val str = view.text.toString()
                    if (str == listToDo[position].notes) return@setOnEditorActionListener true
                    listToDo[position].notes = str
                    Log.i(TAG, "list was updated. cause : edit note")
                    listWasUpdated(listToDo)
                    return@setOnEditorActionListener false
                }
                false
            }
        }
        holder.binding.deadline.apply {
            text = item.deadlineString
            val timeLeftBeforeDeadline = item.deadlineLong - MainFragment.getCurrentTime()
            setTextColor(when{
                timeLeftBeforeDeadline < 1 -> Color.BLACK                                //deadline was left
                timeLeftBeforeDeadline > 7889229000 -> Color.GREEN                       //3 month+
                timeLeftBeforeDeadline > 2629743000 -> Color.CYAN                        //month+
                timeLeftBeforeDeadline > 604800000 -> Color.YELLOW                       //week+
                timeLeftBeforeDeadline > 86400000 -> Color.parseColor("#FFA500")// > day && < week
                timeLeftBeforeDeadline < 86400000 -> Color.RED                           //day
                else -> Log.wtf(TAG, "unknown time $timeLeftBeforeDeadline")
            })
        }


    }
    override fun getItemCount(): Int = listToDo.size

    fun toJson(userId:String): String = Gson().toJson(Data(listToDo, UserData(userId)))

    fun getRawList() = listToDo
}


