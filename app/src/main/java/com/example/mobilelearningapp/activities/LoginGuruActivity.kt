package com.example.mobilelearningapp.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityLoginGuruBinding

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
//                    loginRegisteredUser()
                }

                R.id.tv_register -> {
                    val intent = Intent(this, RegisterGuruActivity::class.java )
                    startActivity(intent)
                }
            }
        }
    }

}