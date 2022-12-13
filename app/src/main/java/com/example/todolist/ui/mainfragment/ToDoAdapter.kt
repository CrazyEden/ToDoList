package com.example.todolist.ui.mainfragment

import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.model.SubTodo
import com.example.todolist.data.model.Todo
import com.example.todolist.data.model.UserData
import com.example.todolist.databinding.ItemTodoBinding
import com.example.todolist.ui.TAG
import com.google.gson.Gson

typealias ListWasUpdated = (list:MutableList<Todo>) -> Unit

class ToDoAdapter(private val listWasUpdated: ListWasUpdated,
                  private var isShowSecretTodo:Boolean = false,
                  private var isAdmin:Boolean = false):RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding:ItemTodoBinding):RecyclerView.ViewHolder(binding.root)

    private var list = mutableListOf<Todo>()
    init { Log.i(TAG,"ToDoAdapter is created") }
    fun setData(list:List<Todo>?){
        this.list = list?.toMutableList() ?: return
        notifyDataSetChanged()
    }

    fun addData(item: Todo){
        list.add(item)
        notifyItemInserted(list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTodoBinding.inflate(inflater,parent,false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        Log.i(TAG,"starting inflate item, position \"$position\"")
        val item= list[position]

        if (!isShowSecretTodo && item.secretToDo){ //hide item if no access
            holder.binding.root.visibility = View.GONE
            holder.binding.root.layoutParams = LayoutParams(0,0)
            Log.i(TAG,"item was hide & wasn't inflated, position \"$position\"")
            return
        }

        val subTodoAdapter = SubTodoAdapter{
            list[position].subTodo = it
            listWasUpdated(list)
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
            listWasUpdated(list)
            subTodoAdapter.addData(SubTodo(string = subStr))
        }


        holder.binding.titleTodo.text = item.string
        if (!isAdmin) holder.binding.secretTodoLayout.visibility = View.GONE
        else holder.binding.checkBoxIsTodoSecret.apply {
            isChecked = item.secretToDo
            setOnCheckedChangeListener { _, isChecked ->
                item.secretToDo = isChecked
                listWasUpdated(list)
            }
        }

        holder.binding.root.setOnClickListener {
            if (holder.binding.commentLayout.isVisible)
                holder.binding.commentLayout.visibility = View.GONE
            else holder.binding.commentLayout.visibility = View.VISIBLE
        }



        holder.binding.comment.apply {
            setText(item.comment)
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    view.clearFocus()
                    val str = view.text.toString()
                    if (str == list[position].comment) return@setOnEditorActionListener true
                    list[position].comment = str
                    listWasUpdated(list)
                    return@setOnEditorActionListener false
                }
                false
            }
        }
        holder.binding.duration.apply {
            text = item.duration
            when(item.duration){
                "День"-> Color.RED
                "Неделя"-> Color.YELLOW
                "Месяц"-> Color.CYAN
                "Квартал"-> Color.GREEN
                else-> null
            }?.let { setTextColor(it) }
        }


    }
    override fun getItemCount(): Int = list.size

    fun toJson(userId:String): String = Gson().toJson(UserData(MainFragment.getCurrentTime(),list,userId))

    fun getDatabaseData(date:Long, userId:String) =
        UserData(dateLastEdit =date,
            listTodo =list,
            userId =userId
        )

}


