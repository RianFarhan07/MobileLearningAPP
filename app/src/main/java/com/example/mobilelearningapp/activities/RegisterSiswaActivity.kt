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
import com.example.mobilelearningapp.databinding.ActivityMainBinding
import com.example.mobilelearningapp.databinding.ActivityRegisterSiswaBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Siswa
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterSiswaActivity : BaseActivity() {
    private var binding : ActivityRegisterSiswaBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterSiswaBinding.inflate(layoutInflater)
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

        binding?.tvLogin?.setOnClickListener{
            val intent = Intent(this, LoginSiswaActivity::class.java)
            startActivity(intent)
        }
        binding?.btnRegister?.setOnClickListener{
            registerSiswa()
        }
    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding?.etName?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Silahkan isi nama depan dan nama belakang", true)
                false
            }

            TextUtils.isEmpty(binding?.etEmail?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Silahkan isi email anda", true)
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
            binding?.cbTermsAndCondition?.isChecked == false -> {
                showErrorSnackBar(
                    "Harap centang syarat dan ketentuan",
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerSiswa() {
        if (validateRegisterDetails()) {
            showProgressDialog(resources.getString(R.string.mohon_tunggu))

            val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
            val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val siswa = Siswa(
                            firebaseUser.uid,
                            binding?.etName?.text.toString().trim{ it <= ' ' },
                            binding?.etEmail?.text.toString().trim{ it <= ' ' },
                        )

                        FirestoreClass().registerSiswa(this,siswa)
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                        hideProgressDialog()
                    }
                }
        }
    }

    fun siswaRegistrationSuccess(){
        hideProgressDialog()
        Toast.makeText(this@RegisterSiswaActivity,"Anda Berhasil Mendaftar",
            Toast.LENGTH_LONG).show()

        val intent = Intent(this,LoginSiswaActivity::class.java)
        startActivity(intent)
    }
}