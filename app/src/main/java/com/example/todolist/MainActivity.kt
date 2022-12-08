package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.example.todolist.databinding.ActivityMainBinding
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

const val TAG = "xdd"
class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private lateinit var auth: FirebaseAuth


    private var currentUser:FirebaseUser? = null

    lateinit var launcher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.wtf(TAG, "hi!")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        currentUser = auth.currentUser
        FirebaseApp.initializeApp(this)
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

    }

    private fun singInAnonim() {
        auth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                openFragment(bundleOf("userId" to it.result.user?.uid))
                hideButtons()
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
        return GoogleSignIn.getClient(this, gso)
    }



    private fun fireAuth(token:String){
        val cred = GoogleAuthProvider.getCredential(token,null)
        auth.signInWithCredential(cred).addOnCompleteListener {
            if (it.isSuccessful) {
                openFragment(bundleOf("userId" to it.result.user?.uid))
                hideButtons()
                Log.d(TAG, "done!")
            }
            else Log.d(TAG, "no done!")
        }
    }

    private fun openFragment(args:Bundle){
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment::class.java, args)
            .commitNow()

    }
    private fun hideButtons(){
        binding.buttonAnonimSignIn.visibility = View.GONE
        binding.buttonGoogleSignIn.visibility = View.GONE
    }

















}