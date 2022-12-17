package com.example.todolist.ui.loginbyemailfragment.signup

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSignUpByLoginBinding
import com.example.todolist.ui.activity.TAG
import com.example.todolist.ui.mainfragment.MainFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpByLoginFragment : Fragment() {
    lateinit var binding: FragmentSignUpByLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val auth = Firebase.auth
        binding = FragmentSignUpByLoginBinding.inflate(inflater, container, false)
        binding.buttonDone.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val passwordSecond = binding.editTextPasswordSecond.text.toString()
            if (email.isEmpty() || password.isEmpty() || passwordSecond.isEmpty()) return@setOnClickListener showLongToast("Заполните все поля")
            if (!email.isEmailValid()) return@setOnClickListener showLongToast("Неверный логин")
            if (password.length < 6) return@setOnClickListener showLongToast("Слишком короткий пароль")
            if (password != passwordSecond) return@setOnClickListener showLongToast("Пароли не совпадают")
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container,MainFragment())
                        .commit()
                    return@addOnCompleteListener
                }
                showLongToast(
                    when(it.exception?.message){
                        "The email address is already in use by another account." -> getString(R.string.the_email_is_already_registered)
                        else -> getString(R.string.register_is_failed_try_again_later)
                    })
                Log.wtf(TAG, "registerByPassword: ",it.exception)
            }

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