package com.example.homeserv.ui.customer.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.homeserv.databinding.FragmentMapBinding
import com.example.homeserv.utils.LocationHelper
import com.example.homeserv.utils.show
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MapFragment : BaseFragment<FragmentMapBinding>(), OnMapReadyCallback {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMapBinding.inflate(inflater, container, false)

    private var googleMap: GoogleMap? = null
    private lateinit var locationHelper: LocationHelper

    private var providerLat  = 0.0
    private var providerLng  = 0.0
    private var providerName = ""

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) setupUserLocation()
            else binding.layoutPermission.show()
        }

    override fun setup() {
        locationHelper = LocationHelper(requireContext())

        providerLat  = arguments?.getFloat("providerLat")?.toDouble()  ?: 0.0
        providerLng  = arguments?.getFloat("providerLng")?.toDouble()  ?: 0.0
        providerName = arguments?.getString("providerName") ?: "Provider"

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvTitle.text = providerName
        binding.btnGrantPermission.setOnClickListener {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.apply {
            isZoomControlsEnabled     = true
            isCompassEnabled          = true
            isMyLocationButtonEnabled = false
        }

        val providerLatLng = LatLng(providerLat, providerLng)
        map.addMarker(
            MarkerOptions()
                .position(providerLatLng)
                .title(providerName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(providerLatLng, 14f))

        binding.cardInfo.show()
        binding.tvProviderName.text = providerName

        // Opens Google Maps app for turn-by-turn navigation
        binding.btnDirections.setOnClickListener {
            val uri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                        "&destination=$providerLat,$providerLng" +
                        "&travelmode=driving"
            )
            startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }.let { intent ->
                // Fallback to browser if Maps not installed
                if (intent.resolveActivity(requireActivity().packageManager) != null)
                    intent
                else
                    Intent(Intent.ACTION_VIEW, uri)
            })
        }

        if (locationHelper.hasLocationPermission()) setupUserLocation()
        else binding.layoutPermission.show()
    }

    private fun setupUserLocation() {
        binding.layoutPermission.visibility = View.GONE
        CoroutineScope(Dispatchers.Main).launch {
            val userLocation = withContext(Dispatchers.IO) {
                locationHelper.getCurrentLocation()
            } ?: return@launch

            val providerLatLng = LatLng(providerLat, providerLng)

            googleMap?.let { map ->
                // User marker
                map.addMarker(
                    MarkerOptions()
                        .position(userLocation)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )

                // Distance
                val distKm   = locationHelper.distanceBetween(userLocation, providerLatLng)
                val distText = if (distKm < 1.0)
                    "${(distKm * 1000).toInt()} m away"
                else "%.1f km away".format(distKm)
                binding.tvDistance.text     = distText
                binding.tvRouteSummary.text = distText
                binding.tvRouteSummary.show()

                // Try to draw real route via Directions API
                val routePoints = withContext(Dispatchers.IO) {
                    fetchRoutePoints(userLocation, providerLatLng)
                }

                if (routePoints.isNotEmpty()) {
                    // Draw actual road route
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(routePoints)
                            .width(8f)
                            .color(ContextCompat.getColor(requireContext(), R.color.primary))
                            .geodesic(true)
                    )
                } else {
                    // Fallback: draw straight line
                    map.addPolyline(
                        PolylineOptions()
                            .add(userLocation, providerLatLng)
                            .width(6f)
                            .color(ContextCompat.getColor(requireContext(), R.color.primary))
                    )
                }

                // Zoom to fit both markers
                val bounds = LatLngBounds.Builder()
                    .include(userLocation)
                    .include(providerLatLng)
                    .build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 160))

                // Enable my location layer
                if (ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) map.isMyLocationEnabled = true
            }
        }
    }

    /**
     * Fetches route polyline points from Google Directions API.
     * Returns empty list if API key not set or request fails —
     * caller falls back to straight line.
     */
    private fun fetchRoutePoints(origin: LatLng, dest: LatLng): List<LatLng> {
        return try {
            val apiKey = getString(R.string.google_maps_key)
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${dest.latitude},${dest.longitude}" +
                    "&mode=driving" +
                    "&key=$apiKey"

            val response = URL(url).readText()
            val json     = JSONObject(response)
            val status   = json.getString("status")

            if (status != "OK") return emptyList()

            val route    = json.getJSONArray("routes").getJSONObject(0)
            val leg      = route.getJSONArray("legs").getJSONObject(0)
            val steps    = leg.getJSONArray("steps")
            val points   = mutableListOf<LatLng>()

            for (i in 0 until steps.length()) {
                val step         = steps.getJSONObject(i)
                val polyline     = step.getJSONObject("polyline").getString("points")
                points.addAll(decodePolyline(polyline))
            }
            points
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Decodes a Google encoded polyline string into LatLng points.
     */
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly  = mutableListOf<LatLng>()
        var index = 0
        var lat   = 0
        var lng   = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift  = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
}