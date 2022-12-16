package com.example.todolist.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.todolist.R
import com.example.todolist.databinding.DialogSignInByEmailAndPasswordBinding
import com.example.todolist.databinding.FragmentSignInBinding
import com.example.todolist.ui.activity.TAG
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
            openFragment(bundleOf("userId" to currentUser!!.uid))
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
            setOnClickListener { singInByPassword() }
        }
        return binding.root
    }

    private var lastEmail:String = ""
    private fun singInByPassword() {
        val signInDialogBinding = DialogSignInByEmailAndPasswordBinding.inflate(layoutInflater)
        signInDialogBinding.editTextEmail.setText(lastEmail)
        val dialog = AlertDialog.Builder(context)
            .setView(signInDialogBinding.root)
            .setPositiveButton(getString(R.string.sign_in),null)      //
            .setNegativeButton(getString(R.string.sign_up),null)      // late init ClickListeners
            .setNeutralButton(getString(R.string.password_reset),null)//
            .setOnDismissListener { lastEmail = signInDialogBinding.editTextPassword.text.toString() }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val email = signInDialogBinding.editTextEmail.text.toString()
            val password = signInDialogBinding.editTextPassword.text.toString()
            if (password.isEmpty() || email.isEmpty()) return@setOnClickListener
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful) {
                    dialog.dismiss()
                    return@addOnCompleteListener openFragment(bundleOf("userId" to it.result.user!!.uid))
                }
                makeLongToast(getString(R.string.invalid_email_or_password))
                Log.wtf(TAG, "singInByPassword: ", it.exception)
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            val email = signInDialogBinding.editTextEmail.text.toString()
            val password = signInDialogBinding.editTextPassword.text.toString()
            if (password.isEmpty() || email.isEmpty())
                return@setOnClickListener makeLongToast(getString(R.string.not_all_fields_are_filled))
            if (password.length<6)
                return@setOnClickListener makeLongToast(getString(R.string.password_is_too_short))
            if (!email.isEmailValid())
                return@setOnClickListener makeLongToast(getString(R.string.email_is_invalid))

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    dialog.dismiss()
                    openFragment(bundleOf("userId" to it.result.user!!.uid))
                    return@addOnCompleteListener
                }
                makeLongToast(
                    when(it.exception?.message){
                        "The email address is already in use by another account." -> getString(R.string.the_email_is_already_registered)
                        else -> getString(R.string.register_is_failed_try_again_later)
                    })
                Log.wtf(TAG, "registerByPassword: ",it.exception)
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            val email = signInDialogBinding.editTextEmail.text.toString()
            auth.sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful)
                    return@addOnCompleteListener makeLongToast(getString(R.string.email_sent))
                Log.wtf(TAG, "singInByPassword: ", it.exception )
                makeLongToast(getString(R.string.try_later))

            }
        }
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun makeLongToast(text:String) =
        Toast.makeText(context,text,Toast.LENGTH_LONG).show()

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