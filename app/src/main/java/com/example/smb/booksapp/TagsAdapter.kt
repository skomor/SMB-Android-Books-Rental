package com.example.smb.booksapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.example.smb.booksapp.data.model.Tag

class TagsAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {
    lateinit var postItems: MutableList<Tag>


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.tag_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        when (holder) {
            is TagViewHolder -> {
                holder.bind(postItems.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return postItems.size
    }

    inner class TagViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val postImageView: Button = itemView.findViewById(R.id.imagePost)

        fun bind(postItem: Tag) {
            postImageView.setText(postItem.name)
            if(!postItem.isUsers){
                postImageView.setBackgroundColor(getColor(postImageView.context, R.color.purple_200));
            }
            else{
                postImageView.setBackgroundColor(getColor(postImageView.context, R.color.teal_700));
            }
        }

        init {
            postImageView.setOnClickListener {
                this.onClick(itemView)
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition;
            if (position != RecyclerView.NO_POSITION)
                listener.onItemClick(position)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
