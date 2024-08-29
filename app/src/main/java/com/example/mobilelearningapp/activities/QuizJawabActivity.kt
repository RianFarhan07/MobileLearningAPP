package com.example.mobilelearningapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.databinding.ActivityQuizJawabBinding

class QuizJawabActivity : AppCompatActivity() {
    private var binding : ActivityQuizJawabBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityQuizJawabBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)


    }
}