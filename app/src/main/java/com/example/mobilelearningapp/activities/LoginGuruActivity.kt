package com.example.mobilelearningapp.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityLoginGuruBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth

class LoginGuruActivity : BaseActivity(), View.OnClickListener {
    private var binding : ActivityLoginGuruBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginGuruBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val typeFaceArco: Typeface = Typeface.createFromAsset(assets, "ARCO.ttf")
        binding?.tvLogin?.typeface = typeFaceArco
        binding?.tvTitle?.typeface = typeFaceArco

        binding?.btnLogin?.setOnClickListener(this)
        binding?.tvRegister?.setOnClickListener(this)
        binding?.tvForgotPassword?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.tv_forgot_password -> {
                    val intent = Intent(this, LupaPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_login -> {
                    loginRegisteredUser()
                }

                R.id.tv_register -> {
                    val intent = Intent(this, RegisterGuruActivity::class.java )
                    startActivity(intent)
                }
            }
        }
    }

    private fun validateLoginDetails() : Boolean {
        return when{
            TextUtils.isEmpty(binding?.etEmail?.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar("Silahkan isi email anda",true)
                false
            }
            TextUtils.isEmpty(binding?.etPassword?.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar("Silahkan isi password anda",true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun loginRegisteredUser(){
        if (validateLoginDetails()){
            showProgressDialog(resources.getString(R.string.mohon_tunggu))

            val email = binding?.etEmail?.text.toString().trim{ it <= ' '}
            val password = binding?.etPassword?.text.toString().trim{it <= ' '}

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful){
                        FirestoreClass().getGuruDetails(this)
                    }else{
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(),true)
                    }
                }
        }
    }


    fun userLoggedInSuccess() {
        hideProgressDialog()
        val intent = Intent(this@LoginGuruActivity, MainGuruActivity::class.java)
        startActivity(intent)
    }
}