package com.example.mobilelearningapp.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

data class TextSpan(val start: Int, val end: Int, val style: Int)

data class FormattedText(val text: String, val spans: List<TextSpan>)

class FormattedTextHandler {
    companion object {

        fun toggleStyle(spannable: Spannable, start: Int, end: Int, style: Int) {
            val spans = spannable.getSpans(start, end, StyleSpan::class.java)
            val existingSpan = spans.find { it.style == style }

            if (existingSpan != null) {
                spannable.removeSpan(existingSpan)
            } else {
                applyStyle(spannable, start, end, style)
            }
        }

        fun applyStyle(spannable: Spannable, start: Int, end: Int, style: Int) {
            spannable.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        fun getExistingSpans(text: Spannable): MutableList<TextSpan> {
            return text.getSpans(0, text.length, StyleSpan::class.java).map { span ->
                TextSpan(text.getSpanStart(span), text.getSpanEnd(span), span.style)
            }.toMutableList()
        }

        fun updateSpannableText(text: Spannable, spans: List<TextSpan>) {
            spans.forEach { span ->
                text.setSpan(StyleSpan(span.style), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        fun toJson(text: String, spans: List<TextSpan>): String {
            val formattedText = FormattedText(text, spans)
            return Gson().toJson(formattedText)
        }

        fun fromJson(json: String): FormattedText {
            return try {
                Gson().fromJson(json, object : TypeToken<FormattedText>() {}.type)
            } catch (e: JsonSyntaxException) {
                // Jika parsing gagal, kembalikan FormattedText dengan teks asli tanpa formatting
                FormattedText(json, emptyList())
            }
        }

        fun toSpannableString(formattedText: FormattedText): SpannableStringBuilder {
            val spannableString = SpannableStringBuilder(formattedText.text)
            formattedText.spans.forEach { span ->
                spannableString.setSpan(StyleSpan(span.style), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return spannableString
        }
    }
}