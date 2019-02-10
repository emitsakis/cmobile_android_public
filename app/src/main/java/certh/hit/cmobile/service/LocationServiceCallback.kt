package certh.hit.cmobile.service

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

}