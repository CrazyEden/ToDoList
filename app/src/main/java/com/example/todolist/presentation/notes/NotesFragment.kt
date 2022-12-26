package com.example.todolist.presentation.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.todolist.R
import com.example.todolist.data.model.Note
import com.example.todolist.databinding.FragmentNotesBinding
import com.example.todolist.presentation.notes.note.NoteInfoFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesFragment : Fragment(),NoteAdapterInt {
    private lateinit var binding: FragmentNotesBinding

    private val vModel:NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater,container,false)

        val adapter = NotesAdapter(this)
        binding.rcViewNotes.adapter = adapter
        vModel.myDataLiveData.observe(viewLifecycleOwner){
            adapter.setData(it?.listNotes)
        }

        binding.buttonAddNote.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, NoteInfoFragment())
                .commit()
        }
        binding.rcViewNotes.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)binding.buttonAddNote.hide()
                else binding.buttonAddNote.show()
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        initRcViewSwipe(adapter)
        return binding.root
    }
    private fun initRcViewSwipe(adapter:NotesAdapter){
        ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder) = false

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val item = adapter.getItem(position)
                if (direction != ItemTouchHelper.LEFT) return
                adapter.removeItem(position)
                Snackbar.make(binding.rcViewNotes,"xdd",Snackbar.LENGTH_LONG)
                    .setAction("Отменить"){ adapter.addData(position,item) }
                    .show()
            }
        }).attachToRecyclerView(binding.rcViewNotes)
    }

    override fun listNotesWasUpdated(notesList: MutableList<Note>) {
        vModel.uploadNotesToFirebase(notesList)
    }

    override fun onItemClick(note: Note,position: Int) {
        val args = bundleOf(
            NoteInfoFragment.NOTE_KEY to note,
            NoteInfoFragment.POSITION_KEY to position
        )
        parentFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.container, NoteInfoFragment::class.java,args)
            .commit()
    }
}