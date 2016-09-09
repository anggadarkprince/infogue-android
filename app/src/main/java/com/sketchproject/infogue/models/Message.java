package com.sketchproject.infogue.models;

/**
 * Comment model data.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 26/04/2016 18.40.
 */
public class Message {
    public static final String ID = "message_id";
    public static final String CONTRIBUTOR_ID = "contributor_id";
    public static final String USERNAME = "username";
    public static final String MESSAGE = "message";
    public static final String NAME = "name";
    public static final String AVATAR = "avatar_ref";
    public static final String TIMESTAMP = "created_at";

    private int id;
    private int contributorId;
    private String username;
    private String message;
    private String name;
    private String avatar;
    private String timestamp;

    public Message() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContributorId() {
        return contributorId;
    }

    public void setContributorId(int contributorId) {
        this.contributorId = contributorId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
