package com.example.todolist.ui.loginbyemailfragment.signin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSignInByLoginBinding
import com.example.todolist.ui.activity.TAG
import com.example.todolist.ui.mainfragment.MainFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInByLoginFragment : Fragment() {
    lateinit var binding:FragmentSignInByLoginBinding
    @Inject lateinit var auth: FirebaseAuth

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

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container,MainFragment())
                        .commit()
                    return@addOnCompleteListener
                }
                showLongToast(getString(R.string.sign_in_failed))
                Log.i(TAG, "registerByPassword: ",it.exception)
            }

        }
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (!email.isEmailValid()) return@setOnClickListener showLongToast(getString(R.string.email_is_invalid))
            auth.sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful)
                    return@addOnCompleteListener showLongToast(getString(R.string.email_sent))

                Log.wtf(TAG, "onCreateView: ", it.exception)
                showLongToast(getString(R.string.unwnown_error))
            }
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