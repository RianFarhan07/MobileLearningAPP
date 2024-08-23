package com.example.mobilelearningapp.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.mobilelearningapp.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val typeFaceMontserrat: Typeface = Typeface.createFromAsset(assets, "Montserrat-Regular.ttf")


        binding?.tvSchoolName?.typeface = typeFaceMontserrat
        binding?.tvAppName?.typeface = typeFaceMontserrat


        Handler().postDelayed({
           val intent = Intent(this, UserChooseActivity::class.java)
            startActivity(intent)
        }, 3000)
    }
}