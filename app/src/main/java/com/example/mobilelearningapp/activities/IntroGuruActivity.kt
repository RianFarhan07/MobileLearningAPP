package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.databinding.ActivityIntroGuruBinding

class IntroGuruActivity : AppCompatActivity() {
    private var binding : ActivityIntroGuruBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityIntroGuruBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        binding?.btnLogin?.setOnClickListener {
            val intent = Intent(this,LoginGuruActivity::class.java)
            startActivity(intent)
        }
        binding?.btnRegister?.setOnClickListener {
            val intent = Intent(this, RegisterGuruActivity::class.java)
            startActivity(intent)
        }
    }
}