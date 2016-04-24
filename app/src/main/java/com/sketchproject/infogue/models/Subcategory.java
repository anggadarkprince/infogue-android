package com.sketchproject.infogue.models;

/**
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.20.
 */
public class Subcategory {
    public static final String TABLE = "subcategory";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CATEGORY_ID = "categoryId";
    public static final String COLUMN_SUBCATEGORY = "subcategory";

    private int id;
    private int category_id;
    private String subcategory;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return category_id;
    }

    public void setCategoryId(int category_id) {
        this.category_id = category_id;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }
}
