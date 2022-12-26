package com.example.todolist.presentation.notes

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.model.Note
import com.example.todolist.databinding.ItemNoteBinding

interface NoteAdapterInt{
    fun listNotesWasUpdated(notesList:MutableList<Note>)
    fun onItemClick(note: Note,position:Int)
}
class NotesAdapter(private val noteAdapterInt:NoteAdapterInt)
    :RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    class NotesViewHolder(val binding: ItemNoteBinding):RecyclerView.ViewHolder(binding.root)

    private var notesList:MutableList<Note> = mutableListOf()

    fun addData(position: Int,note:Note){
        notesList.add(position,note)
        notifyItemInserted(position)
        noteAdapterInt.listNotesWasUpdated(notesList)
    }

    fun getItem(position: Int) = notesList[position]

    fun removeItem(position: Int){
        notesList.removeAt(position)
        notifyItemRemoved(position)
        noteAdapterInt.listNotesWasUpdated(notesList)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(notes:List<Note>?){
        if (notes==null) return
        notesList = notes.toMutableList()
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNoteBinding.inflate(inflater,parent,false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val item = notesList[position]
        holder.binding.noteTitle.text = item.title
        holder.binding.noteBody.text = item.body
        holder.binding.root.setOnClickListener {
            noteAdapterInt.onItemClick(item,position)
        }
    }

    override fun getItemCount(): Int = notesList.size
}