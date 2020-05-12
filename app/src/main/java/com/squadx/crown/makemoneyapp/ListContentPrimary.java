package com.squadx.crown.makemoneyapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

class ListContentPrimary extends ArrayList<Parcelable> implements Parcelable{

    private String mTitle;
    private String mLink;
    private String mSubscription;
    private String mBy;

    ListContentPrimary(String title, String link, String subscription, String by) {
        mTitle = title;
        mLink = link;
        mSubscription = subscription;
        mBy = by;
    }

    protected ListContentPrimary(Parcel in) {
        mTitle = in.readString();
        mLink = in.readString();
        mSubscription = in.readString();
        mBy = in.readString();
    }

    public static final Creator<ListContentPrimary> CREATOR = new Creator<ListContentPrimary>() {
        @Override
        public ListContentPrimary createFromParcel(Parcel in) {
            return new ListContentPrimary(in);
        }

        @Override
        public ListContentPrimary[] newArray(int size) {
            return new ListContentPrimary[size];
        }
    };

    public String getTitle() {
        return mTitle;
    }
    public String getBy() { return mBy; }
    public String getLink() {
        return mLink;
    }
    public String getSubscription() { return mSubscription; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mLink);
        parcel.writeString(mSubscription);
        parcel.writeString(mBy);
    }
}
