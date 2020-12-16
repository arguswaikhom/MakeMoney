package com.squadx.crown.makemoneyapp.model

import com.google.gson.Gson
import com.squadx.crown.makemoneyapp.util.ListItemType

class ArticleUrl : ArticleV0() {
    lateinit var url: String

    override val itemType: Int
        get() = ListItemType.ARTICLE_URL

    override fun toString(): String {
        return Gson().toJson(this)
    }
}