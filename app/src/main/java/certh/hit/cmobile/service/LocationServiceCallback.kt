package certh.hit.cmobile.service

import certh.hit.cmobile.model.IVIUserMessage
import certh.hit.cmobile.model.SPATUserMessage
import certh.hit.cmobile.model.VIVIUserMessage

/**
 * Created by anmpout on 09/02/2019
 */
public abstract class LocationServiceCallback {
    /**
     * Callback called when position is being changed.
     *
     * @param position the position
     */
    abstract fun onPositionChanged(position: Int)

    abstract fun onIVIMessageReceived(message: IVIUserMessage)

    abstract fun onVIVIUserMessage(message: VIVIUserMessage)

    abstract  fun onSPATUserMessage(message: SPATUserMessage)
}