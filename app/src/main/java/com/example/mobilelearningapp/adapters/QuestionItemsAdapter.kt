package com.example.mobilelearningapp.adapters

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.models.Question

class QuestionItemsAdapter(
    private val questions: List<Question>,
    private val onItemClick: (Question) -> Unit
) : RecyclerView.Adapter<QuestionItemsAdapter.QuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.bind(question)
    }

    override fun getItemCount() = questions.size

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)

        fun bind(question: Question) {
            tvQuestion.text = question.question
            itemView.setOnClickListener { onItemClick(question) }
        }
    }


}