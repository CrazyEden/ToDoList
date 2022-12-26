package com.example.todolist.presentation.todos

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.model.Todo
import com.example.todolist.databinding.ItemTodoBinding
import com.example.todolist.presentation.activity.TAG

typealias ListWasUpdated = (todo:Todo,position:Int) -> Unit

class ToDoAdapter(private val listWasUpdated:ListWasUpdated):RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding:ItemTodoBinding):RecyclerView.ViewHolder(binding.root)

    private var listToDo = mutableListOf<Todo>()
    @SuppressLint("NotifyDataSetChanged")
    fun setData(list:List<Todo>?){
        if (list == null) return
        listToDo = list.sortedBy { it.deadlineLong }.toMutableList()
        notifyDataSetChanged()
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


        holder.binding.titleTodo.apply {
            text = item.titleToDo
            setOnClickListener { listWasUpdated(item,position) }
        }


        holder.binding.deadline.apply {
            text = item.deadlineString
            val timeLeftBeforeDeadline = item.deadlineLong - ToDoListFragment.getCurrentTime()
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

    fun getRawList() = listToDo
}


