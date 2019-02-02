package certh.hit.cmobile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import certh.hit.cmobile.location.GpsStatus
import certh.hit.cmobile.location.PermissionStatus
import certh.hit.cmobile.utils.Helper
import certh.hit.cmobile.viewmodel.MapViewModel
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import timber.log.Timber


/**
 * Created by anmpout on 21/01/2019
 */
class HomeActivity : AppCompatActivity(),OnMapReadyCallback,PermissionsListener  {


    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null
    private var style :Style? = null
    private val TAG: String = HomeActivity::class.java.canonicalName  as String
    private var viewModel: MapViewModel? = null
    private val gpsObserver = Observer<GpsStatus> { status ->
        status?.let {
            Log.d(TAG,status.toString())
           // updateGpsCheckUI(status)
        }
    }

    private val permissionObserver = Observer<PermissionStatus> { status ->
        status?.let {
         //   updatePermissionCheckUI(status)
            Log.d(TAG,status.toString())
            when (status) {

           //     is PermissionStatus.Granted -> handleGpsAlertDialog()
          //      is PermissionStatus.Denied -> showLocationPermissionNeededDialog()
            }
        }
    }

    private val lastLocationObserver = Observer<Location> { lastLocation ->
        lastLocation?.let {
            Log.d(TAG,lastLocation.toString())
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, Helper.mapsKey)
        setContentView(R.layout.activity_home)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapView?.getMapAsync(this)

//        mapView?.getMapAsync { mapboxMap ->
//            mapboxMap.setStyle(Style.DARK) {
//                for (singleLayer in it.layers) {
//                   // Log.d(TAG, "onMapReady: layer id = " + singleLayer.id)
//                }
//                Log.d(TAG, "onMapReady: layer id = " + it.layers.get(0).id)
//                mapboxMap.style?.getLayer("water")?.setProperties(PropertyFactory.fillColor(Color.parseColor("#0e6001")))
//
//                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
//            }
//        }

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        Log.d(TAG, "onMapReady:")
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.DARK){

            this.style = style

           enableLocationComponent()
            subscribeToGpsListener()
            subscribeToLocationPermissionListener()
            subscribeToLocationUpdate()
        }
    }

    private fun subscribeToLocationUpdate() = viewModel?.lastLocation?.observe(this,lastLocationObserver)


    private fun subscribeToGpsListener() = viewModel?.gpsStatusLiveData?.observe(this, gpsObserver)

    private fun subscribeToLocationPermissionListener() =
        viewModel?.locationPermissionStatusLiveData?.observe(this, permissionObserver)

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent() {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val options = LocationComponentOptions.builder(this)
                .gpsDrawable(R.drawable.ic_cursor)
                .bearingTintColor(R.color.primary_blue)
                .accuracyAlpha(1.0f)
                .trackingGesturesManagement(true)
                .build()

            // Get an instance of the component
            val locationComponent = mapboxMap?.locationComponent
            // Activate with options
            locationComponent?.activateLocationComponent(this, mapboxMap?.style!!,options)
            locationComponent?.addOnCameraTrackingChangedListener(object : OnCameraTrackingChangedListener {
                override fun onCameraTrackingDismissed() {
                    Log.d(TAG,"dismiss")
                    // Tracking has been dismissed

                }

                override fun onCameraTrackingChanged(currentMode: Int) {
                    // CameraMode has been updated
                    Log.d(TAG,"change"+currentMode.toString())


                }
            })


            // Set the component's camera mode
            locationComponent?.cameraMode = CameraMode.TRACKING_GPS_NORTH

            // Set the component's render mode
            locationComponent?.renderMode = RenderMode.GPS
            locationComponent?.isLocationComponentEnabled = true
            val routeCoordinates = ArrayList<Point>()
            routeCoordinates.add(Point.fromLngLat(-118.394391, 33.397676))
            routeCoordinates.add(Point.fromLngLat(-100.370917, 20.391142))

// Create the LineString from the list of coordinates and then make a GeoJSON FeatureCollection so that we can add the line to our map as a layer.

            val lineString = LineString.fromLngLats(routeCoordinates)
            val featureCollection = FeatureCollection.fromFeatures(
                arrayOf(Feature.fromGeometry(lineString)))

            val geoJsonSource = GeoJsonSource("geojson-source", featureCollection)
            mapboxMap?.style!!.addSource(geoJsonSource)

        } else {

            permissionsManager = PermissionsManager(this)

            permissionsManager?.requestLocationPermissions(this)

        }
    }
    public override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
    Toast.makeText(this, "ddd", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent()
        } else {
            Toast.makeText(this, "dd", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

