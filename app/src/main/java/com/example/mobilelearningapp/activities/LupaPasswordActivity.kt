package com.example.mobilelearningapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityLupaPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class LupaPasswordActivity : BaseActivity() {
    private var binding : ActivityLupaPasswordBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLupaPasswordBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()

        binding?.btnSubmit?.setOnClickListener {
            val email: String = binding?.etEmail?.text.toString().trim() { it <= ' ' }
            if (email.isEmpty()) {
                showErrorSnackBar("Masukkan email anda", true)
            } else {
                showProgressDialog(resources.getString(R.string.mohon_tunggu))
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        hideProgressDialog()
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@LupaPasswordActivity,
                                "email reset password berhasil dikirim ke $email",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
            }
        }
    }
    private fun setupActionBar() {

        setSupportActionBar(binding?.toolbarForgotPasswordActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding?.toolbarForgotPasswordActivity?.setNavigationOnClickListener { onBackPressed() }
    }
}