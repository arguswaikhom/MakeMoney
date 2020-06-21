package com.squadx.crown.makemoneyapp.model;

public abstract class ListItem {
    public static final int TYPE_PRIMARY = 1;
    public static final int TYPE_URL = 0;

    abstract public int getItemType();
}
