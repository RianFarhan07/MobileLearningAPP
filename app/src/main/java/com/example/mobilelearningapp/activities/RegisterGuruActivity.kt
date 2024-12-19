package com.example.mobilelearningapp.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.mobilelearningapp.R

import com.example.mobilelearningapp.databinding.ActivityRegisterGuruBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Guru
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterGuruActivity : BaseActivity() {
    private var binding : ActivityRegisterGuruBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        binding = ActivityRegisterGuruBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        binding?.tvLogin?.setOnClickListener {
            val intent = Intent(this, LoginGuruActivity::class.java)
            startActivity(intent)
        }
        binding?.btnRegister?.setOnClickListener {
            if (binding?.etPassGuru?.text.toString() != "123") {
                showErrorSnackBar("pass guru salah", true)
            } else {
                registerGuru()
            }
        }
    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding?.etName?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Silahkan isi nama", true)
                false
            }

            TextUtils.isEmpty(binding?.etEmail?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Silahkan isi email anda", true)
                false
            }

            TextUtils.isEmpty(binding?.etPassGuru?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Silahkan isi pass guru", true)
                false
            }

            TextUtils.isEmpty(binding?.etPassword?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Silahkan isi password anda", true)
                false
            }

            TextUtils.isEmpty(binding?.etConfirmPassword?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    "Silahkan konfirmasi password anda",
                    true
                )
                false
            }

            binding?.etPassword?.text.toString()
                .trim { it <= ' ' } != binding?.etConfirmPassword?.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(
                    "Password dan konfirmasi password tidak sama",
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerGuru() {
        if (validateRegisterDetails()) {
            showProgressDialog(resources.getString(R.string.mohon_tunggu))

            val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
            val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val guru = Guru(
                            firebaseUser.uid,
                            binding?.etName?.text.toString().trim{ it <= ' ' },
                            binding?.etEmail?.text.toString().trim{ it <= ' ' },
                        )

                        FirestoreClass().registerGuru(this,guru)
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                        hideProgressDialog()
                    }
                }

        }
    }

    fun guruRegistrationSuccess(){
        hideProgressDialog()
        Toast.makeText(this@RegisterGuruActivity,"Anda Berhasil Mendaftar",
            Toast.LENGTH_LONG).show()

        val intent = Intent(this@RegisterGuruActivity, LoginGuruActivity::class.java)
        startActivity(intent)
    }
}