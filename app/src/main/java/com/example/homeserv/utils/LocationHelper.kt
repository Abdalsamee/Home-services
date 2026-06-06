package com.example.homeserv.utils



import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    // ── Permission check ──────────────────────────────────────────

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    // ── Get current location ──────────────────────────────────────

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getCurrentLocation(): LatLng? {
        if (!hasLocationPermission()) return null
        return try {
            val cts = CancellationTokenSource()
            val loc: Location = fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .await()
            LatLng(loc.latitude, loc.longitude)
        } catch (e: Exception) {
            // Fall back to last known location
            getLastKnownLocation()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getLastKnownLocation(): LatLng? {
        if (!hasLocationPermission()) return null
        return try {
            val loc = fusedClient.lastLocation.await() ?: return null
            LatLng(loc.latitude, loc.longitude)
        } catch (e: Exception) {
            null
        }
    }

    // ── Distance calculation ──────────────────────────────────────

    fun distanceBetween(from: LatLng, to: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude,   to.longitude,
            results
        )
        return results[0] / 1000.0   // return km
    }
}