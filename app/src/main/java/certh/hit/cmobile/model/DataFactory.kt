package certh.hit.cmobile.model

import java.util.ArrayList

/**
 * Created by anmpout on 27/01/2019
 */
class DataFactory {
    private var allTopics: List<Topic>? = null
    //internal val subscriptionTopic = "hit_certh/ivi_hit/1/2/2/1/0/0/0/0/0/3/2/1/1/3/2/1/1/0/666"
    //internal val subscriptionTopic = "hit_certh/v-ivi_hit/1/2/2/1/0/0/0/0/0/3/0/3/0/2/3/2/3/0/v.olgas-ymca"
    //internal val subscriptionTopic = "hit_certh/spat_hit/1002"
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
        tmpTopic.basePath = "hit_certh"
        tmpTopic.data = "666"
        tmpTopic.type = "ivi_hit"
        tmpTopic.quadTree = "1/2/2/1/0/0/0/0/0/3/2/1/1/3/2/1/1/0"
        tmpTopic.typeId = "2"
        topics.add(tmpTopic)
        tmpTopic = Topic()
        tmpTopic.basePath = "hit_certh"
        tmpTopic.data = "v.olgas-ymca"
        tmpTopic.type = "v-ivi_hit"
        tmpTopic.quadTree = "1/2/2/1/0/0/0/0/0/3/0/3/0/2/3/2/3/0"
        tmpTopic.typeId = "1"
        topics.add(tmpTopic)

        allTopics = ArrayList(topics)
        return allTopics as ArrayList<Topic>
    }

    fun setAllTopics(allTopics: List<Topic>) {
        this.allTopics = allTopics
    }
}
