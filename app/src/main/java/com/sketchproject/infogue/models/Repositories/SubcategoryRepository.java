package com.sketchproject.infogue.models.Repositories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sketchproject.infogue.database.DatabaseManager;
import com.sketchproject.infogue.models.Subcategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcategory repository handle CRUD operation on category table.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.52.
 */
public class SubcategoryRepository implements DatabaseManager.PersistDataOperator<Subcategory> {

    /**
     * Create subcategory record.
     *
     * @param data subcategory model
     * @return boolean
     */
    @Override
    public boolean createData(Subcategory data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Subcategory.ID, data.getId());
        values.put(Subcategory.CATEGORY_ID, data.getCategoryId());
        values.put(Subcategory.SUBCATEGORY, data.getSubcategory());
        values.put(Subcategory.LABEL, data.getLabel());

        long newSubCategoryId = db.insert(Subcategory.TABLE, null, values);
        db.close();

        return newSubCategoryId != -1;
    }

    /**
     * Retrieve all subcategory, passing 0 value indicate select all.
     *
     * @return list of subcategory
     */
    @Override
    public List<Subcategory> retrieveData() {
        return retrieveData(0);
    }

    /**
     * Retrieve data by category id.
     *
     * @param categoryId reference of category
     * @return list of subcategory
     */
    public List<Subcategory> retrieveData(int categoryId) {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Subcategory.ID, Subcategory.CATEGORY_ID, Subcategory.SUBCATEGORY, Subcategory.LABEL};

        // How we want the results sorted in the resulting Cursor
        String sortOrder = Subcategory.ID + " ASC";

        Cursor cursor;

        // select all
        if (categoryId <= 0) {
            cursor = db.query(Subcategory.TABLE, projection, null, null, null, null, sortOrder);
        } else { // select by category
            String selection = Subcategory.CATEGORY_ID + " = ?";
            String[] selectionArgs = {String.valueOf(categoryId)};

            cursor = db.query(Subcategory.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }

        List<Subcategory> subcategoryList = new ArrayList<>();
        Subcategory subcategory;

        if (cursor.moveToFirst()) {
            do {
                subcategory = new Subcategory();
                subcategory.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.ID)));
                subcategory.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.CATEGORY_ID)));
                subcategory.setSubcategory(cursor.getString(cursor.getColumnIndex(Subcategory.SUBCATEGORY)));
                subcategory.setLabel(cursor.getString(cursor.getColumnIndex(Subcategory.LABEL)));
                subcategoryList.add(subcategory);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return subcategoryList;
    }

    /**
     * Retrieve subcategory data by id.
     *
     * @param data subcategory model
     * @return subcategory data model
     */
    @Override
    public Subcategory findData(Subcategory data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Subcategory.ID, Subcategory.CATEGORY_ID, Subcategory.SUBCATEGORY, Subcategory.LABEL};

        String selection = Subcategory.ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};

        Subcategory subcategory = new Subcategory();
        Cursor cursor = db.query(Subcategory.TABLE, projection, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            subcategory.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.ID)));
            subcategory.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.CATEGORY_ID)));
            subcategory.setSubcategory(cursor.getString(cursor.getColumnIndex(Subcategory.SUBCATEGORY)));
            subcategory.setLabel(cursor.getString(cursor.getColumnIndex(Subcategory.LABEL)));
        }

        cursor.close();
        db.close();

        return subcategory;
    }

    /**
     * Update subcategory data.
     *
     * @param data      subcategory model
     * @param reference update reference
     * @return boolean
     */
    @Override
    public boolean updateData(Subcategory data, Object reference) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(Subcategory.ID, data.getId());
        values.put(Subcategory.CATEGORY_ID, data.getCategoryId());
        values.put(Subcategory.SUBCATEGORY, data.getSubcategory());
        values.put(Subcategory.LABEL, data.getLabel());

        // Which row to update, based on the ID
        String selection = Subcategory.ID + " = ?";
        String[] selectionArgs = {String.valueOf(reference)};

        int affected = db.update(Subcategory.TABLE, values, selection, selectionArgs);
        db.close();

        return affected > 0;
    }

    /**
     * Delete subcategory by id.
     *
     * @param data subcategory model
     * @return boolean
     */
    @Override
    public boolean deleteData(Subcategory data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        String selection = Subcategory.ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};
        int affectedRows = db.delete(Subcategory.TABLE, selection, selectionArgs);

        db.close();
        return affectedRows > 0;
    }

    /**
     * Remove all subcategory data in database.
     *
     * @return boolean
     */
    public boolean clearData() {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        int affectedRows = db.delete(Subcategory.TABLE, null, null);

        db.close();
        return affectedRows > 0;
    }
}
