package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.FragmentSignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
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
        FirebaseApp.initializeApp(requireContext())
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance())

        Log.wtf("xdd", "user is null = ${currentUser == null}")
        Log.wtf("xdd", "user id = ${auth.currentUser?.uid}")

        if (currentUser!=null)openFragment(bundleOf("userId" to currentUser!!.uid))
        else {
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
            binding.buttonAnonimSignIn.apply {
                visibility = View.VISIBLE
                setOnClickListener { singInAnonim() }
            }
        }

        return binding.root
    }


    private fun singInAnonim() {
        auth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                openFragment(bundleOf("userId" to it.result.user?.uid))
                Log.d(TAG, "done!")
            }
            else Log.d(TAG, "no done!")
        }
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
                openFragment(bundleOf("userId" to it.result.user?.uid))
                Log.d(TAG, "done!")
            }
            else Log.d(TAG, "no done!")
        }
    }

    private fun openFragment(args:Bundle){
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment::class.java, args)
            .commit()
    }}