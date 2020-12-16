package com.squadx.crown.makemoneyapp.model

import com.google.gson.Gson
import com.squadx.crown.makemoneyapp.util.ListItemType
import java.sql.Timestamp

open class ArticleV0 : ListItem() {
    lateinit var addedAt: Timestamp
    lateinit var id: String
    lateinit var title: String
    var upVotes: Long = 0
    var downVotes: Long = 0
    var myUpVote: Boolean = false
    var myDownVote: Boolean = false
    lateinit var author: String
    lateinit var source: String
    lateinit var image: String
    var favourites: Long = 0
    var reads: Long = 0
    var myFav: Boolean = false
    var mmType: Int = 0

    override val itemType: Int
        get() = ListItemType.ARTICLE_V0

    override fun toString(): String {
        return Gson().toJson(this)
    }
}