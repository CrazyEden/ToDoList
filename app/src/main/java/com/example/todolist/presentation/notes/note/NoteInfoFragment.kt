package com.example.todolist.presentation.notes.note

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.data.model.Note
import com.example.todolist.databinding.FragmentNoteInfoBinding
import com.example.todolist.presentation.activity.TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteInfoFragment : Fragment() {
    private lateinit var binding:FragmentNoteInfoBinding
    private lateinit var note: Note
    private var position:Int? = null
    private val vModel: NoteInfoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        note = arguments?.getParcelable(NOTE_KEY) as? Note ?: Note()
        position = arguments?.getInt(POSITION_KEY,-1)
        if (position == -1) position = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteInfoBinding.inflate(inflater,container,false)
        if (position == null)binding.floatButtonNote.setImageResource(R.drawable.ic_create)
        initUi()

        vModel.noteUpdateLiveData.observe(viewLifecycleOwner){
            if (it==null)
                parentFragmentManager.popBackStack()
            else
                Log.wtf(TAG, "noteUpdateLiveData: ", it)
        }

        return binding.root
    }

    private fun initUi() {
        binding.noteTitleInfofr.apply {
            setText(note.title)
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            addTextChangedListener {
                note.title = it.toString()
            }
            setOnEditorActionListener { v, _, _ ->
                v.clearFocus()
                true
            }
        }

        binding.noteBodyInfofr.apply {
            setText(note.body)
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            addTextChangedListener {
                note.body = it.toString()
            }
            setOnEditorActionListener { v, _, _ ->
                v.clearFocus()
                true
            }
        }

        binding.floatButtonNote.setOnClickListener {
            if (note.title.isEmpty() || note.body.isEmpty())
                return@setOnClickListener Toast.makeText(context,getString(R.string.fields_should_be_not_empty),Toast.LENGTH_LONG).show()
            if (position==null)
                vModel.createNewNote(note)
            else vModel.updateNote(note, position!!)
        }
        binding.notefrScroller.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY)binding.floatButtonNote.hide()
            else binding.floatButtonNote.show()
            if (scrollY == 0) binding.floatButtonNote.show()
        }
    }

    companion object {
        const val NOTE_KEY = "note_key"
        const val POSITION_KEY = "position_key"
    }
}