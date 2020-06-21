package com.squadx.crown.makemoneyapp.controller;

import android.content.Context;

import com.squadx.crown.makemoneyapp.model.LiUrl;
import com.squadx.crown.makemoneyapp.model.ListItem;

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
            if (listItem.getItemType() == ListItem.TYPE_URL) {
                LiUrl obj = (LiUrl) listItem;
                if (removedArticles.contains(obj.getId())) {
                    iterator.remove();
                    continue;
                }
                if (fav.contains(obj.getId())) obj.setMyFav(true);
                else obj.setMyFav(false);
            }
            temp.add(listItem);
        }
        return temp;
    }
}
