package com.example.smb.booksapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smb.booksapp.data.model.Book
import com.example.smb.booksapp.data.model.Tag
import android.net.Uri;


class BooksAdapter (private val listener: OnItemClickListener
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    var bookItems: MutableList<Book> = mutableListOf();


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksAdapter.BookViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.book_item,
            parent, false)
        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BooksAdapter.BookViewHolder, position: Int) {
        val currentItem = bookItems[position]

        holder.imageView.setImageBitmap(currentItem.picBmap)
        holder.textView1.text = currentItem.author
        holder.textView2.text = currentItem.name
    }

    override fun getItemCount() = bookItems.count()

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val textView1: TextView = itemView.findViewById(R.id.text_view_1)
        val textView2: TextView = itemView.findViewById(R.id.text_view_2)

        init {
            itemView.setOnClickListener {
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