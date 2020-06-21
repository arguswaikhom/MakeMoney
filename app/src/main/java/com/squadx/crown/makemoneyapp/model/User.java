package com.squadx.crown.makemoneyapp.model;

public class User {
    String userId;
    String name;
    String profileImage;
    String email;

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public User(String userId, String name, String email, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
