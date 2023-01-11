package com.example.todolist.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSignInBinding
import com.example.todolist.presentation.auth.loginbyemailfragment.LogInFragment
import com.example.todolist.presentation.todos.ToDoListFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private lateinit var binding:FragmentSignInBinding
    private val vModel: SignInViewModel by viewModels()


    private lateinit var launcher: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater,container,false)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            if (task.isSuccessful) {
                val acc = task.result
                acc.idToken?.let { idToken -> fireAuth(idToken) }
            } else Log.wtf("xdd", "sign in is canceled")
        }
        binding.buttonGoogleSignIn.apply {
            setOnClickListener { singInGoogle() }
        }
        binding.buttonEmailAndPasswordSignIn.apply {
            setOnClickListener { singInByEmail() }
        }
        vModel.googleSignInResult.observe(viewLifecycleOwner){
            if (it == null) return@observe openFragment()
            Toast.makeText(requireContext(),getString(R.string.error),Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    private fun singInByEmail() {
        parentFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.container, LogInFragment())
            .commit()
    }

    private fun singInGoogle(){
        val client = getCl()
        launcher.launch(client.signInIntent)
    }

    private val reqToken = "745751765961-8nv8d2l87ansjtiltpr0mvd568krki2s.apps.googleusercontent.com"
    private fun getCl(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(reqToken)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun fireAuth(token:String){
        val cred = GoogleAuthProvider.getCredential(token,null)
        vModel.googleSignIn(cred)
    }

    private fun openFragment(){
        parentFragmentManager.beginTransaction()
            .replace(R.id.container,ToDoListFragment())
            .commit()
    }
}