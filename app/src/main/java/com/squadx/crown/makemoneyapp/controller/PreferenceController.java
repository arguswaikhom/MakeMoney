package com.squadx.crown.makemoneyapp.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squadx.crown.makemoneyapp.model.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceController {
    private static final String TAG_VERSION = "VERSION";
    private static final String TAG_USER = "USER";
    private static final String TAG_CLICKS = "CLICKS";
    private static final String TAG_FAVOURITE = "FAVOURITE";
    private static final String TAG_READ = "READ";
    private static final String TAG_REACTION = "REACTION";
    private static final String TAG_REMOVED_ARTICLES = "REMOVED_ARTICLES";
    private static final PreferenceController instance = null;
    private final SharedPreferences preferences;

    private PreferenceController(Context context) {
        this.preferences = context.getSharedPreferences(TAG_CLICKS, MODE_PRIVATE);
    }

    public static PreferenceController getInstance(Context context) {
        if (instance == null) return new PreferenceController(context);
        return instance;
    }

    public void clicked() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("click", getClick() + 1);
        editor.apply();
    }

    public int getClick() {
        return preferences.getInt("click", 0);
    }

    public List<String> getFavourites() {
        String json = preferences.getString(TAG_FAVOURITE, null);
        if (json != null) {
            String[] fav = new Gson().fromJson(json, String[].class);
            return new ArrayList<>(Arrays.asList(fav));
        }
        return new ArrayList<>();
    }

    public void updateFavourite(String id) {
        List<String> favList = getFavourites();
        HashSet<String> favSet = new HashSet<>(favList);
        if (favSet.contains(id)) favSet.remove(id);
        else favSet.add(id);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TAG_FAVOURITE, favSet.toString());
        editor.apply();
    }

    public void updateReactions(Map<String, Boolean> reactions) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TAG_REACTION, reactions.toString());
        editor.apply();
    }

    public Map<String, Boolean> getReactions() {
        String json = preferences.getString(TAG_REACTION, null);
        if (json != null) {
            Type type = new TypeToken<Map<String, Boolean>>() {
            }.getType();
            return new Gson().fromJson(json, type);
        }
        return new HashMap<>();
    }

    public List<String> getRemovedArticle() {
        String json = preferences.getString(TAG_REMOVED_ARTICLES, null);
        if (json != null) {
            String[] fav = new Gson().fromJson(json, String[].class);
            return new ArrayList<>(Arrays.asList(fav));
        }
        return new ArrayList<>();
    }

    public void updateRemovedArticle(String id) {
        List<String> articles = getRemovedArticle();
        HashSet<String> favSet = new HashSet<>(articles);
        if (favSet.contains(id)) favSet.remove(id);
        else favSet.add(id);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TAG_REMOVED_ARTICLES, favSet.toString());
        editor.apply();
    }

    public void clearRemovedArticle() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(TAG_REMOVED_ARTICLES);
        editor.apply();
    }

    public User getUser() {
        String json = preferences.getString(TAG_USER, null);
        if (json != null) {
            return new Gson().fromJson(json, User.class);
        }
        return null;
    }

    public void setUser(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TAG_USER, new Gson().toJson(user));
        editor.apply();
    }

    public boolean addToRead(String id) {
        List<String> reads = getAllReads();
        if (reads == null) reads = new ArrayList<>();
        else if (reads.contains(id)) return false;
        reads.add(id);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TAG_READ, reads.toString());
        editor.apply();
        return true;
    }

    public List<String> getAllReads() {
        String json = preferences.getString(TAG_READ, null);
        if (json != null) {
            return new ArrayList<>(Arrays.asList(new Gson().fromJson(json, String[].class)));
        }
        return null;
    }

    public int getVersion() {
        return preferences.getInt(TAG_VERSION, -1);
    }

    public void setVersion(int version) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(TAG_VERSION, version);
        editor.apply();
    }

    public void clearAll() {
        preferences.edit().clear().apply();
    }
}
