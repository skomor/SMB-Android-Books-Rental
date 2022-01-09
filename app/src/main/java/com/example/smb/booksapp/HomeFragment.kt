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
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.smb.booksapp.data.model.FenceRequest
import com.example.smb.booksapp.databinding.FragmentHomeBinding
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModel
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModelFactory
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import com.example.smb.booksapp.viewmodels.main.MainViewModelFactory
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Constants

class HomeFragment : Fragment(), BooksAdapter.OnItemClickListener {

    private val geofenceList: MutableList<Geofence> = mutableListOf()
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private val adapter = BooksAdapter(this)
    private val model: UserInfoViewModel by activityViewModels { UserInfoViewModelFactory() }
    private val mainViewModel: MainViewModel by activityViewModels { MainViewModelFactory() }

    lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        val postsRecyclerView: RecyclerView = binding.recyclerView
        postsRecyclerView.layoutManager = LinearLayoutManager(context)
        postsRecyclerView.adapter = adapter
        mainViewModel.loadBooks()
        mainViewModel.books.observe(viewLifecycleOwner, Observer {
            adapter.bookItems = it
            adapter.notifyDataSetChanged()
        })
        model.userFormState.observe(viewLifecycleOwner, {
            Log.e("lol", "catched")
        })

        binding.fab.setOnClickListener { view ->
            val dialog = BottomAddItemDialog(this.mainViewModel)

            dialog.isCancelable = true
            dialog.show(parentFragmentManager, "lol")
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(context, "Item $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem = mainViewModel.books.value?.get(position)
        if (clickedItem != null) {
            if (clickedItem.lat != null && clickedItem.log != null)
                geofenceList.add(
                    Geofence.Builder()
                        .setRequestId(clickedItem?.bookId!!)
                        .setCircularRegion(
                            clickedItem.lat.toDouble(),
                            clickedItem.log.toDouble(), 3F
                        )
                        .setExpirationDuration(200)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()
                )
        }

        adapter.notifyItemChanged(position)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient?.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Log.e("GEO", "added")
                }
                addOnFailureListener {
                    Log.e("GEO", "failure")
                }
            }
        }


    }
    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }
}