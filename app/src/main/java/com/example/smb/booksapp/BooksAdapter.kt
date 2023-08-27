package com.example.smb.booksapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL
import android.os.Looper

import java.util.concurrent.Executors

import java.util.concurrent.ExecutorService
import java.io.InputStream

import java.util.concurrent.Executor
import java.util.function.Consumer


class BooksAdapter(
	private val listener: OnItemClickListener
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>()
{

	var bookItems: MutableList<Book> = mutableListOf();
	private var executor = Executors.newSingleThreadExecutor()
	private var handler: Handler = Handler(Looper.getMainLooper())


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksAdapter.BookViewHolder
	{
		val itemView = LayoutInflater.from(parent.context).inflate(
			R.layout.book_item,
			parent, false
		)
		return BookViewHolder(itemView)
	}

	override fun onBindViewHolder(holder: BooksAdapter.BookViewHolder, position: Int)
	{
		val currentItem = bookItems[position]
		if (currentItem.pic != null)
		{
			executor.execute {
				val image: Bitmap? = getImageBitmap(currentItem.pic)
				handler.post {
					holder.imageView.setImageBitmap(image)
				}
			}
		}
		else
		{
			holder.imageView.setImageBitmap(null)
		}
		holder.textView1.text = currentItem.author
		holder.textView2.text = currentItem.name
	}

	override fun getItemCount() = bookItems.count()

	inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
		View.OnClickListener
	{

		val imageView: ImageView = itemView.findViewById(R.id.image_view)
		val textView1: TextView = itemView.findViewById(R.id.text_view_1)
		val textView2: TextView = itemView.findViewById(R.id.text_view_2)

		init
		{
			itemView.setOnClickListener {
				this.onClick(itemView)
			}
		}

		override fun onClick(v: View?)
		{
			val position = adapterPosition;
			if (position != RecyclerView.NO_POSITION)
				listener.onItemClick(position)
		}
	}

	interface OnItemClickListener
	{
		fun onItemClick(position: Int)
	}

	private fun getImageBitmap(url: String): Bitmap?
	{
		var bm: Bitmap? = null
		try
		{
			Log.e(ContentValues.TAG, "getting bitmap$url")
			val aURL = URL(url)
			val conn = aURL.openConnection()
			conn.connect()
			val `is` = conn.getInputStream()
			val bis = BufferedInputStream(`is`)
			bm = BitmapFactory.decodeStream(bis)
			bis.close()
			`is`.close()
		} catch (e: IOException)
		{
			Log.e(ContentValues.TAG, "Error getting bitmap", e)
		}
		return bm
	}
}