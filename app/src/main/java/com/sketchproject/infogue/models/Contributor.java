package com.sketchproject.infogue.models;

import java.util.Date;

/**
 * Sketch Project Studio
 * Created by Angga on 10/04/2016 21.32.
 */
public class Contributor {
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";
    public static final String GENDER_OTHER = "other";

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACTIVATED = "activated";
    public static final String STATUS_SUSPENDED = "suspended";

    private int id;
    private String username;
    private String email;
    private String password;
    private String newPassword;
    private String name;
    private String location;
    private String about;
    private String contact;
    private Date birthday;
    private String gender;
    private String facebook;
    private String twitter;
    private String googlePlus;
    private String instagram;
    private boolean notificationSubscribe;
    private boolean notificationMessage;
    private boolean notificationFollower;
    private boolean notificationStream;
    private boolean pushNotification;
    private String avatar;
    private String cover;
    private int article;
    private int followers;
    private int following;
    private boolean isFollowing;

    public Contributor(){

    }

    public Contributor(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getGooglePlus() {
        return googlePlus;
    }

    public void setGooglePlus(String googlePlus) {
        this.googlePlus = googlePlus;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public boolean isNotificationSubscribe() {
        return notificationSubscribe;
    }

    public void setNotificationSubscribe(boolean notificationSubscribe) {
        this.notificationSubscribe = notificationSubscribe;
    }

    public boolean isNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(boolean notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public boolean isNotificationFollower() {
        return notificationFollower;
    }

    public void setNotificationFollower(boolean notificationFollower) {
        this.notificationFollower = notificationFollower;
    }

    public boolean isNotificationStream() {
        return notificationStream;
    }

    public void setNotificationStream(boolean notificationStream) {
        this.notificationStream = notificationStream;
    }

    public boolean isPushNotification() {
        return pushNotification;
    }

    public void setPushNotification(boolean pushNotification) {
        this.pushNotification = pushNotification;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(boolean isFollowing) {
        this.isFollowing = isFollowing;
    }

}
