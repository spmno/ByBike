package com.spmno.bybike;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.FileNotFoundException;

/**
 * Created by spmno on 13-8-5.
 */
public class BikeModel {
    private static final BikeModel instance = new BikeModel();
    private SQLiteDatabase db;
    private int DB_VERSION = 1;
    private String DB_NAME = "bike";
    private String TABLE_NAME = "record";
    public static BikeModel getInstance() {
        return instance;
    }
    private BikeModel(){

    }

    private void openDatabaseInner() {
        db = SQLiteDatabase.openOrCreateDatabase(DB_NAME, null);
    }

    private void createTable() {
        String sql = "create table " + TABLE_NAME + " ("
                + "id integer primary key autoincrement, "
                + "speed integer not null, "
                + "price integer);";

        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }
    }
}
