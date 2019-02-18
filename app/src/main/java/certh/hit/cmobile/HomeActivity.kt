package certh.hit.cmobile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import certh.hit.cmobile.location.GpsStatus
import certh.hit.cmobile.location.PermissionStatus
import certh.hit.cmobile.model.*
import certh.hit.cmobile.service.LocationService
import certh.hit.cmobile.service.LocationServiceCallback
import certh.hit.cmobile.service.LocationServiceInterface
import certh.hit.cmobile.utils.ColorArcProgressBar
import certh.hit.cmobile.utils.Helper
import certh.hit.cmobile.viewmodel.MapViewModel
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style


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
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapView?.getMapAsync(this)

    }

    private fun setupUI() {
        mapView = findViewById(R.id.mapView)
        vIVIMessage = findViewById(R.id.vivi_message);
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
           // subscribeToLocationPermissionListener()
        }
    }

   // private fun subscribeToLocationUpdate() = viewModel?.lastLocation?.observe(this,lastLocationObserver)


    private fun subscribeToGpsListener() = viewModel?.gpsStatusLiveData?.observe(this, gpsObserver)

//    private fun subscribeToLocationPermissionListener() =
//        viewModel?.locationPermissionStatusLiveData?.observe(this, permissionObserver)

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent() {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val options1 = LocationComponentOptions.builder(this)
                .gpsDrawable(R.drawable.ic_cursor_)
                .elevation(5f)
                .accuracyAlpha(0.1f)
                .trackingGesturesManagement(false)
                .compassAnimationEnabled(false)
                .build()

            // Create and customize the LocationComponent's options
            val options = LocationComponentOptions.builder(this)
                .elevation(5f)
                .accuracyAlpha(.1f)
                .accuracyColor(Color.BLUE)
              //  .gpsDrawable(R.drawable.ic_cursor_)
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

            val position = CameraPosition.Builder()
                .zoom(18.0)
                .build()

             //Set the component's camera mode
            locationComponent?.cameraMode = CameraMode.TRACKING_GPS

            // Set the component's render mode
            locationComponent?.renderMode = RenderMode.GPS
            locationComponent?.isLocationComponentEnabled = true
            mapboxMap!!.cameraPosition = position


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

    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent()
        } else {
            iviMessageParent!!.visibility = VISIBLE
            var messageString = "Please go to application setting and enable location permission in order to use the service"
            vIVIMessage!!.text =messageString
            vIVIMessage!!.typeface = Typeface.DEFAULT
            vIVIMessage!!.isSelected  = true
            Handler().postDelayed({
                iviMessageParent!!.visibility = GONE
                finish()
            }, 60000)

        }
    }

    inner class PlaybackListener : LocationServiceCallback() {
        override fun onEgnatiaUserMessage(message: EgnatiaUserMessage) {
            iviMessageParent!!.visibility = VISIBLE
            var messageString = message.egantiaMessage
            vIVIMessage!!.typeface =tf
            vIVIMessage!!.isSelected  = true
            vIVIMessage!!.text = messageString
            Handler().postDelayed({
                iviMessageParent!!.visibility = GONE
            }, 30000)
        }

        override fun onDenmUserMessage(message: DENMUserMessage) {
            iviMessageParent!!.visibility = VISIBLE
            var messageString = "Vehicle Breakdown"
            vIVIMessage!!.typeface =tf
            vIVIMessage!!.isSelected  = true
            vIVIMessage!!.text = messageString
            Handler().postDelayed({
                iviMessageParent!!.visibility = GONE
            }, 30000)
        }

        override fun onIVIMessageReceived(message: IVIUserMessage) {
            Log.d(TAG,message.toString())
            iviSing!!.visibility = VISIBLE
            speedBar!!.setMaxValues(50f)

        }

        override fun onVIVIUserMessage(message: VIVIUserMessage) {
            iviMessageParent!!.visibility = VISIBLE
            var messageString = message.route+" "+Helper.grapMinutes(message.eta)+"'"
            vIVIMessage!!.typeface =tf
            vIVIMessage!!.isSelected  = true
            vIVIMessage!!.text = messageString
            Handler().postDelayed({
                iviMessageParent!!.visibility = GONE
            }, 30000)


        }

        override fun onSPATUserMessage(message: SPATUserMessage) {
            if(message.eventState.equals("green",ignoreCase = true)){
                trafficLight!!.visibility = VISIBLE

            }else{
                trafficLight!!.visibility = GONE

            }
        }

        override fun onPositionChanged(position: Location) {
            speedBar!!.setCurrentValues(Helper.toKmPerHour(position.speed).toFloat())
//            var position1 = CameraPosition.Builder()
//.target( LatLng(position.latitude, position.longitude)) // Sets the new camera position
//.bearing(position.bearing.toDouble()) // Rotate the camera
//.build(); // Creates a CameraPosition from the builder
//
//mapboxMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(position1))


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

