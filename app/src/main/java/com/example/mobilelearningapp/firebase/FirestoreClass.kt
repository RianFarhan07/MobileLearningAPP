package com.example.mobilelearningapp.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.mobilelearningapp.activities.*
import com.example.mobilelearningapp.models.*
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    val mFireStore = FirebaseFirestore.getInstance()

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
                            is MyProfileActivity -> {
                                activity.setSiswaDataInUI(siswa)
                            }
//                                is KelompokDetailsActivity -> {
//                                    activity.setUserDataInUI(siswa)
//                                }
                        }
                    }
                } else {
                    when(activity) {
                        is LoginSiswaActivity -> {
                            activity.hideProgressDialog()
                            Toast.makeText(activity,"Email tidak ditemukan",Toast.LENGTH_SHORT).show()
                        }
                        is MainActivitySiswa -> {
                            activity.hideProgressDialog()
                        }
                        is MyProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
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
                            is GuruProfileActivity -> {
                                activity.setGuruDataInUI(guru)
                            }
//                               is KelompokDetailsActivity -> {
//                                  activity.setUserDataInUI(siswa)
//                               }
                        }
                    }
                } else {
                    when(activity) {
                        is LoginGuruActivity -> {
                            activity.hideProgressDialog()
                            Toast.makeText(activity,"Email tidak ditemukan",Toast.LENGTH_SHORT).show()
                        }
                        is MainGuruActivity -> {
                            activity.hideProgressDialog()
                        }
                        is GuruProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
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

    fun createKelas(activity : MainGuruActivity, kelas: Kelas){
        mFireStore.collection(Constants.KELAS)
            .document()
            .set(kelas, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Kelas berhasil dibuat")
                Toast.makeText(activity,"Berhasil membuat kelas", Toast.LENGTH_LONG).show()
                activity.kelompokCreatedSuccessfully()

            }.addOnFailureListener {
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error membuat kelas",e)
            }
    }

    fun getKelasList(activity: Activity){
        mFireStore.collection(Constants.KELAS)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val kelompokList : ArrayList<Kelas> = ArrayList()
                for(i in document.documents){
                    val kelas = i.toObject(Kelas::class.java)!!
                    kelas.documentId = i.id
                    kelompokList.add(kelas)
                }

                when(activity) {
                    is MainGuruActivity -> {
                        activity.populateKelasListToUI(kelompokList)
                    }
                    is MainActivitySiswa -> {
                        activity.populateKelasListToUI(kelompokList)
                    }
                }

            }.addOnFailureListener {
                when(activity) {
                    is MainGuruActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivitySiswa -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error mendapatkan kelompok")
            }
    }

    fun getKelasDetails(activity: Activity, documentId: String) {
        mFireStore.collection(Constants.KELAS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val kelas = document.toObject(Kelas::class.java)
                kelas?.documentId = document.id

                when (activity) {
                    is MateriListActivity -> {
                        activity.kelasDetails(kelas!!)
                    }
                    is MateriDetailsActivity -> {
                        activity.kelasDetails(kelas!!)
                    }
                    is TugasActivity -> {
                        activity.kelasDetails(kelas!!)
                    }
                }
            }
            .addOnFailureListener { e ->

                when (activity) {
                    is MateriListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MateriDetailsActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error fetching kelompok details: ${e.message}")
            }
    }

    fun updateKelasData(activity: Activity, kelasHashMap: HashMap<String, Any>, kelasId : String){
        mFireStore.collection(Constants.KELAS)
            .document(kelasId)
            .update(kelasHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Kelas Data Update Successfully")
                Toast.makeText(activity,"kelas update successfully", Toast.LENGTH_LONG).show()
                when(activity) {
                    is MainGuruActivity -> {
                        activity.kelompokUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener {
                    e->
                when(activity) {
                    is MainGuruActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while update a profile",
                    e
                )
                Toast.makeText(activity,"ERROR while update a profile", Toast.LENGTH_LONG).show()
            }
    }

    fun deleteKelas(activity: MainGuruActivity, kelasId: String){
        mFireStore.collection(Constants.KELAS)
            .document(kelasId)
            .delete()
            .addOnSuccessListener {
                activity.kelasDeleteSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName,
                    "Error while deleting kelompok",e)
            }
    }



    fun updateSiswaProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.SISWA)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile Data Update Successfully")
                Toast.makeText(activity,"profile update successfully", Toast.LENGTH_LONG).show()
                when(activity) {
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener {
                    e->
                when(activity) {
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while update a profile",
                    e
                )
                Toast.makeText(activity,"ERROR while update a profile", Toast.LENGTH_LONG).show()
            }
    }
    fun updateGuruProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.GURU)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile Data Update Successfully")
                Toast.makeText(activity,"profile update successfully", Toast.LENGTH_LONG).show()
                when(activity) {
                    is GuruProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener {
                    e->
                when(activity) {
                    is GuruProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while update a profile",
                    e
                )
                Toast.makeText(activity,"ERROR while update a profile", Toast.LENGTH_LONG).show()
            }
    }

    fun addUpdateMateriList(activity: Activity, kelas: Kelas){
        val materiListHashMap = HashMap<String, Any>()
        materiListHashMap[Constants.MATERI_LIST] = kelas.materiList

        mFireStore.collection(Constants.KELAS)
            .document(kelas.documentId.toString())
            .update(materiListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully")
                when (activity) {
                    is MateriListActivity -> {
                        activity.addUpdateMateriListSuccess()
                    }
                    is MateriDetailsActivity -> {
                        activity.addUpdateMateriListSuccess()
                    }
                    is TugasActivity -> {
                        activity.addUpdateMateriListSuccess()
                    }
                    is JawabActivity -> {
                        activity.addUpdateMateriListSuccess()
                    }

                }
            }
            .addOnFailureListener {
                    exception ->
                when(activity) {
                    is MateriListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MateriDetailsActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName,"Error while updating TaskList")
            }
    }

    fun updateMateri(activity: MateriListActivity, kelasId: String, updatedMateri: Materi) {
        mFireStore.collection(Constants.KELAS)
            .document(kelasId)
            .get()
            .addOnSuccessListener { document ->
                val kelas = document.toObject(Kelas::class.java)
                kelas?.let {
                    val updatedMateriList = it.materiList.map { materi ->
                        if (materi.id == updatedMateri.id) updatedMateri else materi
                    }
                    it.materiList = updatedMateriList as ArrayList<Materi>

                    mFireStore.collection(Constants.KELAS)
                        .document(kelasId)
                        .set(it)
                        .addOnSuccessListener {
                            activity.materiUpdateSuccess()
                        }
                        .addOnFailureListener { e ->
                            activity.hideProgressDialog()
                            Log.e(activity.javaClass.simpleName, "Error updating materi", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error fetching kelas details", e)
            }
    }

    fun updateMateriDetail(activity: Activity, kelasId: String, updatedMateri: Materi) {
        mFireStore.collection(Constants.KELAS)
            .document(kelasId)
            .get()
            .addOnSuccessListener { document ->
                val kelas = document.toObject(Kelas::class.java)
                kelas?.let {
                    val updatedMateriList = it.materiList.map { materi ->
                        if (materi.id == updatedMateri.id) updatedMateri else materi
                    }
                    it.materiList = updatedMateriList as ArrayList<Materi>

                    mFireStore.collection(Constants.KELAS)
                        .document(kelasId)
                        .set(it)
                        .addOnSuccessListener {
                            when (activity) {
                                is MateriDetailsActivity -> {
                                    activity.materiUpdateSuccess()
                                }
                                is TugasActivity -> {
                                    activity.materiUpdateSuccess()
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            when (activity) {
                                is MateriDetailsActivity -> {
                                    activity.hideProgressDialog()
                                }
                                is TugasActivity -> {
                                    activity.hideProgressDialog()
                                }
                            }

                            Log.e(activity.javaClass.simpleName, "Error updating materi", e)
                        }
                }
            }
    }

    fun deleteMateri(activity: MateriListActivity, materiId: String) {
        mFireStore.collection(Constants.KELAS)
            .document(activity.mKelasDocumentId)
            .get()
            .addOnSuccessListener { document ->
                val kelas = document.toObject(Kelas::class.java)
                kelas?.let {
                    val updatedMateriList = it.materiList.filter { materi -> materi.id != materiId }
                    it.materiList = updatedMateriList as ArrayList<Materi>

                    mFireStore.collection(Constants.KELAS)
                        .document(activity.mKelasDocumentId)
                        .set(it)
                        .addOnSuccessListener {
                            activity.materiDeleteSuccess()
                        }
                        .addOnFailureListener { e ->
                            activity.hideProgressDialog()
                            Log.e(activity.javaClass.simpleName, "Error deleting materi", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error fetching kelas details", e)
            }
    }

    fun updateTugasInMateri(
        activity: TugasActivity,
        kelasDocumentId: String,
        materiPosition: Int,
        tugasPosition: Int,
        updatedTugas: Tugas
    ) {
        mFireStore.collection(Constants.KELAS)
            .document(kelasDocumentId)
            .get()
            .addOnSuccessListener { document ->
                val kelas = document.toObject(Kelas::class.java)
                kelas?.let {
                    it.materiList[materiPosition].tugas[tugasPosition] = updatedTugas
                    mFireStore.collection(Constants.KELAS)
                        .document(kelasDocumentId)
                        .set(it, SetOptions.merge())
                        .addOnSuccessListener {
                            activity.tugasUpdateSuccess()
                        }
                        .addOnFailureListener { e ->
                            activity.hideProgressDialog()
                            Log.e(activity.javaClass.simpleName, "Error while updating tugas", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting kelas details", e)
            }
    }

    fun deleteJawabTugas(
        activity: TugasActivity,
        kelasDocumentId: String,
        materiPosition: Int,
        tugasPosition: Int,
        jawabId: String
    ) {
        mFireStore.collection(Constants.KELAS).document(kelasDocumentId)
            .get()
            .addOnSuccessListener { document ->
                val kelasDetails = document.toObject(Kelas::class.java)
                kelasDetails?.let { kelas ->
                    if (materiPosition < kelas.materiList.size &&
                        tugasPosition < kelas.materiList[materiPosition].tugas.size) {

                        val currentTugas = kelas.materiList[materiPosition].tugas[tugasPosition]
                        val updatedJawabList = currentTugas.jawab.filter { it.id != jawabId }

                        // Create a new Tugas object with the updated jawab list
                        val updatedTugas = currentTugas.copy(jawab = ArrayList(updatedJawabList))

                        // Update the Tugas in the Materi
                        kelas.materiList[materiPosition].tugas[tugasPosition] = updatedTugas

                        // Update the entire Kelas object in Firestore
                        mFireStore.collection(Constants.KELAS)
                            .document(kelasDocumentId)
                            .set(kelas)
                            .addOnSuccessListener {
                                activity.jawabTugasDeleteSuccess()
                            }
                            .addOnFailureListener { e ->
                                activity.hideProgressDialog()
                                Log.e(activity.javaClass.simpleName, "Error while deleting jawab tugas", e)
                            }
                    } else {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Invalid materi or tugas position")
                    }
                } ?: run {
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Kelas details are null")
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while fetching kelas details", e)
            }
    }

    fun updateJawabInMateri(
        activity: JawabActivity,
        kelasDocumentId: String,
        materiPosition: Int,
        tugasPosition: Int,
        jawabPosition: Int,
        updatedJawab: JawabanTugas
    ) {
        mFireStore.collection(Constants.KELAS)
            .document(kelasDocumentId)
            .get()
            .addOnSuccessListener { document ->
                val kelas = document.toObject(Kelas::class.java)
                kelas?.let {
                    it.materiList[materiPosition].tugas[tugasPosition].jawab[jawabPosition] = updatedJawab
                    mFireStore.collection(Constants.KELAS)
                        .document(kelasDocumentId)
                        .set(it, SetOptions.merge())
                        .addOnSuccessListener {
                            activity.jawabUpdateSuccess()
                        }
                        .addOnFailureListener { e ->
                            activity.hideProgressDialog()
                            Log.e(activity.javaClass.simpleName, "Error while updating tugas", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting kelas details", e)
            }
    }

}