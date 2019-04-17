package certh.hit.cmobile.service

/**
 * Created by anmpout on 09/02/2019
 */
interface LocationServiceInterface {

    /**
     * Setups notification.
     *
     * @param noLecture    the no lecture
     * @param lectureTitle the lecture title
     */
    abstract fun setupNotification(noLecture: String, lectureTitle: String)

}