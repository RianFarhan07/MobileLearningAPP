package com.example.mobilelearningapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.databinding.ActivityIntroBinding


class IntroActivity : AppCompatActivity() {
    private var binding : ActivityIntroBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        binding?.
    }
}