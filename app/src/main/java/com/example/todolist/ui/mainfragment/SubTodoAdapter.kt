package com.example.todolist.ui.mainfragment

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.model.SubTodo
import com.example.todolist.databinding.SubTodoBinding
import com.example.todolist.ui.TAG

typealias SubTodoWasUpdated = (list:MutableList<SubTodo>) -> Unit

class SubTodoAdapter(val subTodoWasUpdated:SubTodoWasUpdated) : RecyclerView.Adapter<SubTodoAdapter.SubTodoViewHolder>() {
    class SubTodoViewHolder(val binding:SubTodoBinding):RecyclerView.ViewHolder(binding.root)

    private var list = mutableListOf<SubTodo>()

    fun setData(subTodo: List<SubTodo>?){
        list = subTodo?.toMutableList() ?: return
        notifyDataSetChanged()
    }
    fun addData(item:SubTodo){
        list.add(item)
        Log.i(TAG, "item added $item")
        notifyItemInserted(list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTodoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SubTodoBinding.inflate(inflater,parent,false)
        return SubTodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubTodoViewHolder, position: Int) {
        val item = list[position]
        holder.binding.checkBox.apply {
            isChecked = item.isCompleted
            setOnCheckedChangeListener { _, isChecked ->
                Log.i(TAG,"checkbox set as $isChecked on position $position")
                if (isChecked) holder.binding.textView.setTextColor(Color.GREEN)
                else holder.binding.textView.setTextColor(Color.RED)
                item.isCompleted = isChecked
                subTodoWasUpdated(list)
            }
        }
        holder.binding.textView.apply {
            text = item.string
            setTextColor(if (item.isCompleted) Color.GREEN else Color.RED)
        }
        holder.binding.imageButton.setOnClickListener {
            Log.i(TAG,"removed item SubToDo at position $position")
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,list.size)
            subTodoWasUpdated(list)
        }

    }

    override fun getItemCount(): Int =list.size
}
