package com.sketchproject.infogue.models;

/**
 * Subcategory model data.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.20.
 */
public class Subcategory {
    public static final String TABLE = "subcategories";
    public static final String ID = "id";
    public static final String CATEGORY_ID = "category_id";
    public static final String SUBCATEGORY = "subcategory";
    public static final String LABEL = "label";

    private int id;
    private int category_id;
    private String subcategory;
    private String label;

    public Subcategory() {

    }

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
