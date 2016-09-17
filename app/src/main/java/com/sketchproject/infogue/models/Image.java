package com.sketchproject.infogue.models;

/**
 * Sketch Project Studio
 * Created by angga on 16/09/16.
 */
public class Image {
    public static final String ID = "id";
    public static final String SOURCE = "source";

    private int id;
    private String source;

    public Image() {
    }

    public Image(int id, String source) {
        this.id = id;
        this.source = source;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
