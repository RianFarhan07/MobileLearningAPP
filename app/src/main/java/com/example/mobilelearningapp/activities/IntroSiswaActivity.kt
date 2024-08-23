package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.databinding.ActivityIntroSiswaBinding

class IntroSiswaActivity : AppCompatActivity() {
    private var binding : ActivityIntroSiswaBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityIntroSiswaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        binding?.btnLogin?.setOnClickListener {
            val intent = Intent(this,LoginSiswaActivity::class.java)
            startActivity(intent)
        }
        binding?.btnRegister?.setOnClickListener {
            val intent = Intent(this, RegisterSiswaActivity::class.java)
            startActivity(intent)
        }
    }
}