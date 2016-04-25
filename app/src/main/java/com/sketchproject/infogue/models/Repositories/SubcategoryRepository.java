package com.sketchproject.infogue.models.Repositories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sketchproject.infogue.database.DatabaseManager;
import com.sketchproject.infogue.models.Subcategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.52.
 */
public class SubcategoryRepository implements DatabaseManager.PersistDataOperator<Subcategory> {

    @Override
    public boolean createData(Subcategory data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Subcategory.COLUMN_ID, data.getId());
        values.put(Subcategory.COLUMN_CATEGORY_ID, data.getCategoryId());
        values.put(Subcategory.COLUMN_SUBCATEGORY, data.getSubcategory());

        long newSubCategoryId = db.insert(Subcategory.TABLE, null, values);
        db.close();

        return newSubCategoryId != -1;
    }

    @Override
    public List<Subcategory> retrieveData() {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Subcategory.COLUMN_ID, Subcategory.COLUMN_CATEGORY_ID, Subcategory.COLUMN_SUBCATEGORY};

        // How we want the results sorted in the resulting Cursor
        String sortOrder = Subcategory.COLUMN_ID + " DESC";

        Cursor cursor = db.query(Subcategory.TABLE, projection, null, null, null, null, sortOrder);

        List<Subcategory> subcategoryList = new ArrayList<>();
        Subcategory subcategory;

        if (cursor.moveToFirst()) {
            do {
                subcategory = new Subcategory();
                subcategory.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.COLUMN_ID)));
                subcategory.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.COLUMN_CATEGORY_ID)));
                subcategory.setSubcategory(cursor.getString(cursor.getColumnIndex(Subcategory.COLUMN_SUBCATEGORY)));
                subcategoryList.add(subcategory);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return subcategoryList;
    }

    @Override
    public Subcategory findData(Subcategory data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Subcategory.COLUMN_ID, Subcategory.COLUMN_CATEGORY_ID, Subcategory.COLUMN_SUBCATEGORY};

        String selection = Subcategory.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};

        Subcategory subcategory = new Subcategory();
        Cursor cursor = db.query(Subcategory.TABLE, projection, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            subcategory.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.COLUMN_ID)));
            subcategory.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(Subcategory.COLUMN_CATEGORY_ID)));
            subcategory.setSubcategory(cursor.getString(cursor.getColumnIndex(Subcategory.COLUMN_SUBCATEGORY)));
        }

        cursor.close();
        db.close();

        return subcategory;
    }

    @Override
    public boolean updateData(Subcategory data, Object reference) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(Subcategory.COLUMN_ID, data.getId());
        values.put(Subcategory.COLUMN_CATEGORY_ID, data.getCategoryId());
        values.put(Subcategory.COLUMN_SUBCATEGORY, data.getSubcategory());

        // Which row to update, based on the ID
        String selection = Subcategory.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(reference)};

        int affected = db.update(Subcategory.TABLE, values, selection, selectionArgs);
        db.close();

        return affected > 0;
    }

    @Override
    public boolean deleteData(Subcategory data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        String selection = Subcategory.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};
        int affectedRows = db.delete(Subcategory.TABLE, selection, selectionArgs);

        db.close();
        return affectedRows > 0;
    }

}
