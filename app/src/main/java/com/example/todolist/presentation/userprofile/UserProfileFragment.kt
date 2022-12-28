package com.example.todolist.presentation.userprofile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.databinding.DialogChangeNicknameBinding
import com.example.todolist.databinding.FragmentUserProfileBinding
import com.example.todolist.presentation.auth.SignInFragment
import com.example.todolist.presentation.userprofile.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private lateinit var binding:FragmentUserProfileBinding
    private val vModel:UserProfileViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater,container,false)
        vModel.myDataLiveData.observe(viewLifecycleOwner){it->
            val countOfTodos = getString(R.string.count_of_todo) + " ${it?.listTodo?.size}"
            binding.countOfToDos.text = countOfTodos
            var endedTodo = 0
            it?.listTodo?.forEach { if (it.isCompleted) endedTodo++ }
            val endedTodoStr = getString(R.string.count_of_ended_todo) + " $endedTodo"
            binding.countOfEndedToDos.text = endedTodoStr
            val toDoInWork = it?.listTodo?.size?.minus(endedTodo) ?: 0
            val toDoInWorkStr = getString(R.string.count_of_todo_in_progress)+" $toDoInWork"
            binding.countOfToDosInWork.text = toDoInWorkStr
            binding.userNickName.text = it?.userData?.nickname ?: getString(R.string.you_havent_nickname)
            val countNotes = getString(R.string.count_of_notes) + " ${it?.listNotes?.size}"
            binding.countOfNotes.text = countNotes
        }
        binding.buttonSignOut.setOnClickListener {
            vModel.signOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, SignInFragment())
                .commit()
        }
        binding.buttonSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, SettingsFragment())
                .commit()
        }
        binding.userNickName.setOnClickListener {
            val dialogChangeNicknameBinding = DialogChangeNicknameBinding.inflate(layoutInflater)
            val oldNick = binding.userNickName.text
            if (oldNick != getString(R.string.you_havent_nickname))
                dialogChangeNicknameBinding.nicknameEditTextView.setText(oldNick)
            AlertDialog.Builder(context)
                .setView(dialogChangeNicknameBinding.root)
                .setPositiveButton(R.string.apply) { _, _ ->
                    val newNickname = dialogChangeNicknameBinding.nicknameEditTextView.text.toString()
                    if (newNickname.isEmpty()) return@setPositiveButton
                    activity?.title = newNickname
                    binding.userNickName.text = newNickname
                    vModel.updateNickname(newNickname)
                }
                .setNegativeButton(getString(R.string.cancel)){ _, _ ->}
                .setTitle(R.string.change_nickname)
                .show()
        }
        return binding.root
    }

}