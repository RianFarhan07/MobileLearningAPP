import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.models.JawabanKuis
import com.example.mobilelearningapp.models.Question

class JawabKuisItemsAdapter(
    private val questions: List<Question>,
    private val onAnswerSelected: (questionId: Int, selectedAnswer: Int) -> Unit
) : RecyclerView.Adapter<JawabKuisItemsAdapter.QuizViewHolder>() {

    private val answers = mutableMapOf<Int, Int>()

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestion: TextView = itemView.findViewById(R.id.tv_question)
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        private val tvOptionOne: TextView = itemView.findViewById(R.id.tv_option_one)
        private val tvOptionTwo: TextView = itemView.findViewById(R.id.tv_option_two)
        private val tvOptionThree: TextView = itemView.findViewById(R.id.tv_option_three)
        private val tvOptionFour: TextView = itemView.findViewById(R.id.tv_option_four)

        fun bind(question: Question) {
            tvQuestion.text = question.question

            if (question.image.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(question.image)
                    .into(ivImage)
                ivImage.visibility = View.VISIBLE
            } else {
                ivImage.visibility = View.GONE
            }

            tvOptionOne.text = question.optionOne
            tvOptionTwo.text = question.optionTwo
            tvOptionThree.text = question.optionThree
            tvOptionFour.text = question.optionFour

            val options = listOf(tvOptionOne, tvOptionTwo, tvOptionThree, tvOptionFour)
            options.forEachIndexed { index, textView ->
                textView.setOnClickListener {
                    selectOption(question.id, index + 1, options)
                }
            }

            // Restore previously selected answer
            answers[question.id]?.let { selectedAnswer ->
                options[selectedAnswer - 1].setBackgroundResource(R.drawable.selected_option_border_bg)
            }
        }

        private fun selectOption(questionId: Int, selectedAnswer: Int, options: List<TextView>) {
            options.forEach { it.setBackgroundResource(R.drawable.default_option_border_bg) }
            options[selectedAnswer - 1].setBackgroundResource(R.drawable.selected_option_border_bg)
            answers[questionId] = selectedAnswer
            onAnswerSelected(questionId, selectedAnswer)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kuis_soal, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount() = questions.size

    fun getAnswers(): List<JawabanKuis> {
        return answers.map { (questionId, selectedAnswer) -> JawabanKuis(questionId, selectedAnswer) }
    }
}