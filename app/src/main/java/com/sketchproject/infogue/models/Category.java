package com.sketchproject.infogue.models;

/**
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.16.
 */
public class Category {
    public static final String TABLE = "categories";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CATEGORY = "category";

    private int id;
    private String category;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
