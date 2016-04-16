package com.sketchproject.infogue.models;

import java.util.List;

/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 22.09.
 */
public class Article {
    public static final String ARTICLE_ID = "article_id";
    public static final String ARTICLE_SLUG = "slug";
    public static final String ARTICLE_TITLE = "title";
    public static final String ARTICLE_FEATURED = "featured";
    public static final String ARTICLE_CATEGORY = "category";
    public static final String ARTICLE_SUBCATEGORY = "subcategory";
    public static final String ARTICLE_CONTENT = "content";
    public static final String ARTICLE_PUBLISHED_AT = "published_at";
    public static final String ARTICLE_TAGS = "tags";
    public static final String ARTICLE_VIEW = "view";
    public static final String ARTICLE_RATING = "rating";

    private int id;
    private String slug;
    private String title;
    private String featured;
    private String category;
    private String subcategory;
    private String content;
    private String publishedAt;
    private List<String> tags;
    private int view;
    private int rating;

    public Article(int id, String slug, String title) {
        this.id = id;
        this.slug = slug;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFeatured() {
        return featured;
    }

    public void setFeatured(String featured) {
        this.featured = featured;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}