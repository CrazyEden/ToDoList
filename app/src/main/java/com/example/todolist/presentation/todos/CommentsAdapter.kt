package com.example.todolist.presentation.todos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemCommentBinding

class CommentsAdapter(private val comments:MutableList<String>,private val edit:Boolean = false)
    :RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    class CommentsViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)
    fun addComment(comment:String){
        comments.add(comment)
        notifyItemInserted(comments.size)
    }
    private fun removeComment(position:Int){
        comments.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position,comments.size)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(inflater, parent, false)
        return CommentsViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.binding.textViewComment.text = comments[position]
        if (edit)
            holder.binding.iconRemoveComment.visibility = View.VISIBLE
        holder.binding.iconRemoveComment.setOnClickListener{
            removeComment(position)
        }
    }

    override fun getItemCount() = comments.size
}