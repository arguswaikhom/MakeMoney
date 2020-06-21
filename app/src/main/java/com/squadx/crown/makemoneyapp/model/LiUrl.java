package com.squadx.crown.makemoneyapp.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class LiUrl extends ListItem {

    String title;
    String url;
    String author;
    String source;
    String image;
    String id;
    Long upVotes;
    Long downVotes;
    Long favourites;
    Long reads;
    Boolean myFav;
    Boolean myUpVote;
    Boolean myDownVote;

    public LiUrl() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return author;
    }

    public String getSource() {
        return source;
    }

    public String getImage() {
        return image;
    }

    public String getId() {
        return id;
    }

    public Long getUpVotes() {
        return upVotes != null ? upVotes : 0;
    }

    public Long getDownVotes() {
        return downVotes != null ? downVotes : 0;
    }

    public Long getFavourites() {
        return favourites != null ? favourites : 0;
    }

    public Long getReads() {
        return reads != null ? reads : 0;
    }

    public Boolean getMyFav() {
        return myFav == null ? false : myFav;
    }

    public void setMyFav(Boolean myFav) {
        this.myFav = myFav;
    }

    public Boolean getMyUpVote() {
        return myUpVote == null ? false : myUpVote;
    }

    public void setMyUpVote(Boolean myUpVote) {
        this.myUpVote = myUpVote;
    }

    public Boolean getMyDownVote() {
        return myDownVote == null ? false : myDownVote;
    }

    public void setMyDownVote(Boolean myDownVote) {
        this.myDownVote = myDownVote;
    }

    @Override
    public int getItemType() {
        return ListItem.TYPE_URL;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
