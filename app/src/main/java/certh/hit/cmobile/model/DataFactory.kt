package certh.hit.cmobile.model

import java.util.ArrayList

/**
 * Created by anmpout on 27/01/2019
 */
class DataFactory {
    private var allTopics: List<Topic>? = null

    init {
        allTopics = ArrayList()
    }

    fun getAllTopics(): List<Topic> {

        return if (allTopics!!.isEmpty()) createTopics() else allTopics!!
    }

    private fun createTopics(): List<Topic> {
        val topics = ArrayList<Topic>()
        var tmpTopic :Topic?
        tmpTopic = Topic()
        tmpTopic.basePath = "hit"
        tmpTopic.data = "data"
        tmpTopic.type = Topic.DENM
        tmpTopic.quadTree = "1/2/3/4"
        tmpTopic.typeId = "2"
        topics.add(tmpTopic)
        tmpTopic = Topic()
        tmpTopic.basePath = "hit"
        tmpTopic.data = "data"
        tmpTopic.type = Topic.IVI
        tmpTopic.quadTree = "4/3/2/1"
        tmpTopic.typeId = "1"
        topics.add(tmpTopic)

        allTopics = ArrayList(topics)
        return allTopics as ArrayList<Topic>
    }

    fun setAllTopics(allTopics: List<Topic>) {
        this.allTopics = allTopics
    }
}
