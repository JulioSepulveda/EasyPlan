package com.juliosepulveda.easyplan.activities.newPlan

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure.LOCATION_MODE
import android.support.v4.app.ActivityCompat
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.juliosepulveda.easyplan.R
import com.juliosepulveda.easyplan.utils.toast
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*

@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, SearchView.OnQueryTextListener {

    private lateinit var gMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var geocoder: Geocoder

    private lateinit var locationManager: LocationManager
    private var currentLocation = ""

    private var localizacion = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        getExtras()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        geocoder = Geocoder(this, Locale.getDefault())

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (this.isGPSEnabled() == 0)
            showInfoAlert()
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                var location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location == null)
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (localizacion == "")
                    zoomToLocation(location!!.latitude, location.longitude)
                else
                    findLocation(localizacion)
            }
        }

        //Método para cuando se arrastra el marcador
        gMap.setOnMarkerDragListener(this)

        //Método para crear un marcador cuando se realiza una pulsada larga sobre el mapa
        gMap.setOnMapLongClickListener(this)

        //Método para cuando se realiza una busqueda
        svLocalizacion.setOnQueryTextListener(this)

        //Método cuando se pulse el botón para guardar la direccción
        btnSelect.setOnClickListener { returnNewPlan() }

    }

    override fun onMarkerDragEnd(marker: Marker?) {
        marker!!.title = getTitleMarker(marker.position.latitude, marker.position.longitude)
        zoomToLocation(marker.position.latitude, marker.position.longitude)
        marker.showInfoWindow()
    }

    override fun onMarkerDragStart(marker: Marker?) {
        marker!!.hideInfoWindow()
        svLocalizacion.setQuery("", false)
        svLocalizacion.clearFocus()
    }

    override fun onMarkerDrag(p0: Marker?) {}

    override fun onMapLongClick(direction: LatLng?) {
        createMarker(direction!!)
    }

    override fun onQueryTextSubmit(direction: String?): Boolean {
        if (!direction.equals("")) {
            findLocation(direction!!)

            //Ocultamos el teclado
            val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(svLocalizacion.windowToken, 0)

            //Quitamos el foco del searchView
            svLocalizacion.clearFocus()
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    private fun getExtras() {
        if ("" != intent.getStringExtra("localizacion")) {
            localizacion = intent.getStringExtra("localizacion")
        }
    }

    private fun isGPSEnabled(): Int {
        return try {
            Settings.Secure.getInt(this.contentResolver, LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            0
        }
    }

    private fun showInfoAlert() {
        AlertDialog.Builder(this)
                .setTitle(R.string.gps_signal)
                .setMessage(R.string.gps_disable)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(i)
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    private fun findLocation(location: String) {
        val addresses: List<Address>? = geocoder.getFromLocationName(location, 3)
        val address: Address?

        if (addresses != null && addresses.isNotEmpty()) {
            address = addresses[0]
            createMarker(LatLng(address.latitude, address.longitude))
        }
    }

    private fun createMarker(latLon: LatLng) {
        gMap.clear()

        val marker: Marker = gMap.addMarker(MarkerOptions()
                .position(latLon)
                .title(getTitleMarker(latLon.latitude, latLon.longitude))
                .visible(true)
                .draggable(true))

        marker.showInfoWindow()
        zoomToLocation(latLon.latitude, latLon.longitude)

    }

    private fun zoomToLocation(latitude: Double, longitude: Double) {
        val camera = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(15f)
                .bearing(0f)
                .tilt(30f)
                .build()
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera))

    }

    private fun getTitleMarker(latitude: Double, longitude: Double): String {
        val location = geocoder.getFromLocation(latitude, longitude, 1)[0].getAddressLine(0)
        val parts = location.split(",")
        var title = ""
        var titleOld = ""
        var flag = false


        for (text in parts) if (!flag) {
            if (title.length < 27) {
                titleOld = title

                title = if (title == "" && text.length >= 27) {
                    text.substring(0, 26)
                } else if (title == "" && text.length < 27)
                    text
                else
                    "$title, $text"
            }

            if (title.length > 27) {
                title = "$titleOld..."
                flag = true
            }
            else if (title.length == 27) {
                title = "$title..."
                flag = true
            }
        }
        currentLocation = title

        return title
    }

    private fun returnNewPlan() {
        if ("" != currentLocation) {
            val intent = intent
            intent.putExtra("location", currentLocation)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else
            toast(R.string.select_location, Toast.LENGTH_LONG)
    }
}
