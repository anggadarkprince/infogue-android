package com.sketchproject.infogue.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Sketch Project Studio
 * Created by angga on 16/09/16.
 */
public class Image implements Parcelable {
    public static final String ID = "id";
    public static final String SOURCE = "source";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
    private int id;
    private String source;

    public Image() {
    }

    public Image(int id, String source) {
        this.id = id;
        this.source = source;
    }

    protected Image(Parcel in) {
        id = in.readInt();
        source = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(source);
    }
}
