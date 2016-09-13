package com.sketchproject.infogue.models;

/**
 * Sketch Project Studio
 * Created by angga on 13/09/16.
 */
public class Conversation {
    public static final String ID = "id";
    public static final String OWNER = "owner";
    public static final String MESSAGE = "message";
    public static final String AVATAR = "avatar_ref";
    public static final String TIMESTAMP = "created_at";

    private int id;
    private String owner;
    private String message;
    private String avatar;
    private String timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message.replaceAll("<br />", "");
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
