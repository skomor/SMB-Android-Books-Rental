package com.example.smb.booksapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AudioAdapter (private val listener: OnItemClickListener
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    var audioItems: MutableList<Pair<String, Boolean>> = mutableListOf();

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.audioName)
        val play: Button = itemView.findViewById(R.id.play)
        val star: Button = itemView.findViewById(R.id.star)
        val pause: Button = itemView.findViewById(R.id.pause)

        init {
            play.setOnClickListener{
                val position = adapterPosition;
                listener.onPlayClick(position);
            }
            star.setOnClickListener{
                val position = adapterPosition;
                listener.onStarClick(position);
            }
            pause.setOnClickListener{
                val position = adapterPosition;
                listener.onPauseClick(position);
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.audio_item,
            parent, false)
        return AudioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val currentItem = audioItems[position]
        holder.name.setText(currentItem.first)
        if(currentItem.second == true)
            holder.star.setBackgroundColor(
                ContextCompat.getColor(
                    holder.star.context,
                    R.color.design_default_color_error
                )
            )
    }

    override fun getItemCount() = audioItems.count()

    interface OnItemClickListener {
        fun onPlayClick(position: Int)
        fun onStarClick(position: Int)
        fun onPauseClick(position: Int)
    }
}