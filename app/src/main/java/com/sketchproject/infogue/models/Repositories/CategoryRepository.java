package com.sketchproject.infogue.models.Repositories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sketchproject.infogue.database.DatabaseManager;
import com.sketchproject.infogue.models.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.47.
 */
public class CategoryRepository implements DatabaseManager.PersistDataOperator<Category> {

    @Override
    public boolean createData(Category data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Category.ID, data.getId());
        values.put(Category.CATEGORY, data.getCategory());

        long newCategoryId = db.insert(Category.TABLE, null, values);
        db.close();

        return newCategoryId != -1;
    }

    @Override
    public List<Category> retrieveData() {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Category.ID, Category.CATEGORY};

        // How we want the results sorted in the resulting Cursor
        String sortOrder = Category.ID + " ASC";

        Cursor cursor = db.query(
                Category.TABLE,     // The table to query
                projection,         // The columns to return
                null,               // The columns for the WHERE clause
                null,               // The values for the WHERE clause
                null,               // don't group the rows
                null,               // don't filter by row groups
                sortOrder           // The sort order
        );

        List<Category> categoryList = new ArrayList<>();
        Category category;

        if (cursor.moveToFirst()) {
            do {
                category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Category.ID)));
                category.setCategory(cursor.getString(cursor.getColumnIndex(Category.CATEGORY)));
                categoryList.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return categoryList;
    }

    @Override
    public Category findData(Category data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Category.ID, Category.CATEGORY};

        String selection = Category.ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};

        Category category = new Category();
        Cursor cursor = db.query(Category.TABLE, projection, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Category.ID)));
            category.setCategory(cursor.getString(cursor.getColumnIndex(Category.CATEGORY)));
        }

        cursor.close();
        db.close();

        return category;
    }

    @Override
    public boolean updateData(Category data, Object reference) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(Category.ID, data.getId());
        values.put(Category.CATEGORY, data.getCategory());

        // Which row to update, based on the ID
        String selection = Category.ID + " = ?";
        String[] selectionArgs = {String.valueOf(reference)};

        int affected = db.update(Category.TABLE, values, selection, selectionArgs);
        db.close();

        return affected > 0;
    }

    @Override
    public boolean deleteData(Category data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        String selection = Category.ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};
        int affectedRows = db.delete(Category.TABLE, selection, selectionArgs);

        db.close();
        return affectedRows > 0;
    }

    public boolean clearData(){
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        int affectedRows = db.delete(Category.TABLE, null, null);

        db.close();
        return affectedRows > 0;
    }
}
