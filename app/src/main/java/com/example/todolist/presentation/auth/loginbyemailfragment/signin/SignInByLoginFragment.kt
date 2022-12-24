package com.example.todolist.presentation.auth.loginbyemailfragment.signin

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSignInByLoginBinding
import com.example.todolist.presentation.mainfragment.MainFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInByLoginFragment : Fragment() {
    lateinit var binding:FragmentSignInByLoginBinding
    private val vModel: SignInViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentSignInByLoginBinding.inflate(inflater, container, false)
        binding.buttonDone.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) return@setOnClickListener showLongToast(getString(R.string.not_all_fields_are_filled))
            if (!email.isEmailValid()) return@setOnClickListener showLongToast(getString(R.string.email_is_invalid))
            if (password.length < 6) return@setOnClickListener showLongToast(getString(R.string.password_is_too_short))

            vModel.signInWithEmailAndPassword(email,password)
        }
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (!email.isEmailValid()) return@setOnClickListener showLongToast(getString(R.string.email_is_invalid))

            vModel.sendEmailToResetPassword(email)
        }
        vModel.signInWithEmailAndPassword.observe(viewLifecycleOwner){
            if (it==null){
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container,MainFragment())
                    .commit()
            } else showLongToast(getString(R.string.sign_in_failed))
        }
        return binding.root
    }
    private fun showLongToast(text:String){
        Toast.makeText(context,text,Toast.LENGTH_LONG).show()
    }
    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}