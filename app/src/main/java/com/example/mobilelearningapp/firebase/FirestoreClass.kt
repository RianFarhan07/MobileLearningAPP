package com.example.mobilelearningapp.firebase

import android.app.Activity
import android.util.Log
import com.example.mobilelearningapp.activities.LoginGuruActivity
import com.example.mobilelearningapp.activities.LoginSiswaActivity
import com.example.mobilelearningapp.activities.RegisterGuruActivity
import com.example.mobilelearningapp.activities.RegisterSiswaActivity
import com.example.mobilelearningapp.models.Guru
import com.example.mobilelearningapp.models.Siswa
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()


    fun registerSiswa(activity: RegisterSiswaActivity, siswaInfo: Siswa){
        mFireStore.collection(Constants.SISWA)
            .document(siswaInfo.id)
            .set(siswaInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.siswaRegistrationSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Ketika Registrasi User"
                )
            }
    }

    fun registerGuru(activity: RegisterGuruActivity, guruInfo: Guru){
        mFireStore.collection(Constants.GURU)
            .document(guruInfo.id)
            .set(guruInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.guruRegistrationSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Ketika Registrasi User"
                )
            }
    }

    fun getCurrentUserID() : String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getSiswaDetails(activity: Activity) {
        val userCollection = Constants.SISWA

        mFireStore.collection(userCollection)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loggedInSiswa = document.toObject(Siswa::class.java)
                    Log.i(activity.javaClass.simpleName, document.toString())

                    loggedInSiswa?.let { siswa ->
                        when (activity) {
                            is LoginSiswaActivity -> {
                                activity.userLoggedInSuccess()
                            }
//                                is MainActivity -> {
//                                    activity.updateNavigationUserDetails(siswa, readKelompokList)
//                                }
//                                is MyProfileActivity -> {
//                                    activity.setUserDataInUI(siswa)
//                                }
//                                is KelompokDetailsActivity -> {
//                                    activity.setUserDataInUI(siswa)
//                                }
                        }
                    }
                } else {
                    Log.e(activity.javaClass.simpleName.toString(), "Dokumen tidak ditemukan")
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is LoginSiswaActivity -> {
                        activity.hideProgressDialog()
                    }
//                    is MainActivity -> {
//                        activity.hideProgressDialog()
//                    }
//                    is GuruMainActivity -> {
//                        activity.hideProgressDialog()
//                    }
//                    is MyProfileActivity -> {
//                        activity.hideProgressDialog()
//                    }
                }

                Log.e(
                    activity.javaClass.simpleName.toString(),
                    "Error Mengambil data detail siswa", e
                )
            }
    }

    fun getGuruDetails(activity: Activity) {
        val userCollection = Constants.GURU

        mFireStore.collection(userCollection)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loggedInGuru = document.toObject(Guru::class.java)
                    Log.i(activity.javaClass.simpleName, document.toString())

                    loggedInGuru?.let { guru ->
                        when (activity) {
                            is LoginGuruActivity -> {
                                activity.userLoggedInSuccess()
                            }
//                                is MainActivity -> {
//                                    activity.updateNavigationUserDetails(siswa, readKelompokList)
//                                }
//                                is MyProfileActivity -> {
//                                    activity.setUserDataInUI(siswa)
//                                }
//                                is KelompokDetailsActivity -> {
//                                    activity.setUserDataInUI(siswa)
//                                }
                        }
                    }
                } else {
                    Log.e(activity.javaClass.simpleName.toString(), "Dokumen tidak ditemukan")
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is LoginGuruActivity -> {
                        activity.hideProgressDialog()
                    }
//                        is MainActivity -> {
//                            activity.hideProgressDialog()
//                        }
//                        is GuruMainActivity -> {
//                            activity.hideProgressDialog()
//                        }
//                        is MyProfileActivity -> {
//                            activity.hideProgressDialog()
//                        }
                }

                Log.e(
                    activity.javaClass.simpleName.toString(),
                    "Error Mengambil data detail guru", e
                )
            }
    }
}