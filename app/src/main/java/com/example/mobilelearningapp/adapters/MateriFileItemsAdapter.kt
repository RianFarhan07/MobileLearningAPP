package com.example.mobilelearningapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ItemMateriFileBinding
import com.example.mobilelearningapp.models.File

class MateriFileItemsAdapter(
    private val context: Context,
    private var list: ArrayList<File>
) :
    RecyclerView.Adapter<MateriFileItemsAdapter.MateriFileViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriFileViewHolder {
        val binding = ItemMateriFileBinding.inflate(
            LayoutInflater.from(context), parent, false)
        return MateriFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MateriFileViewHolder, position: Int) {
        val model = list[position]

        if (holder is MateriFileViewHolder) {

            holder.binding.tvNamaMateri.text = model.name

            when (model.fileType) {
                "pdf" -> holder.binding.imageViewPdfLogo.setImageResource(R.drawable.pdf)
                "doc", "docx" -> holder.binding.imageViewPdfLogo.setImageResource(R.drawable.word)
                "ppt", "pptx" -> holder.binding.imageViewPdfLogo.setImageResource(R.drawable.ppt)
                else -> holder.binding.imageViewPdfLogo.setImageResource(R.drawable.pdf)
            }

            holder.itemView.setOnClickListener {
                val index = list.indexOf(model) // Dapatkan indeks model
                if (onClickListener != null && index != -1) {
                    onClickListener!!.onClick(index, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model: File)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

//    fun notifyEditItem(fragment: MateriFragment, position: Int) {
//        val intent = Intent(context, CreateMateriActivity::class.java)
//        intent.putExtra(Constants.MATERI_ID, list[position].id)
//        fragment.startActivityForResult(intent, MateriFragment.EDIT_MATERI_REQUEST_CODE)
//        notifyItemChanged(position)
//    }

    fun notifySearchItem(list: ArrayList<File>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class MateriFileViewHolder(val binding: ItemMateriFileBinding) :
        RecyclerView.ViewHolder(binding.root)
}
