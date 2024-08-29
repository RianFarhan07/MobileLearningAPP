package com.example.mobilelearningapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.databinding.ActivityCreateQuestionBinding
import com.example.mobilelearningapp.databinding.ActivityCreateQuizBinding

class CreateQuestionActivity : AppCompatActivity() {
    private var binding : ActivityCreateQuestionBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateQuestionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()

    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarAddQuestion)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)


        }
        binding?.toolbarAddQuestion?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}