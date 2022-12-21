package com.example.todolist.ui.notefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.todolist.data.model.Note
import com.example.todolist.databinding.FragmentNotesBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesFragment : Fragment() {
    private lateinit var binding: FragmentNotesBinding

    private val vModel:NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater,container,false)

        val adapter = NotesAdapter{ vModel.uploadNotesToFirebase(it) }
        binding.rcViewNotes.adapter = adapter
        vModel.myDataLiveData.observe(viewLifecycleOwner){
            adapter.setData(it?.listNotes)
        }

        binding.buttonAddNote.setOnClickListener {
            adapter.addData(Note("",""))
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
}