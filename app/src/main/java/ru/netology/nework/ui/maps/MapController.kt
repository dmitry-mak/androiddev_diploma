package ru.netology.nework.ui.maps

import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ru.netology.nework.dto.CoordinatesDto


private const val MAP_TAG = "detail_map"
private const val ZOOM = 12f

fun Fragment.showDetailMap(container: FrameLayout, coords: CoordinatesDto?) {
    if (coords == null) {
        container.isVisible = false
        return
    }
    container.isVisible = true
    val position = LatLng(coords.lat, coords.lng)

    var mapFragment = childFragmentManager.findFragmentByTag(MAP_TAG) as? SupportMapFragment
    if (mapFragment == null) {
        val options = GoogleMapOptions()
            .camera(cameraStngs(position))
            .scrollGesturesEnabled(false)
            .zoomGesturesEnabled(false)
            .rotateGesturesEnabled(false)
            .tiltGesturesEnabled(false)
            .zoomControlsEnabled(false)
            .compassEnabled(false)
            .mapToolbarEnabled(false)
        mapFragment = SupportMapFragment.newInstance(options)
        childFragmentManager.commit {
            add(container.id, mapFragment, MAP_TAG)
        }
    }
    mapFragment.getMapAsync { map ->
        map.clear()
        map.addMarker(MarkerOptions().position(position))
        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(cameraStngs(position))
        )
    }
}

private fun cameraStngs(position: LatLng): CameraPosition =
    CameraPosition.Builder()
        .target(position)
        .zoom(ZOOM)
        .build()