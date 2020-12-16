package com.squadx.crown.makemoneyapp.model

import com.squadx.crown.makemoneyapp.util.ListItemType

class ArticleHtml : ArticleV0() {
    lateinit var htmlContent: String

    override val itemType: Int
        get() = ListItemType.ARTICLE_HTML
}