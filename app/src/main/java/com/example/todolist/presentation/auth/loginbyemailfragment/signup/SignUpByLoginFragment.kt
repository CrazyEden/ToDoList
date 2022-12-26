package com.example.todolist.presentation.auth.loginbyemailfragment.signup

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSignUpByLoginBinding
import com.example.todolist.presentation.todos.ToDoListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpByLoginFragment : Fragment() {
    lateinit var binding: FragmentSignUpByLoginBinding
    private val vModel:SignUpViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentSignUpByLoginBinding.inflate(inflater, container, false)
        binding.buttonDone.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val passwordSecond = binding.editTextPasswordSecond.text.toString()
            if (email.isEmpty() || password.isEmpty() || passwordSecond.isEmpty()) return@setOnClickListener showLongToast("Заполните все поля")
            if (!email.isEmailValid()) return@setOnClickListener showLongToast(getString(R.string.email_is_invalid))
            if (password.length < 6) return@setOnClickListener showLongToast(getString(R.string.password_is_too_short))
            if (password != passwordSecond) return@setOnClickListener showLongToast(getString(R.string.passwords_dont_match))

            vModel.createUserByEmailAndPassword(email, password)
        }
        vModel.createUserLiveData.observe(viewLifecycleOwner){
            if (it == null){
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container,ToDoListFragment())
                    .commit()
                return@observe
            }
            showLongToast(
                when(it.message){
                    "The email address is already in use by another account." -> getString(R.string.the_email_is_already_registered)
                    else -> getString(R.string.register_is_failed_try_again_later)
                })

        }
        return binding.root
    }
    private fun showLongToast(text:String){
        Toast.makeText(context,text, Toast.LENGTH_LONG).show()
    }
    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}