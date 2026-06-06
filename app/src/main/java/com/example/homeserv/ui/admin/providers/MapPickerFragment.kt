package com.example.homeserv.ui.admin.providers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.homeserv.base.BaseFragment
import com.homeserv.databinding.FragmentMapPickerBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.homeserv.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapPickerFragment : BaseFragment<FragmentMapPickerBinding>(), OnMapReadyCallback {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMapPickerBinding.inflate(inflater, container, false)

    private var pickedLat = 0.0
    private var pickedLng = 0.0
    private var googleMap: GoogleMap? = null

    override fun setup() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConfirm.setOnClickListener {
            // Pass result back as Double for precision
            findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.set("picked_lat", pickedLat)
            findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.set("picked_lng", pickedLng)
            findNavController().navigateUp()
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapPickerView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled      = true

        // Read pre-selected location if editing
        val initLat = arguments?.getFloat("initLat")?.toDouble() ?: 0.0
        val initLng = arguments?.getFloat("initLng")?.toDouble() ?: 0.0

        val startLat = if (initLat != 0.0) initLat else 31.9539
        val startLng = if (initLng != 0.0) initLng else 35.9106
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(startLat, startLng), 12f))

        if (initLat != 0.0) {
            pickedLat = initLat
            pickedLng = initLng
            map.addMarker(MarkerOptions().position(LatLng(initLat, initLng)))
            binding.tvCoords.text    = "📍 %.5f, %.5f".format(initLat, initLng)
            binding.btnConfirm.isEnabled = true
        }

        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Provider Location"))
            pickedLat = latLng.latitude
            pickedLng = latLng.longitude
            binding.tvCoords.text        = "📍 %.5f, %.5f".format(pickedLat, pickedLng)
            binding.btnConfirm.isEnabled = true
        }
    }
}