package com.squadx.crown.makemoneyapp.controller;

import android.content.Context;

import com.squadx.crown.makemoneyapp.model.ArticleV0;
import com.squadx.crown.makemoneyapp.model.ListItem;
import com.squadx.crown.makemoneyapp.util.ListItemType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpdateDataset {
    public static List<ListItem> update(Context context, List<ListItem> dataset) {
        PreferenceController pre = PreferenceController.getInstance(context);
        List<String> fav = pre.getFavourites();
        List<String> removedArticles = pre.getRemovedArticle();

        List<ListItem> temp = new ArrayList<>();
        Iterator<ListItem> iterator = dataset.iterator();
        while (iterator.hasNext()) {
            ListItem listItem = iterator.next();
            if (listItem.getItemType() == ListItemType.ARTICLE_URL || listItem.getItemType() == ListItemType.ARTICLE_HTML) {
                ArticleV0 obj = (ArticleV0) listItem;
                if (removedArticles.contains(obj.getId())) {
                    iterator.remove();
                    continue;
                }
                obj.setMyFav(fav.contains(obj.getId()));
            }
            temp.add(listItem);
        }
        return temp;
    }
}
