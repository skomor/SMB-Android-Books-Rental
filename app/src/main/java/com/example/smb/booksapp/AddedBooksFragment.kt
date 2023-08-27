package com.example.smb.booksapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smb.booksapp.databinding.FragmentAddedBooksBinding
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import com.example.smb.booksapp.viewmodels.main.MainViewModelFactory
import kotlinx.coroutines.NonCancellable.cancel

class AddedBooksFragment : Fragment(), BooksAdapter.OnItemClickListener
{
	private var _binding: FragmentAddedBooksBinding? = null
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
		_binding = FragmentAddedBooksBinding.inflate(inflater, container, false)

		val postsRecyclerView: RecyclerView = binding.recyclerView2
		postsRecyclerView.layoutManager = LinearLayoutManager(context)
		postsRecyclerView.adapter = adapter


		mainViewModel.getUserAddedBooks()
		mainViewModel.userBooks.observe(viewLifecycleOwner, Observer {
			adapter.bookItems = it
			adapter.notifyDataSetChanged()
		})

		return binding.root
	}

	override fun onItemClick(position: Int)
	{
		Toast.makeText(context, "Item $position clicked", Toast.LENGTH_SHORT).show()
		val clickedItem = mainViewModel.userBooks.value?.get(position)
		if (clickedItem != null)
		{
			val alertDialog: AlertDialog? = activity?.let {
				val builder = AlertDialog.Builder(it)
				builder.apply {
					setPositiveButton("Ok",
						DialogInterface.OnClickListener { dialog, id ->
							mainViewModel.removeUserBook(clickedItem) { it ->
								if (it is com.example.smb.booksapp.data.Result.Success)
								{
									mainViewModel.getUserAddedBooks()
								}
							}
							dialog.dismiss()
						})
					setNegativeButton("Cancel",
						DialogInterface.OnClickListener { dialog, id ->
							dialog.dismiss()
						})
					setMessage("Are you sure you want to delete this item?")
				}

				builder.create()
			}
			alertDialog?.show()

		}
	}
}