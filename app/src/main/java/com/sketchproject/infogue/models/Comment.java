package com.sketchproject.infogue.models;

/**
 * Sketch Project Studio
 * Created by Angga on 26/04/2016 18.40.
 */
public class Comment {
    public static final String COMMENT_ID = "id";
    public static final String COMMENT_CONTENT = "comment";
    public static final String COMMENT_ARTICLE_ID = "article_id";
    public static final String COMMENT_CONTRIBUTOR_ID = "contributor_id";
    public static final String COMMENT_USERNAME = "username";
    public static final String COMMENT_NAME = "name";
    public static final String COMMENT_AVATAR = "avatar";
    public static final String COMMENT_TIMESTAMP = "created_at";

    private int id;
    private int articleId;
    private int contributorId;
    private String username;
    private String comment;
    private String name;
    private String avatar;
    private String timestamp;

    public Comment() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
