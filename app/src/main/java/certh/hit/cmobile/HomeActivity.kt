package certh.hit.cmobile

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import certh.hit.cmobile.location.GpsStatus
import certh.hit.cmobile.location.PermissionStatus
import certh.hit.cmobile.model.*
import certh.hit.cmobile.service.LocationService
import certh.hit.cmobile.service.LocationServiceCallback
import certh.hit.cmobile.service.LocationServiceInterface
import certh.hit.cmobile.utils.ColorArcProgressBar
import certh.hit.cmobile.utils.GeoHelper
import certh.hit.cmobile.utils.Helper
import certh.hit.cmobile.utils.TTS
import certh.hit.cmobile.viewmodel.MapViewModel
import com.google.gson.Gson
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Created by anmpout on 21/01/2019
 */
class HomeActivity : AppCompatActivity(),OnMapReadyCallback,PermissionsListener, MapboxMap.OnMapClickListener {


    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null
    private var style :Style? = null
    private val TAG: String = HomeActivity::class.java.canonicalName  as String
    private var viewModel: MapViewModel? = null
    private var locationIntent :Intent? = null
    private var locationSrv: LocationService? = null
    private var mLocationAdapter:LocationServiceInterface? = null
    private var vIVIMessage :TextView? = null
    private var iviMessageParent :RelativeLayout? = null
    private var trafficLight :RelativeLayout? = null
    private var trafficLightRed :ImageView? = null
    private var trafficLightYellow :ImageView? = null
    private var trafficLightGreen :ImageView? = null
    private var settings :FloatingActionButton? = null
    private var flip :FloatingActionButton? = null
    private var reCenter :FloatingActionButton? = null
    private var  tf : Typeface? = null
    private var speedBar : ColorArcProgressBar? = null
    private var iviSing : ImageView? = null
    private var  root : RelativeLayout? = null
    private lateinit var  denmStaticMessages :DENMStaticMessage
    private var flipFlag: Int? = 0
    private var isInsideIvi: Int? =0
    private var isInsideDenm: Int? =0
    private var iVIMessageToHandle :IVIUserMessage? = null
    private var denmMessageToHandle :DENMUserMessage? = null
    private var denmTTS :Boolean? = null
    private var iviTTS :Boolean? = null
    private var mapboxIsReady :Boolean = false

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
        Helper.createUID()
        Log.d("locked",checkOrientationLocked().toString())
        Mapbox.getInstance(this, Helper.mapsKey)
        setContentView(R.layout.activity_home)
        setupUI()
        var data = Helper.getAssetJsonData(applicationContext,"data.json")
        denmStaticMessages = Gson().fromJson<DENMStaticMessage>(data,DENMStaticMessage::class.java)
        mapView?.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapView?.getMapAsync(this)



    }

    private fun setupUI() {
        mapView = findViewById(R.id.mapView)
        vIVIMessage = findViewById(R.id.vivi_message);
       // tf = Typeface.createFromAsset(assets,  "fonts/led.ttf");
       // vIVIMessage!!.typeface =tf
        speedBar = findViewById(R.id.speed_bar)
        iviMessageParent = findViewById(R.id.vivi_message_parent)
        trafficLight = findViewById(R.id.traffic_light)
        trafficLightRed = findViewById(R.id.red_light)
        trafficLightYellow = findViewById(R.id.yellow_light)
        trafficLightGreen = findViewById(R.id.green_light)
        iviSing = findViewById(R.id.ivi_sign)
        settings = findViewById(R.id.settings)
        settings!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //Go to next page i.e, start the next activity.
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
            }})
        flip = findViewById(R.id.flip)
        flip!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if(checkOrientationLocked()){
                    showAlertDialogLockedOrientation()

                }else {
                  if (flipFlag == 0) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        root!!.scaleX = -1.0f
                        mapView!!.scaleX = -1.0f
                        flipFlag = 1
                    } else {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        root!!.scaleX = 1.0f
                        mapView!!.scaleX = 1.0f
                        flipFlag = 0
                    }
                }
         }
        })
        reCenter = findViewById(R.id.fixed_location)
        reCenter!!.setOnClickListener(object : View.OnClickListener{
            @SuppressLint("MissingPermission")
            override fun onClick(p0: View?) {
                if(mapboxMap!!.locationComponent.isLocationComponentEnabled && mapboxMap!!.locationComponent.lastKnownLocation != null){
                    var cameraPosition = CameraPosition.Builder().target(LatLng(mapboxMap!!.locationComponent.lastKnownLocation!!.latitude,mapboxMap!!.locationComponent.lastKnownLocation!!.longitude)).zoom(18.0).build()
                   mapboxMap!!.cameraPosition = cameraPosition


                }
            }
        })
        root = findViewById(R.id.root)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        Log.d(TAG, "onMapReady:")
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.DARK){
            this.mapboxMap!!.addOnMapClickListener(this);
            this.style = mapboxMap.style


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
                    //locationComponent?.cameraMode = CameraMode.TRACKING_GPS
                }

                override fun onCameraTrackingChanged(currentMode: Int) {
                    // CameraMode has been updated
                    Log.d(TAG,"change"+currentMode.toString())
                   // locationComponent?.cameraMode = CameraMode.TRACKING_GPS

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
            mapboxIsReady = true

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
        mapboxIsReady = false

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
        override fun onSPATUnsubscribe() {
            trafficLight!!.visibility = GONE
        }

        override fun onIVIUnsubscribe() {
            iVIMessageToHandle = null
            //iviSing!!.visibility = GONE

        }
        override fun onDenmUnsubscribe() {
            denmMessageToHandle = null



        }
        override fun onEgnatiaUserMessage(message: EgnatiaUserMessage) {
            iviMessageParent!!.visibility = VISIBLE
            var messageString = message.egantiaMessage
            vIVIMessage!!.typeface =tf
            vIVIMessage!!.isSelected  = true
            vIVIMessage!!.text = messageString
            vIVIMessage!!.setTextColor(resources.getColor(R.color.gold));
            Handler().postDelayed({
                iviMessageParent!!.visibility = GONE
            }, 30000)
        }

        override fun onDenmUserMessage(message: DENMUserMessage) {
            denmMessageToHandle = message
            isInsideDenm = 0
            denmTTS = true
        }

        override fun onIVIMessageReceived(message: IVIUserMessage) {
            iVIMessageToHandle = message
             isInsideIvi = 0
             iviTTS = true
        }

        override fun onVIVIUserMessage(message: VIVIUserMessage) {
            iviMessageParent!!.visibility = VISIBLE
            var messageString = message.route+" "+Helper.grapMinutes(message.eta)+"'"
            vIVIMessage!!.typeface =tf
            vIVIMessage!!.isSelected  = true
            vIVIMessage!!.text = messageString
            vIVIMessage!!.setTextColor(resources.getColor(R.color.white));
            Handler().postDelayed({
                iviMessageParent!!.visibility = GONE
            }, 30000)


        }

        override fun onSPATUserMessage(
            message: SPATUserMessage,
            lastLocation: Location
        ) {

            if(message.eventState.equals("1000xxxx")){
                var isInsideSpat =0
            for(relevanceZone in message!!.relevanceZones!!){
                if(GeoHelper.isLocationInsideLineBox(message!!.actualLatitude,message!!.actuallongitude,
                        relevanceZone.zones,lastLocation.latitude,lastLocation.longitude)){

                    isInsideSpat = isInsideSpat!!.plus(other = 1)
                }
            }
            Log.d("isInside",isInsideSpat!!.toString())
            val systemTimestamp = System.currentTimeMillis()/1000
            val validTimestamp = systemTimestamp - message.timestamp!!
            Log.d("message timestamp", message!!.timestamp.toString())
            Log.d("system timestamp", systemTimestamp.toString())
            Log.d("valid timestamp",validTimestamp.toString())
            if (isInsideSpat!!>0 && validTimestamp<61){
                trafficLight!!.visibility = VISIBLE
                isInsideSpat = 0
            }else{
                trafficLight!!.visibility = GONE

            }
        }else{
                trafficLight!!.visibility = GONE
            }


            Log.d("debug message",message.toString())
//            if(GeoHelper.isLocationInsideLineBox(message.mapMessage!!.osmTagsStartLat,message.mapMessage!!.osmTagsStartLon,message.mapMessage!!.osmTagsStopLat,message.mapMessage!!.osmTagsStopLon,lastLocation.latitude,lastLocation.longitude)&& message.eventState.equals("green",ignoreCase = true)){
//                trafficLight!!.visibility = VISIBLE
//
//            }else{
//                trafficLight!!.visibility = GONE
//
//            }
        }

        override fun onPositionChanged(position: Location) {
            speedBar!!.setCurrentValues(Helper.toKmPerHour(position.speed).toFloat())
    if(mapboxIsReady && mapboxMap != null && mapboxMap?.locationComponent != null && mapboxMap?.locationComponent?.cameraMode != null) {
                if (mapboxMap?.locationComponent!!.cameraMode != CameraMode.TRACKING_GPS) {
                    mapboxMap?.locationComponent!!.cameraMode = CameraMode.TRACKING_GPS
                    //Set the component's camera mode
                    // Set the component's render mode
                    mapboxMap?.locationComponent!!.renderMode = RenderMode.GPS
                }
                if (mapboxMap?.cameraPosition!!.zoom != 18.0) {
                    val camPosition = CameraPosition.Builder()
                        .zoom(18.0)
                        .build()
                    mapboxMap?.cameraPosition = camPosition
                }
            }
            handleIvi(position)
            handleDenm(position)
       }
    }

    private fun handleDenm(position: Location) {
        if(denmMessageToHandle != null){

            for(relevanceZone in denmMessageToHandle!!.relevanceZones!!){
                if(GeoHelper.isLocationInsideLineBox(denmMessageToHandle!!.actualLatitude,denmMessageToHandle!!.actuallongitude,
                        relevanceZone.zones,position.latitude,position.longitude)){

                    isInsideDenm = isInsideDenm!!.plus(other = 1)
                }
            }
            Log.d("isInside",isInsideDenm!!.toString())
            val systemTimestamp = System.currentTimeMillis()/1000
            val validTimestamp = systemTimestamp - denmMessageToHandle!!.timestamp!!+ denmMessageToHandle!!.duration!!
            Log.d("message timestamp", denmMessageToHandle!!.timestamp.toString())
            Log.d("system timestamp", systemTimestamp.toString())
            Log.d("valid timestamp",validTimestamp.toString())
            if (isInsideDenm!!>0 &&validTimestamp>=0) {
                var messageToShow = Helper.getViviNameFromExtraText(denmMessageToHandle!!.extraTexts)
                var staticMessageToShow =
                    denmStaticMessages!!.list!!.firstOrNull() { w -> w.code == denmMessageToHandle!!.causeCode && w.subcode == denmMessageToHandle!!.subCauseCode }
                if (messageToShow.equals("")) {
                    if (staticMessageToShow != null) {
                        messageToShow = staticMessageToShow.message
                    }
                }
                //&& denmMessageToHandle!!.causeCode ==99 && denmMessageToHandle!!.subCauseCode ==99
                if (iviMessageParent!!.visibility== GONE &&!messageToShow.equals("")){
                    iviMessageParent!!.visibility = VISIBLE
                var messageString = messageToShow
                vIVIMessage!!.typeface = tf
                vIVIMessage!!.isSelected = true
                vIVIMessage!!.text = messageString
                vIVIMessage!!.setTextColor(resources.getColor(R.color.gold));
                if (denmTTS!!) {
                    TTS(this@HomeActivity, messageToShow)
                    denmTTS = false

                    style!!.addImage(
                        denmMessageToHandle!!.timestamp.toString(),
                        BitmapFactory.decodeResource(
                            this@HomeActivity.resources, R.drawable.ic_warning
                        )
                    )

                    var geoJsonSource = GeoJsonSource(
                        denmMessageToHandle!!.timestamp.toString(), Feature.fromGeometry(
                            Point.fromLngLat(
                                denmMessageToHandle!!.actuallongitude!!,
                                denmMessageToHandle!!.actualLatitude!!
                            )
                        )
                    );
                    style!!.addSource(geoJsonSource);

                    var symbolLayer = SymbolLayer(denmMessageToHandle!!.timestamp.toString(), denmMessageToHandle!!.timestamp.toString())
                    symbolLayer.withProperties(PropertyFactory.iconImage(denmMessageToHandle!!.timestamp.toString()))
                    style!!.addLayer(symbolLayer)

                }
            }
                isInsideDenm = 0
            }else{
                iviMessageParent!!.visibility = GONE
                style!!.removeLayer(denmMessageToHandle!!.timestamp.toString())
                style!!.removeSource(denmMessageToHandle!!.timestamp.toString())

            }
        }
    }

    private fun handleIvi(position: Location) {
        if(iVIMessageToHandle != null){

            for(relevanceZone in iVIMessageToHandle!!.relevanceZones!!){
                if(GeoHelper.isLocationInsideLineBox(iVIMessageToHandle!!.actualLatitude,iVIMessageToHandle!!.actuallongitude,
                        relevanceZone.zones,position.latitude,position.longitude)){
                    Log.d("isInside", true.toString())
                    isInsideIvi = isInsideIvi!!.plus(other = 1)
                }
            }
            Log.d("isInside",isInsideIvi!!.toString())
            if (isInsideIvi!!>0){
                if(iVIMessageToHandle!!.iviType==1) {
                    val iviImg = Helper.getIviSing(iVIMessageToHandle!!.serviceCategoryCode,iVIMessageToHandle!!.pictogramCategoryCode)
                    if(iviImg!=0) {
                        iviSing!!.visibility = VISIBLE
                        iviSing!!.setImageResource(iviImg)
                    }

                  //  speedBar!!.setMaxValues(50f)
                }else if(iVIMessageToHandle!!.iviType==2 && iVIMessageToHandle!!.messageOffline!!) {
                    iviMessageParent!!.visibility = VISIBLE
                    var path = Helper.getViviNameFromExtraText(iVIMessageToHandle!!.extraTexts)
                     if(!path.equals("")){
                    var messageString =
                        path + " " + Helper.convertSecToMin(
                            iVIMessageToHandle!!.travelTime
                        ) + "'"
                    vIVIMessage!!.typeface = tf
                    vIVIMessage!!.isSelected = true
                    vIVIMessage!!.text = messageString
                    vIVIMessage!!.setTextColor(resources.getColor(R.color.white))
                    if (iviTTS!!) {
                        TTS(this@HomeActivity, messageString)
                        iviTTS = false
                    }
                }
                }
                isInsideIvi = 0
            }else{
                if(iVIMessageToHandle!!.iviType==1) {
                    iviSing!!.visibility = GONE
                }else if(iVIMessageToHandle!!.iviType==2) {
                    iviMessageParent!!.visibility = GONE
                }

            }
        }
    }



    /**
     * Connect to the service
     *
     *
     */
    private val locationConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG,"onStart: create service")
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

    override fun onBackPressed() {
        showExitAlert()

    }

    private fun showExitAlert() {
        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setTitle("C- Mobile")
        builder.setMessage("Exit application")
        builder.setPositiveButton("YES"){dialog, which ->
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        builder.setNegativeButton("No"){dialog,which ->
            dialog.dismiss()

        }
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()    }

    override fun onMapClick(point: LatLng): Boolean {


        return false
    }

    fun calculateOhmId(position: Location){
        var point = LatLng(position.latitude,position.longitude)
        val pixel = mapboxMap!!.projection.toScreenLocation(point)
        val features = mapboxMap!!.queryRenderedFeatures(pixel)
        //  map

        // Get the first feature within the list if one exist
        if (features.size > 0) {
            val feature = features[0]
            val call = CMobileApplication.getService()!!.listRepos(feature.id().toString())
            call.enqueue(object : Callback<Osm> {
                override fun onResponse(call: Call<Osm>, response: Response<Osm>) {

                }

                override fun onFailure(call: Call<Osm>, t: Throwable) {

                    println(t.localizedMessage)
                }
            })

            // Ensure the feature has properties defined
            if (feature.properties() != null) {
                Log.d(TAG, String.format("Id = %s", feature.toString()))
                for ((key, value) in feature.properties()!!.entrySet()) {
                    // Log all the properties
                    Log.d(TAG, String.format("%s = %s", key, value))
                }
            }
        }


    }

    fun checkOrientationLocked():Boolean{
          val str = Settings.System.getInt(this@HomeActivity.contentResolver,
             Settings.System.ACCELEROMETER_ROTATION);

   if(str==1)
   {
       return false
      // rotation is Unlocked
   }
       return true
      // rotation is Locked

    }
    fun showAlertDialogLockedOrientation(){
        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setTitle("C- Mobile")
        builder.setMessage("Rotation is locked!Please go to the settings and disable it.")
        builder.setPositiveButton("OK"){dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()



    }
}

