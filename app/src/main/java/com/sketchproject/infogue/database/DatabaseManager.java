package com.sketchproject.infogue.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * SQLite database manager.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 24/04/2016 09.34.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    /**
     * Initialize database helper and this manager.
     *
     * @param helper child of SQLiteOpenHelper class
     */
    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    /**
     * Retrieve single instance design pattern.
     *
     * @return DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    /**
     * Get rewritable database SQLiteDatabase.
     *
     * @return SQLiteDatabase
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        mDatabase = mDatabaseHelper.getWritableDatabase();
        return mDatabase;
    }

    /**
     * Get readable database SQLiteDatabase.
     *
     * @return SQLiteDatabase
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
        mDatabase = mDatabaseHelper.getReadableDatabase();
        return mDatabase;
    }

    /**
     * Close database
     */
    @SuppressWarnings("unused")
    public synchronized void closeDatabase() {
        mDatabase.close();
    }

    /**
     * Contract CRUD data operation.
     *
     * @param <T>
     */
    public interface PersistDataOperator<T> {
        /**
         * Insert data into database.
         *
         * @param data model T
         * @return boolean
         */
        boolean createData(T data);

        /**
         * Retrieve all data from database.
         *
         * @return List<T>
         */
        List<T> retrieveData();

        /**
         * Find data by reference
         *
         * @param data model T
         * @return object model T
         */
        @SuppressWarnings("unused")
        T findData(T data);

        /**
         * Update data on database.
         *
         * @param data      model T
         * @param reference to perform update
         * @return boolean
         */
        @SuppressWarnings("unused")
        boolean updateData(T data, Object reference);

        /**
         * Delete data from database.
         *
         * @param data model T
         * @return boolean
         */
        @SuppressWarnings("unused")
        boolean deleteData(T data);
    }
}
