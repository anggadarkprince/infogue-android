package com.sketchproject.infogue.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Pooling object into list and retrieve if needed, this class for optimization
 * and prevent object to recreate again.
 *
 * Sketch Project Studio
 * Created by Angga on 25/04/2016 09.37.
 */
public class ObjectPooling {
    public static final String KEY_LABEL = "label";
    public static final String KEY_OBJECT = "object";

    private List<HashMap<String, Object>> objects;
    private boolean isFixedSize = false;
    private int max = 0;

    /**
     * Default constructor
     */
    public ObjectPooling() {
        objects = new ArrayList<>();
    }

    /**
     * Constructor with default object has been set.
     *
     * @param objectList objects with label.
     */
    @SuppressWarnings("unused")
    public ObjectPooling(List<HashMap<String, Object>> objectList) {
        objects = objectList;
    }

    /**
     * Create fixed object pooling.
     *
     * @param maxSize number of objects can handle.
     */
    @SuppressWarnings("unused")
    public ObjectPooling(int maxSize) {
        isFixedSize = true;
        max = maxSize;
        objects = new ArrayList<>(max);
    }

    /**
     * Registering object into list and populate by label.
     *
     * @param data object need to returned when needed
     * @param label label related to data object as reference to identifying it
     * @return status if the object has been there or not
     */
    public boolean pool(Object data, String label) {
        if (isObjectAvailable(label)) {
            return false;
        }

        if (isFixedSize && getSize() > max) {
            throw new IndexOutOfBoundsException(ObjectPooling.class.getSimpleName() +
                    " is out of max number of container pool object. Max size of object is " + max);
        }

        HashMap<String, Object> object = new HashMap<>();
        object.put(KEY_LABEL, label);
        object.put(KEY_OBJECT, data);
        objects.add(object);
        return true;
    }

    /**
     * Check if an object exist by their label.
     *
     * @param label related identity to object
     * @return boolean that indicate object found or not
     */
    public boolean isObjectAvailable(String label) {
        for (HashMap object : objects) {
            if (object.get(KEY_LABEL).equals(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Delete object on list by label.
     *
     * @param label related label which object want to destroyed
     * @return object which destroyed
     */
    @SuppressWarnings("unused")
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

    /**
     * Find out if and object related by certain label exist.
     *
     * @param label related with object
     * @return object if found and null if not found
     */
    public Object find(String label) {
        for (HashMap object : objects) {
            if (object.get(KEY_LABEL).equals(label)) {
                return object.get(KEY_OBJECT);
            }
        }
        return null;
    }

    /**
     * Check if current pooling is fixed.
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isFixedSize() {
        return isFixedSize;
    }

    /**
     * Check if current pooling is empty.
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return objects.isEmpty();
    }

    /**
     * Count current size object has polled.
     *
     * @return number of object
     */
    public int getSize() {
        return objects.size();
    }

    /**
     * Destroy all objects.
     */
    public void clear() {
        objects.clear();
    }
}
