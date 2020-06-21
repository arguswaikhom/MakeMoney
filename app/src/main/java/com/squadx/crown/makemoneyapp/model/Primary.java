package com.squadx.crown.makemoneyapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Primary extends ListItem implements Parcelable{

    private String mTitle;
    private String mLink;
    private String mSubscription;
    private String mBy;

    public Primary(String title, String link, String subscription, String by) {
        mTitle = title;
        mLink = link;
        mSubscription = subscription;
        mBy = by;
    }

    protected Primary(Parcel in) {
        mTitle = in.readString();
        mLink = in.readString();
        mSubscription = in.readString();
        mBy = in.readString();
    }

    public static final Creator<Primary> CREATOR = new Creator<Primary>() {
        @Override
        public Primary createFromParcel(Parcel in) {
            return new Primary(in);
        }

        @Override
        public Primary[] newArray(int size) {
            return new Primary[size];
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

    @Override
    public int getItemType() {
        return ListItem.TYPE_PRIMARY;
    }
}
