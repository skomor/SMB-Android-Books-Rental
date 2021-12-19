package com.example.smb.booksapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smb.booksapp.databinding.FragmentSetMarkerSheetBinding
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.location.Geocoder


class MapDialogFragment(private val userInfoViewModel: UserInfoViewModel): BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentSetMarkerSheetBinding
    val markers:MutableList<Marker> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetMarkerSheetBinding.inflate(inflater, container, false)
        var setlocationBtn = binding.SetLocationButton;
        var mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = false
        setlocationBtn.setOnClickListener(saveAndDismiss())
        return binding.root
    }

    private fun saveAndDismiss(): View.OnClickListener? {
        return View.OnClickListener {
            val gcd = Geocoder( context)
            var lng = this.markers.first().position.longitude
            var lat = this.markers.first().position.latitude
            val addresses = gcd.getFromLocation(lat, lng, 1)
            if (addresses.size > 0) {
                userInfoViewModel.locationChange(this.markers.first(), addresses[0].locality)
            } else {
                userInfoViewModel.locationChange(this.markers.first(),
                    "Unknown address at: $lng, $lat"
                )
            }
            this.dialog?.dismiss()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        val sydney = LatLng(-33.852, 151.211)

        val probablyMarker =  googleMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
        if (probablyMarker != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
            markers.add(probablyMarker)
        }
        googleMap.setOnMapClickListener {
            for (marker in markers) {
                marker.remove()
            }
            val marker =  googleMap.addMarker(
                MarkerOptions()
                    .position(it)
            )
            if (marker != null) {
                markers.add(marker)
            }
        }
    }
}