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
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.databinding.ItemKelasBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas

class KelasItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Kelas>
) : RecyclerView.Adapter<KelasItemsAdapter.KelasViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private var onEditClickListener: OnEditClickListener? = null
    private var onDeleteClickListener: OnDeleteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelasViewHolder {
        val binding = ItemKelasBinding.inflate(LayoutInflater.from(context), parent, false)
        return KelasViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KelasViewHolder, position: Int) {
        val model = list[position]

        holder.binding.tvName.text = model.nama

        holder.itemView.setOnClickListener {
            val index = list.indexOf(model)
            if (onClickListener != null && index != -1) {
                onClickListener!!.onClick(index, model)
            }
        }

        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    holder.binding.btnMore.visibility = View.GONE
                }
            }
        }

        holder.binding.btnMore.setOnClickListener {
            showOptionsDialog(position, model)
        }
    }

    private fun showOptionsDialog(position: Int, kelas: Kelas) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_more_materi, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvEdit = dialogView.findViewById<TextView>(R.id.tv_edit)
        val tvDelete = dialogView.findViewById<TextView>(R.id.tv_delete)

        tvDialogTitle.text = "Pilih Aksi"
        tvEdit.text = "Edit"
        tvDelete.text = "Delete"

        tvEdit.setOnClickListener {
            onEditClickListener?.onEditClick(position, kelas)
            dialog.dismiss()
        }

        tvDelete.setOnClickListener {
            onDeleteClickListener?.onDeleteClick(position, kelas)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = list.size

    interface OnClickListener {
        fun onClick(position: Int, model: Kelas)
    }

    interface OnEditClickListener {
        fun onEditClick(position: Int, model: Kelas)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int, model: Kelas)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun setOnEditClickListener(listener: OnEditClickListener) {
        this.onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.onDeleteClickListener = listener
    }


    inner class KelasViewHolder(val binding: ItemKelasBinding) :
        RecyclerView.ViewHolder(binding.root)
}