package certh.hit.cmobile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import certh.hit.cmobile.location.GpsStatus
import certh.hit.cmobile.location.PermissionStatus
import certh.hit.cmobile.model.IVIUserMessage
import certh.hit.cmobile.model.SPATUserMessage
import certh.hit.cmobile.model.UserMessage
import certh.hit.cmobile.model.VIVIUserMessage
import certh.hit.cmobile.service.LocationService
import certh.hit.cmobile.service.LocationServiceCallback
import certh.hit.cmobile.service.LocationServiceInterface
import certh.hit.cmobile.utils.ColorArcProgressBar
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
    private var locationIntent :Intent? = null
    private var locationSrv: LocationService? = null
    private var mLocationAdapter:LocationServiceInterface? = null
    private var  vIVIMessage :TextView? = null
    private var iviMessageParent :RelativeLayout? = null
    private var trafficLight :RelativeLayout? = null
    private var trafficLightRed :ImageView? = null
    private var trafficLightYellow :ImageView? = null
    private var trafficLightGreen :ImageView? = null
    private var  tf : Typeface? = null
    private var speedBar : ColorArcProgressBar? = null
    private var iviSing : ImageView? =null
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

    private val lastLocationObserver = Observer<UserMessage> { userMessage ->
        userMessage?.let {


        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, Helper.mapsKey)
        setContentView(R.layout.activity_home)
        setupUI()

        mapView?.onCreate(savedInstanceState)
        //viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapView?.getMapAsync(this)

//        mapView?.getMapAsync { mapboxMap ->
//            mapboxMap.setStyle(Style.DARK) {
//                for (singleLayer in it.layers) {
//                   // Log.d(TAG(TAG, "onMapReady: layer id = " + singleLayer.id)
//                }
//                Log.d(TAG(TAG, "onMapReady: layer id = " + it.layers.get(0).id)
//                mapboxMap.style?.getLayer("water")?.setProperties(PropertyFactory.fillColor(Color.parseColor("#0e6001")))
//
//                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
//            }
//        }

    }

    private fun setupUI() {
        mapView = findViewById(R.id.mapView)
        vIVIMessage =findViewById(R.id.vivi_message);
        tf = Typeface.createFromAsset(getAssets(),  "fonts/led.ttf");
        vIVIMessage!!.typeface =tf
        speedBar = findViewById(R.id.speed_bar)
        iviMessageParent = findViewById(R.id.vivi_message_parent)
        trafficLight = findViewById(R.id.traffic_light)
        trafficLightRed = findViewById(R.id.red_light)
        trafficLightYellow = findViewById(R.id.yellow_light)
        trafficLightGreen = findViewById(R.id.green_light)
        iviSing = findViewById(R.id.ivi_sign)

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        Log.d(TAG, "onMapReady:")
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.DARK){

            this.style = style


           enableLocationComponent()
            subscribeToGpsListener()
            subscribeToLocationPermissionListener()
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
                .gpsDrawable(R.drawable.ic_cursor_)
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
            locationComponent?.cameraMode = CameraMode.TRACKING_GPS

            // Set the component's render mode
            locationComponent?.renderMode = RenderMode.GPS
            locationComponent?.isLocationComponentEnabled = true

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
        if (mLocationAdapter == null) {
            initializeService()

        }
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
        if (mLocationAdapter != null) {
        unbindService(locationConnection)
        stopService(locationIntent)

    }
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

    inner class PlaybackListener : LocationServiceCallback() {
        override fun onIVIMessageReceived(message: IVIUserMessage) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onVIVIUserMessage(message: VIVIUserMessage) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSPATUserMessage(message: SPATUserMessage) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onPositionChanged(position: Int) {
            speedBar!!.setCurrentValues(position.toFloat())

        }


    }

    /**
     * Connect to the service
     *
     *
     */
    private val locationConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG,"onStart: create MediaPlayer")
            val binder = service as LocationService.LocationBinder
            //get service
            locationSrv = binder.service
            locationSrv!!.setPlaybackInfoListener(PlaybackListener())
            mLocationAdapter = locationSrv
            mLocationAdapter!!.setupNotification("","")
            Log.d(TAG,"connectied")


        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG,"onServiceDisconnected")
            mLocationAdapter = null
            //  musicBound = false;
        }
    }

    private fun initializeService() {
        if (locationIntent == null) {
            locationIntent = Intent(this, LocationService::class.java)
            bindService(locationIntent, locationConnection, Context.BIND_AUTO_CREATE)
        }
    }

}

