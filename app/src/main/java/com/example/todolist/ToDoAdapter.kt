package com.example.todolist

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemTodoBinding
import com.google.gson.Gson

typealias ListWasUpdated = (list:MutableList<Todo>) -> Unit

class ToDoAdapter(private val listWasUpdated: ListWasUpdated,
                  private var isShowSecretTodo:Boolean,
                  private val isAdmin:Boolean):RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding:ItemTodoBinding):RecyclerView.ViewHolder(binding.root)

    private var list = mutableListOf<Todo>()

    fun setData(list:List<Todo>?, isShowSecretTodo:Boolean?=null){
        this.list = list?.toMutableList() ?: return
        if (isShowSecretTodo != null) {
            this.isShowSecretTodo = isShowSecretTodo
        }
        notifyDataSetChanged()
    }

    fun addData(item:Todo){
        list.add(item)
        notifyItemInserted(list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTodoBinding.inflate(inflater,parent,false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {

        val item= list[position]

        if (!isShowSecretTodo && item.secretToDo){
            holder.binding.root.visibility = View.GONE
            holder.binding.comment.visibility = View.GONE
            holder.binding.checkBox.visibility = View.GONE
            holder.binding.textView.visibility = View.GONE
            holder.binding.imageButton.visibility = View.GONE
            return
        }

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

        holder.binding.textView.text = item.string

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

        holder.binding.checkBox.apply {
            isChecked = item.isCompleted
            setOnCheckedChangeListener { _, isChecked ->
                item.isCompleted = isChecked
                listWasUpdated(list)
            }
        }

        holder.binding.imageButton.setOnClickListener {
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,list.size)
            listWasUpdated(list)
        }


    }
    override fun getItemCount(): Int = list.size


    fun toJson(userId:String): String = Gson().toJson(DatabaseData(MainFragment.getCurrentTime(),list,userId))

    fun getDatabaseData(date:Long, userId:String) = DatabaseData(date,list,userId)
    fun getRawList() = list

}


