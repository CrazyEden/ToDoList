package com.example.todolist.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSignInBinding
import com.example.todolist.ui.activity.TAG
import com.example.todolist.ui.loginbyemailfragment.LogInFragment
import com.example.todolist.ui.mainfragment.MainFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignInFragment : Fragment() {
    private lateinit var binding:FragmentSignInBinding
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater,container,false)

        auth = Firebase.auth
        currentUser = auth.currentUser

        if (currentUser!=null) {
            openFragment()
            return binding.root
        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            if (task.isSuccessful) {
                val acc = task.result
                acc.idToken?.let { idToken -> fireAuth(idToken) }
            } else Log.wtf("xdd", "sign in is canceled")
        }
        binding.buttonGoogleSignIn.apply {
            visibility = View.VISIBLE
            setOnClickListener { singInGoogle() }
        }
        binding.buttonEmailAndPasswordSignIn.apply {
            visibility = View.VISIBLE
            setOnClickListener { singInByPassword2() }
        }
        return binding.root
    }

    private fun singInByPassword2() {
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
        auth.signInWithCredential(cred).addOnCompleteListener {
            if (it.isSuccessful) {
                openFragment()
                Log.d(TAG, "done!")
            }
            else Log.d(TAG, "no done!")
        }
    }

    private fun openFragment(){
        parentFragmentManager.beginTransaction()
            .replace(R.id.container,MainFragment())
            .commit()
    }
}