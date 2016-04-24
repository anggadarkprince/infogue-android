package com.sketchproject.infogue.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.34.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        mDatabase = mDatabaseHelper.getWritableDatabase();
        return mDatabase;
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        mDatabase = mDatabaseHelper.getReadableDatabase();
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mDatabase.close();
    }

    public interface PersistDataOperator<T>{
        boolean createData(T data);
        List<T> retrieveData();
        T findData(T data);
        boolean updateData(T data, Object reference);
        boolean deleteData(T data);
    }
}
