package com.udacity.project4.locationreminders.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private val TAG = SelectLocationFragment::class.simpleName

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private val REQUEST_LOCAL_PERMISSION = 1

    /**
     * utility for checking permission has been granted or not
     */
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCAL_PERMISSION
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.selectLocationDone.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        // get current location
        val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var location: Location? = null

        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (location == null || loc.accuracy < location.accuracy) {
                location = loc
            }
        }
        return location
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        var home = LatLng(-34.0, 151.0)
        val location = getLastKnownLocation()
        if (location == null) {
            _viewModel.showErrorMessage.postValue("Can not load current location")
            Timber.d("Current location: null temporary set to Sydney")
        } else {
            home = LatLng(location.latitude, location.longitude)
        }
        Timber.d("Current location: ${home.latitude}, ${home.longitude}")
        val zoomLevel = 18f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoomLevel))
        // set on Poi Click listener
        setPoiClick(map)
        // set map style
        setMapStyle(map)
        // enable my location
        enableMyLocation()
    }

    /**
     * set listener for click on
     * Poi (point of interest)
     */
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            poiMarker?.showInfoWindow()
            // update selected location to viewModel
            _viewModel.updateSelectedPoi(poi)
        }
    }

    /**
     * set map style from json
     */
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object define in a raw resource file
            val success =
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireActivity(),
                        R.raw.map_style
                    )
                )
            if (!success) {
                Timber.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(e, TAG, "Can't find style. Error: ")
        }
    }

    /**
     * verify permission request response
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // check if location permissions are granted and if so enable the location data layer
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCAL_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    /**
     * action when user click fab done
     */
    private fun onLocationSelected() {
        // navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // set mapType to change appearance of Map
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
