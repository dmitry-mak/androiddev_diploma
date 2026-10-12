package ru.netology.nework.ui.maps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapCoordsPickerBinding


class MapPickerFragment : Fragment(R.layout.fragment_map_coords_picker) {

    private var _binding: FragmentMapCoordsPickerBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    private var selectedCoords: LatLng? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapCoordsPickerBinding.bind(view)

        val hasCoords = arguments?.getBoolean(ARG_HAS_COORDS) ?: false
        val initial = if (hasCoords) {
            LatLng(
                arguments?.getDouble(ARG_LAT) ?: 0.0,
                arguments?.getDouble(ARG_LONG) ?: 0.0
            )
        } else null

        binding.btnConfirm.isEnabled = initial != null
        binding.btnConfirm.setOnClickListener {
            val coords = selectedCoords ?: return@setOnClickListener
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putDouble(KEY_LAT, coords.latitude)
                    putDouble(KEY_LONG, coords.longitude)
                }
            )
            findNavController().navigateUp()
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.picker_map) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            if (!isAdded) return@getMapAsync
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = true
            map.setOnMapClickListener { latLng -> select(latLng) }
            if (initial != null) {
                select(initial)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(initial, 15f))
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER, 10f))
            }
        }
    }

    private fun select(latLng: LatLng) {
        selectedCoords = latLng
        val map = googleMap ?: return
        if (marker == null) {
            marker = map.addMarker(MarkerOptions().position(latLng))
        } else {
            marker?.position = latLng
        }
        binding.btnConfirm.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "map_picker_request"
        const val KEY_LAT = "lat"
        const val KEY_LONG = "lng"
        const val ARG_HAS_COORDS = "has_coords"
        const val ARG_LAT = "arg_lat"
        const val ARG_LONG = "arg_lng"
        private val DEFAULT_CENTER = LatLng(55.7558, 37.6173)
    }
}