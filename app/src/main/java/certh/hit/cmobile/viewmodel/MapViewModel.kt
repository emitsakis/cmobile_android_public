package certh.hit.cmobile.viewmodel

import android.Manifest
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import certh.hit.cmobile.location.GpsStatusListener
import certh.hit.cmobile.location.LocationUpdateListener
import certh.hit.cmobile.location.PermissionStatusListener

/**
 * Created by anmpout on 02/02/2019
 */


class MapViewModel(application: Application) : AndroidViewModel(application) {

    val gpsStatusLiveData = GpsStatusListener(application)

    val lastLocation = LocationUpdateListener(application)

    val locationPermissionStatusLiveData = PermissionStatusListener(application,
        Manifest.permission.ACCESS_FINE_LOCATION)


}