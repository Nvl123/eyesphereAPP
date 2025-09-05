package com.dicoding.eyesphere_nav.ui.History

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.data.database.ClassificationResult
import com.dicoding.eyesphere_nav.ui.detail.DetailItemActivity
import java.io.File

class HistoryAdapter(private var classificationResults: List<ClassificationResult>) : 
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val result = classificationResults[position]
        holder.bind(result)
    }

    override fun getItemCount(): Int = classificationResults.size

    fun updateData(newResults: List<ClassificationResult>) {
        classificationResults = newResults
        notifyDataSetChanged()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        private val classificationTextView: TextView = itemView.findViewById(R.id.tv_classification)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_date)
        private val timeTextView: TextView = itemView.findViewById(R.id.tv_time)
        private val confidenceTextView: TextView = itemView.findViewById(R.id.tv_confidence)
        private val statusIndicator: View = itemView.findViewById(R.id.view_status_indicator)

        fun bind(result: ClassificationResult) {
            // Set classification and confidence based on status
            val statusColor = when (result.status) {
                com.dicoding.eyesphere_nav.data.database.ProcessingStatus.PENDING -> {
                    classificationTextView.text = "Menunggu..."
                    confidenceTextView.text = "Antrian"
                    val color = ContextCompat.getColor(itemView.context, android.R.color.holo_blue_dark)
                    statusIndicator.setBackgroundColor(color)
                    color
                }
                com.dicoding.eyesphere_nav.data.database.ProcessingStatus.PROCESSING -> {
                    classificationTextView.text = "Memproses..."
                    confidenceTextView.text = "${result.progress}%"
                    val color = ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark)
                    statusIndicator.setBackgroundColor(color)
                    color
                }
                com.dicoding.eyesphere_nav.data.database.ProcessingStatus.COMPLETED -> {
                    classificationTextView.text = result.classification
                    val confidencePercent = (result.confidence * 100).toInt()
                    confidenceTextView.text = "$confidencePercent%"
                    val color = when {
                        result.confidence >= 0.8f -> ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
                        result.confidence >= 0.6f -> ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark)
                        else -> ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
                    }
                    statusIndicator.setBackgroundColor(color)
                    color
                }
                com.dicoding.eyesphere_nav.data.database.ProcessingStatus.FAILED -> {
                    classificationTextView.text = "Gagal diproses"
                    confidenceTextView.text = "Error"
                    val color = ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
                    statusIndicator.setBackgroundColor(color)
                    color
                }
            }
            
            // Set date and time
            dateTextView.text = result.date
            timeTextView.text = result.time
            
            // Load thumbnail image
            if (File(result.imagePath).exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(result.imagePath)
                    thumbnailImageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    thumbnailImageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                thumbnailImageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
            
            // Set confidence text color
            confidenceTextView.setTextColor(    statusColor)
            
            // Set click listener to open detail activity
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailItemActivity::class.java).apply {
                    // Send only ID to avoid TransactionTooLargeException
                    putExtra("result_id", result.id)
                    putExtra("image_path", result.imagePath)
                    putExtra("classification", result.classification)
                    putExtra("confidence", result.confidence)
                    putExtra("date", result.date)
                    putExtra("time", result.time)
                }
                context.startActivity(intent)
            }
        }
    }
}