package com.example.smb.booksapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.smb.booksapp.data.model.Book
import com.example.smb.booksapp.databinding.FragmentBottomBookInfoBinding
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors

class BottomBookInfoFragment(private val book: Book, private val mainViewModel: MainViewModel) :
	BottomSheetDialogFragment(),
	OnMapReadyCallback
{
	private val permissions = arrayOf(
		Manifest.permission.ACCESS_BACKGROUND_LOCATION,
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.ACCESS_COARSE_LOCATION
	)


	private lateinit var binding: FragmentBottomBookInfoBinding
	private lateinit var mapView: MapView
	private var isMapViewVisible: Boolean = true
	private var executor = Executors.newSingleThreadExecutor()
	private var handler: Handler = Handler(Looper.getMainLooper())

	private val geofenceList: MutableList<Geofence> = mutableListOf()

	lateinit var geofencingClient: GeofencingClient

	private var requestPermissionLauncher =
		registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { isGranted: Boolean ->
			if (isGranted)
			{
				Log.i("Permission: ", "Granted")
			}
			else
			{
				Log.i("Permission: ", "Denied")
			}
		}

	private val geofencePendingIntent: PendingIntent by lazy {
		val intent = Intent(context, GeofenceReceiver::class.java)
		PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		geofencingClient = LocationServices.getGeofencingClient(requireContext())
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		binding.bookNameTextView.text = book.name
		binding.bookAuthorTextView.text = book.author
		if (book.pic != null)
		{
			executor.execute {
				val image: Bitmap? = getImageBitmap(book.pic)
				handler.post {
					binding.bookImage.setImageBitmap(image)
				}
			}
		}
		else
		{
			binding.bookImage.setImageBitmap(null)
		}
		binding.descriptionOfBook.text = "Description:" + book.description
		mapView = binding.mapView
		if (book.lat == null || book.log == null)
		{
			mapView.isVisible = false;
			isMapViewVisible = false
		}
		else
		{
			mapView.onCreate(savedInstanceState);
			mapView.getMapAsync(this)
		}

		binding.button.setOnClickListener {
			if (!checkPermissions()) return@setOnClickListener
			mainViewModel.setAvailabilityAndBooker(book, false) {
				Toast.makeText(view.context, "Book booked!", Toast.LENGTH_LONG).show()
				setGeofence(book.lat, book.log, book.bookId!!) {
					mainViewModel.reloadTags()
					this.dismiss();
				}
			}
		}

		val sheet = dialog?.findViewById<View>(R.id.design_bottom_sheet)
		sheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT;
	}

	private fun checkPermissions(): Boolean
	{
		if (ContextCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_BACKGROUND_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		)
		{
			requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
			return false
		}
		 if (ContextCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		)
		{
			requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
			return false
		}
		if (ContextCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		)
		{
			requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
			return false
		}
			return true

	}

	@SuppressLint("MissingPermission")
	private fun setGeofence(lat: String?, log: String?, bookId: String, successCallback: () -> Unit)
	{
		if (lat != null && log != null)
		{
			geofenceList.add(
				Geofence.Builder()
					.setRequestId(bookId)
					.setCircularRegion(
						lat.toDouble(),
						log.toDouble(), 300000F
					)
					.setExpirationDuration(2000000)
					.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
					.build()
			)
			geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
				addOnSuccessListener {
					Log.e("GEO", "added")
				}
				addOnFailureListener {

					Log.e("GEO", "failure" + it.message)
				}
			}
			successCallback.invoke()
		}
	}

	private fun getGeofencingRequest(): GeofencingRequest
	{
		return GeofencingRequest.Builder().apply {
			setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
			addGeofences(geofenceList)
		}.build()
	}

	private fun getImageBitmap(url: String): Bitmap?
	{
		var bm: Bitmap? = null
		try
		{
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

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		binding = FragmentBottomBookInfoBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onMapReady(p0: GoogleMap)
	{
		if (book.lat != null && book.log != null)
		{
			val location = LatLng(book.lat.toDouble(), book.log.toDouble())
			p0.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13F))
			p0.addMarker(
				MarkerOptions()
					.position(location)
					.title("Marker")
			)
		}
	}

	override fun onResume()
	{
		if (isMapViewVisible)
			mapView.onResume()
		super.onResume()
	}

	override fun onPause()
	{
		super.onPause()
		if (isMapViewVisible)
			mapView.onPause()
	}

	override fun onDestroy()
	{
		super.onDestroy()
		if (isMapViewVisible)
			mapView.onDestroy()
	}

	override fun onLowMemory()
	{
		super.onLowMemory()
		if (isMapViewVisible)
			mapView.onLowMemory()
	}
}