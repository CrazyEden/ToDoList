package com.example.todolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemTodoBinding
import com.google.gson.Gson

typealias ListWasUpdated = (list:MutableList<Todo>) -> Unit;

class ToDoAdapter(private val listWasUpdated: ListWasUpdated):RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding:ItemTodoBinding):RecyclerView.ViewHolder(binding.root)

    private var list = mutableListOf<Todo>()

    fun setData(list:List<Todo>?){
        this.list = list?.toMutableList() ?: return
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

//        val item1= list[holder.adapterPosition]
//        val item2= list[position]
        val item= list[holder.adapterPosition]


        holder.binding.textView.text = item.string

        holder.binding.checkBox.isChecked = item.isCompleted
        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            listWasUpdated(list)
            item.isCompleted = isChecked
        }
        holder.binding.imageButton.setOnClickListener {

            list.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            notifyItemRangeChanged(holder.adapterPosition,list.size)
            listWasUpdated(list)
        }

    }
    override fun getItemCount(): Int = list.size


    fun toJson(): String = Gson().toJson(DatabaseData(MainFragment.getCurrentTime(),list))

    fun getDatabaseData(date:Long) = DatabaseData(date,list)
    fun getRawList() = list

}


