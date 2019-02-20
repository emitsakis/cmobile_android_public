package certh.hit.cmobile.service

import android.location.Location
import certh.hit.cmobile.model.*

/**
 * Created by anmpout on 09/02/2019
 */
public abstract class LocationServiceCallback {
    /**
     * Callback called when position is being changed.
     *
     * @param position the position
     */
    abstract fun onPositionChanged(position: Location)

    abstract fun onIVIMessageReceived(message: IVIUserMessage)

    abstract fun onVIVIUserMessage(message: VIVIUserMessage)

    abstract  fun onSPATUserMessage(message: SPATUserMessage)

    abstract fun onEgnatiaUserMessage(message: EgnatiaUserMessage)

    abstract fun onDenmUserMessage(message: DENMUserMessage)

    abstract fun onIVIUnsubscribe()

    abstract fun onSPATUnsubscribe()
}