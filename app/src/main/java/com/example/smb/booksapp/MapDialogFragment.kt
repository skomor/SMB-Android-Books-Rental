package com.example.smb.booksapp

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat


class MapDialogFragment(private val userInfoViewModel: UserInfoViewModel) :
    BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentSetMarkerSheetBinding
    val markers: MutableList<Marker> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetMarkerSheetBinding.inflate(inflater, container, false)
        val setlocationBtn = binding.SetLocationButton;
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
            val gcd = Geocoder(context)
            var lng = this.markers.last().position.longitude
            var lat = this.markers.last().position.latitude
            val addresses = gcd.getFromLocation(lat, lng, 1)
            if (addresses.size > 0 && addresses[0].locality != null  && addresses[0].postalCode != null) {
                userInfoViewModel.changeLocation(this.markers.last(),
                    addresses[0].locality + " " + addresses[0].postalCode)
            } else {
                userInfoViewModel.changeLocation(
                    this.markers.last(),
                    "Unknown0 address at: $lng, $lat"
                )
            }
            this.dialog?.dismiss()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        val warsaw = LatLng(52.237049, 21.017532)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true;
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 5F));
        val probablyMarker = googleMap.addMarker(
            MarkerOptions()
                .position(warsaw)
                .title("Warsaw")
        )
        if (probablyMarker != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(warsaw))
            markers.add(probablyMarker)
        }
        googleMap.setOnMapClickListener {
            for (marker in markers) {
                marker.remove()
            }
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(it)
            )
            if (marker != null) {
                markers.add(marker)
            }
        }
    }
}