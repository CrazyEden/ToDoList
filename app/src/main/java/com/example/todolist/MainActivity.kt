package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true
    private var currentUser:FirebaseUser? = null
    private val TAG = "xdd"
    lateinit var launcher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "hi!")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            it.resultCode
            RESULT_CANCELED
//            try {
                Log.wtf(TAG, it.resultCode.toString())
                Log.wtf(TAG, it.data?.data.toString())
                // As documented, we return a completed Task in this case and it's safe to directly call
                // getResult(Class<ExceptionType>) here (without need to worry about IllegalStateException).
                val a = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                val b = a.getResult(ApiException::class.java)
                b.idToken?.let { it1 -> fireAuth(it1) }
//            } catch (apiException: ApiException) {
//                Log.wtf(TAG, "Unexpected error parsing sign-in result")
//            }
//
//            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
//
//                val acc = task.result
//                acc.idToken?.let { it1 -> fireAuth(it1) }
        }
        binding.knopka.setOnClickListener { singIn() }

    }
    val reqToken = "696381024372-453jfa8045m5j525ipvg0d6viskrt8lc.apps.googleusercontent.com"
    private fun getCl(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(reqToken)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun singIn(){
        val client = getCl()
        launcher.launch(client.signInIntent)
    }

    private fun fireAuth(token:String){
        val cred = GoogleAuthProvider.getCredential(token,null)
        auth.signInWithCredential(cred).addOnCompleteListener {
            if (it.isSuccessful) Log.d(TAG, "done!")
            else Log.d(TAG, "no done!")
        }
    }













//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, MainFragment())
//                .commitNow()
//        }





}