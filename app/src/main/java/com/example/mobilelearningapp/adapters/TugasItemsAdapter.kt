package com.example.mobilelearningapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.activities.MateriDetailsActivity
import com.example.mobilelearningapp.databinding.ItemKelasBinding
import com.example.mobilelearningapp.databinding.ItemMateriBinding
import com.example.mobilelearningapp.databinding.ItemTugasBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

class TugasItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Tugas>
) : RecyclerView.Adapter<TugasItemsAdapter.TugasViewHolder>() {

    private var onClickListener: OnClickListener? = null
//    private var onEditClickListener: OnEditClickListener? = null
//    private var onDeleteClickListener: OnDeleteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        val binding = ItemTugasBinding.inflate(LayoutInflater.from(context), parent, false)
        return TugasViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TugasViewHolder, position: Int) {
        val model = list[position]

        holder.binding.tvNamaTugas.text = model.namaTugas
        holder.binding.tvKelas.text = "Kelas : ${model.namaKelas}"
        holder.binding.tvMateri.text = "Materi : ${model.namaMateri}"
        holder.binding.tvMapel.text = "Mata Pelajaran : ${model.namaMapel}"

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.timeInMillis = model.dueDate //
        val diffInMillis = dueDate.timeInMillis - currentDate.timeInMillis
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
        val diffInHours = (diffInMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)

        if (model.dueDate == 0L){
            holder.binding.tvSisaWaktu.text = "Belum ada tenggat waktu"
        }else{
            holder.binding.tvSisaWaktu.text = "Sisa waktu: $diffInDays hari $diffInHours jam"

            if (diffInMillis < 0) {
                holder.binding.tvSisaWaktu.setTextColor(ContextCompat.getColor(context, R.color.colorSnackBarError))
                holder.binding.tvSisaWaktu.text = "Waktu telah lewat"
            } else {
                holder.binding.tvSisaWaktu.setTextColor(ContextCompat.getColor(context, R.color.colorSnackBarSuccess))
                holder.binding.tvSisaWaktu.text = "Sisa waktu: $diffInDays hari $diffInHours jam"
            }

        }


        holder.itemView.setOnClickListener {
            val index = list.indexOf(model)
            if (onClickListener != null && index != -1) {
                onClickListener!!.onClick(index, model)
            }
        }

//        val currentUserID = FirestoreClass().getCurrentUserID()
//        if (currentUserID.isNotEmpty()) {
//            FirestoreClass().getUserRole(currentUserID) { role ->
//                if (role == "siswa") {
//                    holder.binding.btnMore.visibility = View.GONE
//                }
//            }
//        }

//        holder.binding.btnMore.setOnClickListener {
//            showOptionsDialog(position, model)
//        }
    }

//    private fun showOptionsDialog(position: Int, materi: Materi) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_more_materi, null)
//        val dialog = AlertDialog.Builder(context)
//            .setView(dialogView)
//            .create()
//
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
//        val tvEdit = dialogView.findViewById<TextView>(R.id.tv_edit)
//        val tvDelete = dialogView.findViewById<TextView>(R.id.tv_delete)
//
//        tvDialogTitle.text = "Pilih Aksi"
//        tvEdit.text = "Edit"
//        tvDelete.text = "Delete"
//
////        tvEdit.setOnClickListener {
////            showEditDialog(position, materi)
////            dialog.dismiss()
////        }
////
////        tvDelete.setOnClickListener {
////            onDeleteClickListener?.onDeleteClick(position, materi)
////            dialog.dismiss()
////        }
//
//        dialog.show()
//    }

    override fun getItemCount(): Int = list.size

    interface OnClickListener {
        fun onClick(position: Int, model: Tugas)
    }

//    interface OnEditClickListener {
//        fun onEditClick(position: Int, model: Materi)
//    }

//    private fun showEditDialog(position: Int, tugas: Tugas) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_materi, null)
//        val dialog = AlertDialog.Builder(context)
//            .setView(dialogView)
//            .create()
//
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//
//        val etMateri = dialogView.findViewById<EditText>(R.id.et_classses_name)
//        val tvEdit = dialogView.findViewById<TextView>(R.id.tv_edit_materi)
//        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
//
//
//        etMateri.setText(materi.nama)
//
//
//        tvEdit.setOnClickListener {
//            val newName = etMateri.text.toString()
//            if (newName.isNotEmpty()) {
//                onEditClickListener?.onEditClick(position, materi.copy(nama = newName))
//                dialog.dismiss()
//            }
//        }
//
//        tvCancel.setOnClickListener {
//            onDeleteClickListener?.onDeleteClick(position, Tugas)
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }
//
//    interface OnDeleteClickListener {
//        fun onDeleteClick(position: Int, model: Tugas)
//    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

//    fun setOnEditClickListener(listener: OnEditClickListener) {
//        this.onEditClickListener = listener
//    }
//
//    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
//        this.onDeleteClickListener = listener
//    }


    inner class TugasViewHolder(val binding: ItemTugasBinding) :
        RecyclerView.ViewHolder(binding.root)
}