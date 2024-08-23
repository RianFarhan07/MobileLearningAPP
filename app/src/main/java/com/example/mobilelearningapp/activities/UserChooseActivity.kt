package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.databinding.ActivityUserChooseBinding


class UserChooseActivity : AppCompatActivity() {
    private var binding : ActivityUserChooseBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUserChooseBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        binding?.btnGuru?.setOnClickListener {
            val intent = Intent(this,IntroGuruActivity::class.java)
            startActivity(intent)
        }
        binding?.btnSiswa?.setOnClickListener {
            val intent = Intent(this,IntroSiswaActivity::class.java)
            startActivity(intent)
        }
    }
}