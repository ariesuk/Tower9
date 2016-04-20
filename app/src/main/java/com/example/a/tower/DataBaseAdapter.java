package com.example.a.tower;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by a on 2016/4/12.
 */
public class DataBaseAdapter {
    protected static final String TAG = "DataBaseAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public DataBaseAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public DataBaseAdapter createDatabase() throws SQLException
    {
        try
        {
            mDbHelper.createDataBase();
        }
        catch (IOException mIOException)
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataBaseAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    public Cursor getTestData()
    {
        try
        {
            String sql ="SELECT * FROM REGISTEREDSTATION5";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>" + mSQLException.toString());
            throw mSQLException;
        }
    }

    public  Cursor getStationsByGpsScope(String tableName, LatLng ll_West_South, LatLng ll_East_North)
    {
        try
        {
            String condition;
            if (tableName.equals(TowerConstant.registeredStationTable)) {
                condition = " LATITUDE > " + String.valueOf(ll_West_South.latitude) + " AND LATITUDE < " + String.valueOf(ll_East_North.latitude) +  " AND LONGITUDE > " + String.valueOf(ll_West_South.longitude) + " AND LONGITUDE < " + String.valueOf(ll_East_North.longitude);
            }
            else if (tableName.equals(TowerConstant.detectedStationTable)) {
                condition = " gps_lat > " + String.valueOf(ll_West_South.latitude) + " AND gps_lat < " + String.valueOf(ll_East_North.latitude) +  " AND gps_lon > " + String.valueOf(ll_West_South.longitude) + " AND gps_lon < " + String.valueOf(ll_East_North.longitude);
            }
            else {
                return null;
            }
            String sql ="SELECT * FROM " + tableName +" WHERE " + condition;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getStationsByGpsScope >>" + mSQLException.toString());
            throw mSQLException;
        }
    }


    public Cursor returnDetectedStations() {
        Cursor mCur = mDb.rawQuery("SELECT * FROM " + TowerConstant.detectedStationTable, null);
        if (mCur!=null) {
            mCur.moveToFirst();
        }
        return mCur;
    }

    public static String getCurrentTimeStamp() {
        //yyyyMMddHHmmss <-- this format is needed for OCID upload
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
    }

    public boolean cellInDbiBts(int lac, int cellID) {
        String query = String.format("SELECT CID,LAC FROM DBi_bts WHERE LAC = %d AND CID = %d",
                lac, cellID);
        Cursor cursor = mDb.rawQuery(query, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //if (!cellInDbiBts(cell.getLac(), cell.getCid())) {
    public void insertBTS(Cell cell) {
        ContentValues values = new ContentValues();
        values.put("MCC", cell.getMcc());
        values.put("MNC", cell.getMnc());
        values.put("LAC", cell.getLac());
        values.put("CID", cell.getCid());
        values.put("PSC", cell.getPsc());

        // TODO: setting these to 0 because if empty causing error in DB Restore
        values.put("T3212", 0);  // TODO
        values.put("A5x", 0);    // TODO
        values.put("ST_id", 0);  // TODO

        values.put("time_first", getCurrentTimeStamp());
        values.put("time_last", getCurrentTimeStamp());

        values.put("gps_lat", cell.getLat());  // TODO NO! These should be exact GPS from DBe_import or by manual addition!
        values.put("gps_lon", cell.getLon());  // TODO NO! These should be exact GPS from DBe_import or by manual addition!

        mDb.insert("DBi_bts", null, values);
        //log.info("DBi_bts was populated.");

    }

    public void updateBTS(Cell cell) {
        // If cell is already in the DB, update it to last time seen and
        // update its GPS coordinates, if not 0.0
        ContentValues values = new ContentValues();
        values.put("time_last", getCurrentTimeStamp());
        values.put("gps_lat", cell.getLat());
        values.put("gps_lon", cell.getLon());
        // This is the native update equivalent to:
        // "UPDATE Dbi_bts time_last=...,gps_lat=..., gps_lon=... WHERE CID=..."
        // update (String table, ContentValues values, String whereClause, String[] whereArgs)
        mDb.update("DBi_bts", values, "CID=?", new String[]{Integer.toString(cell.getCid())});
    }

    public void insertCellSingalHistory(Cell cell) {
        ContentValues values = new ContentValues();
        values.put("MCC", cell.getMcc());
        values.put("MNC", cell.getMnc());
        values.put("LAC", cell.getLac());
        values.put("CID", cell.getCid());
        values.put("RSS", cell.getDbm());

        values.put("time", getCurrentTimeStamp());
        values.put("gps_lat", cell.getLat());
        values.put("gps_lon", cell.getLon());

        mDb.insert(TowerConstant.cellSignalHistoryTable, null, values);
        //log.info("DBi_bts was populated.");
    }

    public double maxLatOfCellArea(Cell cell) {
        String query = String.format("SELECT MAX(gps_lat) FROM CELLSIGNALHISTROY WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
       else {
            return 0;
        }
    }


    public double minLatOfCellArea(Cell cell) {
        String query = String.format("SELECT MIN(gps_lat) FROM CELLSIGNALHISTROY WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public double maxLonOfCellArea(Cell cell) {
        String query = String.format("SELECT MAX(gps_lon) FROM CELLSIGNALHISTROY WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public double minLonOfCellArea(Cell cell) {
        String query = String.format("SELECT MIN(gps_lon) FROM CELLSIGNALHISTROY WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }
}
