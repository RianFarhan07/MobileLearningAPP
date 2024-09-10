package com.example.mobilelearningapp.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.mobilelearningapp.databinding.ActivitySplashBinding
import com.example.mobilelearningapp.firebase.FirestoreClass

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

        val typeFaceMontserrat: Typeface = Typeface.createFromAsset(assets, "Montserrat-Bold.ttf")


        binding?.tvSchoolName?.typeface = typeFaceMontserrat
        binding?.tvAppName?.typeface = typeFaceMontserrat


        Handler().postDelayed({
            checkUserLoggedIn()
        }, 3000)
    }

    private fun checkUserLoggedIn() {
        val currentUserID = FirestoreClass().getCurrentUserID()

        Handler().postDelayed({
            if (currentUserID.isNotEmpty()) {
                FirestoreClass().getUserRole(currentUserID) { role ->
                    if (role != null) {
                        handleUserRole(role)
                    }
                }
            } else {
                startActivity(Intent(this, UserChooseActivity::class.java))
                finish()
            }
        }, 2000)
    }

    fun handleUserRole(role: String) {
        when(role) {
            "siswa" -> {
                startActivity(Intent(this, MainActivitySiswa::class.java))
            }
            "guru" -> {
                startActivity(Intent(this, MainGuruActivity::class.java))
            }
        }
        finish()
    }
}