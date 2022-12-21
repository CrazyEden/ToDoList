package com.example.todolist.ui.notefragment

import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.model.Note
import com.example.todolist.databinding.ItemNoteBinding

typealias ListNotesWasUpdated = (notesList:MutableList<Note>) -> Unit

class NotesAdapter(private val listNotesWasUpdated: ListNotesWasUpdated)
    :RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    class NotesViewHolder(val binding: ItemNoteBinding):RecyclerView.ViewHolder(binding.root)

    private var notesList:MutableList<Note> = mutableListOf()

    fun addData(position: Int,note:Note){
        notesList.add(position,note)
        notifyItemInserted(position)
        listNotesWasUpdated(notesList)
    }
    fun addData(note:Note){
        notesList.add(note)
        notifyItemInserted(notesList.size)
        listNotesWasUpdated(notesList)
    }

    fun getItem(position: Int) = notesList[position]

    fun removeItem(position: Int){
        notesList.removeAt(position)
        notifyItemRemoved(position)
        listNotesWasUpdated(notesList)
    }

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
        holder.binding.noteTitle.apply {
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            setText(item.title.toString())
            setOnEditorActionListener { v, _, _ ->
                v.clearFocus()
                false
            }
            setOnFocusChangeListener { it, hasFocus ->
                if (hasFocus || it !is EditText) return@setOnFocusChangeListener
                notesList[position].title = it.text.toString()
                listNotesWasUpdated(notesList)
            }
        }
        holder.binding.noteBody.apply {
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            setText(item.body.toString())
            setOnEditorActionListener { v, _, _ ->
                v.clearFocus()
                false
            }
            setOnFocusChangeListener { it, hasFocus ->
                if (hasFocus || it !is EditText) return@setOnFocusChangeListener
                notesList[position].body = it.text.toString()
                listNotesWasUpdated(notesList)
            }
        }
    }

    override fun getItemCount(): Int = notesList.size
}