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
    import androidx.recyclerview.widget.DiffUtil
    import androidx.recyclerview.widget.RecyclerView
    import com.example.mobilelearningapp.activities.MateriDetailsActivity
    import com.example.mobilelearningapp.databinding.ItemKelasBinding
    import com.example.mobilelearningapp.databinding.ItemMateriBinding
    import com.example.mobilelearningapp.firebase.FirestoreClass
    import com.example.mobilelearningapp.models.Kelas
    import com.example.mobilelearningapp.models.Materi
    import com.example.mobilelearningapp.utils.Constants

    class MateriItemsAdapter(
        private val context: Context,
        private var list: ArrayList<Materi>
    ) : RecyclerView.Adapter<MateriItemsAdapter.MateriViewHolder>() {

        private var onClickListener: ((Materi) -> Unit)? = null
        private var onEditClickListener: ((Materi) -> Unit)? = null
        private var onDeleteClickListener: ((Materi) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriViewHolder {
            val binding = ItemMateriBinding.inflate(LayoutInflater.from(context), parent, false)
            return MateriViewHolder(binding)
        }

        override fun onBindViewHolder(holder: MateriViewHolder, position: Int) {
            val model = list[position]

            holder.binding.apply {
                tvName.text = model.nama
                tvMapel.text = "Mata Pelajaran : ${model.mapel}"

                root.setOnClickListener {
                    onClickListener?.invoke(model)
                }

                btnMore.apply {
                    FirestoreClass().getUserRole(FirestoreClass().getCurrentUserID()) { role ->
                        visibility = if (role == "siswa") View.GONE else View.VISIBLE
                    }

                    setOnClickListener {
                        showOptionsDialog(model)
                    }
                }
            }
        }

        private fun showOptionsDialog(materi: Materi) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_more_materi, null)
            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialogView.apply {
                findViewById<TextView>(R.id.tvDialogTitle).text = "Pilih Aksi"
                findViewById<TextView>(R.id.tv_edit).apply {
                    text = "Edit"
                    setOnClickListener {
                        showEditDialog(materi)
                        dialog.dismiss()
                    }
                }
                findViewById<TextView>(R.id.tv_delete).apply {
                    text = "Delete"
                    setOnClickListener {
                        onDeleteClickListener?.invoke(materi)
                        dialog.dismiss()
                    }
                }
            }

            dialog.show()
        }

        private fun showEditDialog(materi: Materi) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_materi, null)
            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialogView.apply {
                val etMateri = findViewById<EditText>(R.id.et_classses_name).apply { setText(materi.nama) }
                val etCourse = findViewById<EditText>(R.id.et_classses_course).apply { setText(materi.mapel) }

                findViewById<TextView>(R.id.tv_edit_materi).setOnClickListener {
                    val newName = etMateri.text.toString()
                    val newCourse = etCourse.text.toString()
                    if (newName.isNotEmpty() && newCourse.isNotEmpty()) {
                        onEditClickListener?.invoke(materi.copy(nama = newName, mapel = newCourse))
                        dialog.dismiss()
                    }
                }

                findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
                    dialog.dismiss()
                }
            }

            dialog.show()
        }

        fun updateMateriList(newList: ArrayList<Materi>) {
            val diffResult = DiffUtil.calculateDiff(MateriDiffCallback(list, newList))
            list.clear()
            list.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }

        override fun getItemCount(): Int = list.size

        fun setOnClickListener(listener: (Materi) -> Unit) {
            this.onClickListener = listener
        }

        fun setOnEditClickListener(listener: (Materi) -> Unit) {
            this.onEditClickListener = listener
        }

        fun setOnDeleteClickListener(listener: (Materi) -> Unit) {
            this.onDeleteClickListener = listener
        }

        inner class MateriViewHolder(val binding: ItemMateriBinding) :
            RecyclerView.ViewHolder(binding.root)

        private class MateriDiffCallback(
            private val oldList: List<Materi>,
            private val newList: List<Materi>
        ) : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].id == newList[newItemPosition].id
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]
        }
    }