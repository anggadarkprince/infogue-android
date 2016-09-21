package com.sketchproject.infogue.models.Repositories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.sketchproject.infogue.database.DatabaseManager;
import com.sketchproject.infogue.models.Bank;

import java.util.ArrayList;
import java.util.List;

/**
 * Bank repository handle CRUD operation on banks table.
 * <p/>
 * Sketch Project Studio
 * Created by Angga on 21/09/2016 09.52.
 */
public class BankRepository implements DatabaseManager.PersistDataOperator<Bank> {

    public boolean isEmpty() {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();
        String count = "SELECT COUNT(*) FROM " + Bank.TABLE;
        Cursor mCursor = db.rawQuery(count, null);
        mCursor.moveToFirst();
        int iCount = mCursor.getInt(0);
        mCursor.close();
        db.close();
        return (iCount <= 0);
    }

    /**
     * Create bank record.
     *
     * @param data bank model
     * @return boolean
     */
    @Override
    public boolean createData(Bank data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        db.beginTransaction();

        SQLiteStatement stmt = db.compileStatement("insert into " + Bank.TABLE + " (" + Bank.ID + ", " + Bank.BANK + ", " + Bank.LOGO + ") values (?, ?, ?);");

        stmt.bindString(1, String.valueOf(data.getId()));
        stmt.bindString(2, String.valueOf(data.getBank()));
        stmt.bindString(3, String.valueOf(data.getLogo()));
        long newBankId = stmt.executeInsert();
        stmt.clearBindings();

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return newBankId != -1;
    }

    /**
     * Insert a lot of bank data.
     *
     * @param banks collection of Bank objects
     * @return status
     */
    public boolean createAllData(List<Bank> banks) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();
        db.beginTransaction();
        SQLiteStatement stmt = db.compileStatement("insert into " + Bank.TABLE + " (" + Bank.ID + ", " + Bank.BANK + ", " + Bank.LOGO + ") values (?, ?, ?);");
        for (int i = 0; i < banks.size(); i++) {
            stmt.bindString(1, String.valueOf(banks.get(i).getId()));
            stmt.bindString(2, String.valueOf(banks.get(i).getBank()));
            stmt.bindString(3, String.valueOf(banks.get(i).getLogo()));
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return true;
    }

    /**
     * Retrieve all subcategory, passing 0 value indicate select all.
     *
     * @return list of banks
     */
    public List<Bank> retrieveData() {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Bank.ID, Bank.BANK, Bank.LOGO};

        // How we want the results sorted in the resulting Cursor
        String sortOrder = Bank.ID + " ASC";

        Cursor cursor;

        // select all
        cursor = db.query(Bank.TABLE, projection, null, null, null, null, sortOrder);

        List<Bank> bankList = new ArrayList<>();
        Bank bank;

        if (cursor.moveToFirst()) {
            do {
                bank = new Bank();
                bank.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Bank.ID)));
                bank.setBank(cursor.getString(cursor.getColumnIndexOrThrow(Bank.BANK)));
                bank.setLogo(cursor.getString(cursor.getColumnIndex(Bank.LOGO)));
                bankList.add(bank);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return bankList;
    }

    /**
     * Retrieve subcategory data by id.
     *
     * @param data subcategory model
     * @return subcategory data model
     */
    @Override
    public Bank findData(Bank data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {Bank.ID, Bank.BANK, Bank.LOGO};

        String selection = Bank.ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};

        Bank bank = new Bank();
        Cursor cursor = db.query(Bank.TABLE, projection, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            bank.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Bank.ID)));
            bank.setBank(cursor.getString(cursor.getColumnIndexOrThrow(Bank.BANK)));
            bank.setLogo(cursor.getString(cursor.getColumnIndex(Bank.LOGO)));
        }

        cursor.close();
        db.close();

        return bank;
    }

    /**
     * Update subcategory data.
     *
     * @param data      subcategory model
     * @param reference update reference
     * @return boolean
     */
    @Override
    public boolean updateData(Bank data, Object reference) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(Bank.ID, data.getId());
        values.put(Bank.BANK, data.getBank());
        values.put(Bank.LOGO, data.getLogo());

        // Which row to update, based on the ID
        String selection = Bank.ID + " = ?";
        String[] selectionArgs = {String.valueOf(reference)};

        int affected = db.update(Bank.TABLE, values, selection, selectionArgs);
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
    public boolean deleteData(Bank data) {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        String selection = Bank.ID + " = ?";
        String[] selectionArgs = {String.valueOf(data.getId())};
        int affectedRows = db.delete(Bank.TABLE, selection, selectionArgs);

        db.close();
        return affectedRows > 0;
    }

    /**
     * Remove all banks data in database.
     *
     * @return boolean
     */
    public boolean clearData() {
        SQLiteDatabase db = DatabaseManager.getInstance().getWritableDatabase();

        int affectedRows = db.delete(Bank.TABLE, null, null);

        db.close();
        return affectedRows > 0;
    }
}
