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
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.activities.MateriDetailsActivity
import com.example.mobilelearningapp.databinding.ItemKelasBinding
import com.example.mobilelearningapp.databinding.ItemMateriBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.utils.Constants

class TugasItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Materi>
) : RecyclerView.Adapter<TugasItemsAdapter.MateriViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private var onEditClickListener: OnEditClickListener? = null
    private var onDeleteClickListener: OnDeleteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriViewHolder {
        val binding = ItemMateriBinding.inflate(LayoutInflater.from(context), parent, false)
        return MateriViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MateriViewHolder, position: Int) {
        val model = list[position]

        holder.binding.tvName.text = model.nama

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MateriDetailsActivity::class.java)
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

    private fun showOptionsDialog(position: Int, materi: Materi) {
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
            showEditDialog(position, materi)
            dialog.dismiss()
        }

        tvDelete.setOnClickListener {
            onDeleteClickListener?.onDeleteClick(position, materi)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = list.size

    interface OnClickListener {
        fun onClick(position: Int, model: Materi)
    }

    interface OnEditClickListener {
        fun onEditClick(position: Int, model: Materi)
    }

    private fun showEditDialog(position: Int, materi: Materi) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_materi, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val etMateri = dialogView.findViewById<EditText>(R.id.et_classses_name)
        val tvEdit = dialogView.findViewById<TextView>(R.id.tv_edit_materi)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)


        etMateri.setText(materi.nama)


        tvEdit.setOnClickListener {
            val newName = etMateri.text.toString()
            if (newName.isNotEmpty()) {
                onEditClickListener?.onEditClick(position, materi.copy(nama = newName))
                dialog.dismiss()
            }
        }

        tvCancel.setOnClickListener {
            onDeleteClickListener?.onDeleteClick(position, materi)
            dialog.dismiss()
        }

        dialog.show()
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int, model: Materi)
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


    inner class MateriViewHolder(val binding: ItemMateriBinding) :
        RecyclerView.ViewHolder(binding.root)
}