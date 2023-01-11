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


class ToDoAdapter(private val toDoArgs: ToDoArgs):RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding:ItemTodoBinding):RecyclerView.ViewHolder(binding.root)

    private var listToDo = mutableListOf<Todo>()
    @SuppressLint("NotifyDataSetChanged")
    fun setData(list:List<Todo>?){
        if (list == null) return
        listToDo = list.sortedBy { it.deadlineLong }.toMutableList()
        notifyDataSetChanged()
    }

    fun getItem(position: Int) =
        listToDo[position]

    fun addItem(position: Int,todo:Todo){
        listToDo.add(position,todo)
        notifyItemInserted(position)
        toDoArgs.listWasUpdated(listToDo)
    }

    fun removeItem(position: Int){
        listToDo.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listToDo.size)
        toDoArgs.listWasUpdated(listToDo)
    }

    private var isShowSecretTodo:Boolean = true
    fun setPermission(isShowSecretTodo:Boolean) {
        this.isShowSecretTodo = isShowSecretTodo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTodoBinding.inflate(inflater,parent,false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val item= listToDo[position]
        val param = holder.binding.root.layoutParams
        if (!isShowSecretTodo && item.secretToDo){ //hide item if no access
            holder.binding.root.visibility = View.GONE
            holder.binding.root.layoutParams = LayoutParams(0,0)
            Log.i(TAG,"item was hide & wasn't inflated, position \"$position\"")
            return
        }else{
            holder.binding.root.visibility = View.VISIBLE
            holder.binding.root.layoutParams = param
            Log.i(TAG,"item was inflated, position \"$position\"")
        }

        holder.binding.iconSecretTodo.visibility =
            if(item.secretToDo) View.VISIBLE else View.GONE

        holder.binding.iconTodoCompleted.visibility =
            if (item.isCompleted) View.VISIBLE else View.GONE

        holder.binding.rcViewSubTodo.adapter = SubTodoAdapter(item.subTodo){subTodoList->
            listToDo[position].isCompleted = subTodoList.all { it.isCompleted }.also {
                if (it)
                    holder.binding.iconTodoCompleted.visibility = View.VISIBLE
                else
                    holder.binding.iconTodoCompleted.visibility = View.GONE
            }
            listToDo[position].subTodo = subTodoList
            toDoArgs.itemWasUpdated(listToDo[position],position)
        }
        holder.binding.commentsListView.adapter = CommentsAdapter(item.comments)

        holder.binding.titleTodo.apply {
            text = item.titleToDo
            setOnClickListener { toDoArgs.openToDoItem(item,position) }
        }

        val timeLeftBeforeDeadline = item.deadlineLong - ToDoListFragment.getCurrentTime()
        holder.binding.deadline.apply {
            text = item.deadlineString
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


