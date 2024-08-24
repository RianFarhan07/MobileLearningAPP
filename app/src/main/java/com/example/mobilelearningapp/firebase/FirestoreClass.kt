package com.example.mobilelearningapp.firebase

import android.app.Activity
import android.util.Log
import com.example.mobilelearningapp.activities.*
import com.example.mobilelearningapp.models.Guru
import com.example.mobilelearningapp.models.Siswa
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun getCurrentUserID() : String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getUserRole(userId: String, onComplete: (String?) -> Unit) {
        mFireStore.collection(Constants.SISWA)
            .document(userId)
            .get()
            .addOnSuccessListener { siswaDocument ->
                if (siswaDocument.exists()) {
                    onComplete(Constants.SISWA) // Mengembalikan peran siswa
                } else {
                    // Jika tidak ditemukan sebagai siswa, cek sebagai guru
                    mFireStore.collection(Constants.GURU)
                        .document(userId)
                        .get()
                        .addOnSuccessListener { guruDocument ->
                            if (guruDocument.exists()) {
                                onComplete(Constants.GURU) // Mengembalikan peran guru
                            } else {
                                onComplete(null) // Jika tidak ditemukan sebagai guru, kembalikan null
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreClass", "Error getting user role (guru): ${e.message}", e)
                            onComplete(null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreClass", "Error getting user role (siswa): ${e.message}", e)
                onComplete(null)
            }
    }

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
                                is MainActivitySiswa -> {
                                    activity.updateNavigationUserDetails(siswa)
                                }
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
                                is MainGuruActivity -> {
                                    activity.updateNavigationUserDetails(guru)
                                }
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