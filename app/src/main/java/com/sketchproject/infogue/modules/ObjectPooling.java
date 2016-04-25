package com.sketchproject.infogue.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Sketch Project Studio
 * Created by Angga on 25/04/2016 09.37.
 */
public class ObjectPooling {
    public static final String KEY_LABEL = "label";
    public static final String KEY_OBJECT = "object";

    private List<HashMap<String, Object>> objects;
    private boolean isFixedSize = false;
    private int max = 0;

    public ObjectPooling() {
        objects = new ArrayList<>();
    }

    public ObjectPooling(List<HashMap<String, Object>> objectList) {
        objects = objectList;
    }

    public ObjectPooling(int maxSize) {
        isFixedSize = true;
        max = maxSize;
        objects = new ArrayList<>(max);
    }

    public boolean pool(Object data, String label) {
        if (isObjectAvailable(label)) {
            return false;
        }

        if (isFixedSize && getSize() > max) {
            throw new IllegalStateException(ObjectPooling.class.getSimpleName() +
                    " is out of max number of container pool object. Max size of object is " + max);
        }

        HashMap<String, Object> object = new HashMap<>();
        object.put(KEY_LABEL, label);
        object.put(KEY_OBJECT, data);
        objects.add(object);
        return true;
    }

    public boolean isObjectAvailable(String label) {
        for (HashMap object : objects) {
            if (object.get(KEY_LABEL).equals(label)) {
                return true;
            }
        }
        return false;
    }

    public Object unpool(String label) {
        for (HashMap object : objects) {
            if (object.get(KEY_LABEL).equals(label)) {
                Object data = object.get(KEY_OBJECT);
                objects.remove(object);
                return data;
            }
        }
        return null;
    }

    public Object find(String label) {
        for (HashMap object : objects) {
            if (object.get(KEY_LABEL).equals(label)) {
                return object.get(KEY_OBJECT);
            }
        }
        return null;
    }

    public boolean isFixedSize() {
        return isFixedSize;
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public int getSize() {
        return objects.size();
    }

    public void clear() {
        objects.clear();
    }
}
