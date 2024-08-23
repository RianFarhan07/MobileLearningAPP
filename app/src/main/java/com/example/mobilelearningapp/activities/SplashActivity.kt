package com.example.mobilelearningapp.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.mobilelearningapp.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {
    private var binding : ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val typeFacePixel: Typeface = Typeface.createFromAsset(assets,"PixelArmy.ttf")
        val typeFaceBrazil: Typeface = Typeface.createFromAsset(assets,"BrasiliaDelight-Regular.ttf")

        binding?.tvSchoolName?.typeface = typeFacePixel
        binding?.tvAppName?.typeface = typeFaceBrazil

        binding?.ivIcon?.setOnClickListener{
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }

        Handler().postDelayed({
//            checkUserLoggedIn()
        }, 3000)
    }