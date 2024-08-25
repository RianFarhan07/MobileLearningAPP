package com.example.mobilelearningapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.databinding.ActivityGuruProfileBinding
import com.example.mobilelearningapp.databinding.ActivityMateriDetailsBinding

class MateriDetailsActivity : AppCompatActivity() {
    private var binding : ActivityMateriDetailsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMateriDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
    }
}