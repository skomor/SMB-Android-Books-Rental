package com.example.smb.booksapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smb.booksapp.databinding.FragmentHomeBinding
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModel
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModelFactory
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import com.example.smb.booksapp.viewmodels.main.MainViewModelFactory
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment(), BooksAdapter.OnItemClickListener
{

	private var _binding: FragmentHomeBinding? = null

	private val binding get() = _binding!!
	private val adapter = BooksAdapter(this)
	private val userInfoViewModel: UserInfoViewModel by activityViewModels { UserInfoViewModelFactory() }
	private val mainViewModel: MainViewModel by activityViewModels {
		MainViewModelFactory(
			requireContext()
		)
	}

	@SuppressLint("NotifyDataSetChanged")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		_binding = FragmentHomeBinding.inflate(inflater, container, false)

		val postsRecyclerView: RecyclerView = binding.recyclerView
		postsRecyclerView.layoutManager = LinearLayoutManager(context)
		postsRecyclerView.adapter = adapter

		mainViewModel.userTagsLoaded = {
			mainViewModel.loadBooksByTags()
		}

		mainViewModel.books.observe(viewLifecycleOwner, Observer {
			adapter.bookItems = it
			adapter.notifyDataSetChanged()
		})

		binding.floatingActionButton.setOnClickListener { view ->
			if (userInfoViewModel.userAdv?.logn == null && userInfoViewModel.userAdv?.lat == null)
			{
				Toast.makeText(
					this.context,
					"Please specify your location first in setting",
					Toast.LENGTH_LONG
				).show()
				return@setOnClickListener
			}
			val dialog = BottomAddItemDialog(this.mainViewModel, this.userInfoViewModel)
			dialog.isCancelable = true
			dialog.show(parentFragmentManager, "l=ol")
		}
		mainViewModel.reloadTags()

		return binding.root
	}

	override fun onItemClick(position: Int)
	{
		Toast.makeText(context, "Item $position clicked", Toast.LENGTH_SHORT).show()
		val clickedItem = mainViewModel.books.value?.get(position)
		if (clickedItem != null)
		{
			val dialog = BottomBookInfoFragment(clickedItem, mainViewModel)
			dialog.isCancelable = true
			dialog.show(parentFragmentManager, "lol")
		}
		adapter.notifyItemChanged(position)
	}
}