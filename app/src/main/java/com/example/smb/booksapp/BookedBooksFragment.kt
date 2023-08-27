package com.example.smb.booksapp

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smb.booksapp.databinding.FragmentAddedBooksBinding
import com.example.smb.booksapp.databinding.FragmentBookedBooksBinding
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import com.example.smb.booksapp.viewmodels.main.MainViewModelFactory

class BookedBooksFragment : Fragment(), BooksAdapter.OnItemClickListener
{
	private var _binding: FragmentBookedBooksBinding? = null
	private val binding get() = _binding!!

	private val mainViewModel: MainViewModel by activityViewModels {
		MainViewModelFactory(
			requireContext()
		)
	}
	private val adapter = BooksAdapter(this)

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		_binding = FragmentBookedBooksBinding.inflate(inflater, container, false)

		val postsRecyclerView: RecyclerView = binding.recyclerView3
		postsRecyclerView.layoutManager = LinearLayoutManager(context)
		postsRecyclerView.adapter = adapter

		mainViewModel.getUserBookedBooks()
		mainViewModel.userBookedBooks.observe(viewLifecycleOwner, Observer {
			adapter.bookItems = it
			adapter.notifyDataSetChanged()
		})

		return binding.root
	}

	override fun onItemClick(position: Int)
	{
		Toast.makeText(context, "Item $position clicked", Toast.LENGTH_SHORT).show()
		val clickedItem = mainViewModel.userBookedBooks.value?.get(position)
		if (clickedItem != null)
		{
			val alertDialog: AlertDialog? = activity?.let {
				val builder = AlertDialog.Builder(it)
				builder.apply {
					setPositiveButton("Ok",
						DialogInterface.OnClickListener { dialog, id ->
							mainViewModel.unbookBook(clickedItem) { it ->
								if (it is com.example.smb.booksapp.data.Result.Success)
								{
									mainViewModel.getUserBookedBooks()
								}
							}
							dialog.dismiss()
						})
					setNegativeButton("Cancel",
						DialogInterface.OnClickListener { dialog, id ->
							showNotification()
							dialog.dismiss()
						})
					setMessage("Unbook?")
				}

				builder.create()
			}
			alertDialog?.show()
		}
	}

	private fun showNotification()
	{
		var builder = NotificationCompat.Builder(requireContext(), "aosad")
			.setContentTitle("Aborted!")
			.setContentText("Broadcast worked")
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
		val mNotificationManager =
			context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		mNotificationManager.notify(1, builder.build())
	}
}