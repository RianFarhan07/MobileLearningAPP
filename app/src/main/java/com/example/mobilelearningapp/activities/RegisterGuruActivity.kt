package com.example.mobilelearningapp.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.example.mobilelearningapp.databinding.ActivityMainBinding
import com.example.mobilelearningapp.databinding.ActivityRegisterGuruBinding

class RegisterGuruActivity : AppCompatActivity() {
    private var binding : ActivityRegisterGuruBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterGuruBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        binding?.tvLogin?.setOnClickListener{
            val intent = Intent(this, LoginGuruActivity::class.java)
            startActivity(intent)
        }
        binding?.btnRegister?.setOnClickListener{
//            registerGuru()
        }
    }
}