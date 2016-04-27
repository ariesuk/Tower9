package com.example.a.tower;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import au.com.bytecode.opencsv.CSVWriter;

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
            String sql ="SELECT * FROM " + TowerConstant.registeredStationTable;

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
            else if (tableName.equals(TowerConstant.detectedTowerTable)) {
                condition = " gps_lat > " + String.valueOf(ll_West_South.latitude) + " AND gps_lat < " + String.valueOf(ll_East_North.latitude) +  " AND gps_lon > " + String.valueOf(ll_West_South.longitude) + " AND gps_lon < " + String.valueOf(ll_East_North.longitude);
            }
            else if (tableName.equals(TowerConstant.detectedCellTable)) {
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
        Cursor mCur = mDb.rawQuery("SELECT * FROM " + TowerConstant.detectedCellTable, null);
        if (mCur!=null) {
            mCur.moveToFirst();
        }
        return mCur;
    }

    public Cursor getDetectedCellsByTowerId(int tid) {
        Cursor mCur = mDb.rawQuery(String.format("SELECT * FROM " + TowerConstant.detectedCellTable + " WHERE TOWER = %d",tid), null);
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
        String query = String.format("SELECT CID,LAC FROM " + TowerConstant.detectedCellTable + " WHERE LAC = %d AND CID = %d",
                lac, cellID);
        Cursor cursor = mDb.rawQuery(query, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean cellRelatedTowerInTable(Cell cell) {
        String query = String.format("SELECT LAC,TOWER FROM " + TowerConstant.detectedTowerTable +" WHERE LAC = %d AND TOWER = %d",
                cell.getLac(), cell.getTowerId());
        Cursor cursor = mDb.rawQuery(query, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //if (!cellInDbiBts(cell.getLac(), cell.getCid())) {
    public void insertBTS(Cell cell, String imei) {
        ContentValues values = new ContentValues();

        values.put("IMEI", imei);
        values.put("MCC", cell.getMcc());
        values.put("MNC", cell.getMnc());
        values.put("LAC", cell.getLac());
        values.put("TOWER", cell.getTowerId());
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
        values.put("net_type", cell.getNetType());

        mDb.insert(TowerConstant.detectedCellTable, null, values);
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
        mDb.update(TowerConstant.detectedCellTable, values, "CID=?", new String[]{Integer.toString(cell.getCid())});
    }

    public void insertCellSingalHistory(Cell cell,String imei) {
        ContentValues values = new ContentValues();
        values.put("IMEI", imei);
        values.put("MCC", cell.getMcc());
        values.put("MNC", cell.getMnc());
        values.put("LAC", cell.getLac());
        values.put("CID", cell.getCid());
        values.put("RSS", cell.getDbm());

        values.put("time", getCurrentTimeStamp());
        values.put("gps_lat", cell.getLat());
        values.put("gps_lon", cell.getLon());
        values.put("net_type", cell.getNetType());

        mDb.insert(TowerConstant.cellSignalHistoryTable, null, values);
        //log.info("DBi_bts was populated.");
    }

    //
    public void insertCellRelatedTower(Cell cell, String imei) {
        ContentValues values = new ContentValues();

        values.put("IMEI", imei);
        values.put("MCC", cell.getMcc());
        values.put("MNC", cell.getMnc());
        values.put("LAC", cell.getLac());
        values.put("TOWER", cell.getTowerId());

        values.put("time_first", getCurrentTimeStamp());
        values.put("time_last", getCurrentTimeStamp());

        values.put("gps_lat", cell.getLat());
        values.put("gps_lon", cell.getLon());
        values.put("net_type", cell.getNetType());

        mDb.insert(TowerConstant.detectedTowerTable, null, values);
    }

    public void updateCellRelatedTower(Cell cell) {
        // If tower is already in the DB, update it to last time seen and
        // update its GPS coordinates, if not 0.0
        ContentValues values = new ContentValues();
        values.put("time_last", getCurrentTimeStamp());
        values.put("gps_lat", cell.getLat());
        values.put("gps_lon", cell.getLon());

        mDb.update(TowerConstant.detectedTowerTable, values, "TOWER=?", new String[]{Integer.toString(cell.getTowerId())});
    }


    public double maxLatOfCellArea(Cell cell) {
        String query = String.format("SELECT MAX(gps_lat) FROM " + TowerConstant.cellSignalHistoryTable + " WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
       else {
            return 0;
        }
    }


    public double minLatOfCellArea(Cell cell) {
        String query = String.format("SELECT MIN(gps_lat) FROM " + TowerConstant.cellSignalHistoryTable + " WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public double maxLonOfCellArea(Cell cell) {
        String query = String.format("SELECT MAX(gps_lon) FROM " + TowerConstant.cellSignalHistoryTable + " WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public double minLonOfCellArea(Cell cell) {
        String query = String.format("SELECT MIN(gps_lon) FROM " + TowerConstant.cellSignalHistoryTable + " WHERE LAC = %d AND CID = %d", cell.getLac(), cell.getCid());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public double maxLatOfCellRelatedTowerArea(Cell cell) {
        String query = String.format("SELECT MAX(gps_lat) FROM " + TowerConstant.detectedCellTable + " WHERE LAC = %d AND TOWER = %d", cell.getLac(), cell.getTowerId());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }


    public double minLatOfCellRelatedTowerArea(Cell cell) {
        String query = String.format("SELECT MIN(gps_lat) FROM " + TowerConstant.detectedCellTable + " WHERE LAC = %d AND TOWER = %d", cell.getLac(), cell.getTowerId());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public double maxLonOfCellRelatedTowerArea(Cell cell) {
        String query = String.format("SELECT MAX(gps_lon) FROM " + TowerConstant.detectedCellTable + " WHERE LAC = %d AND TOWER = %d", cell.getLac(), cell.getTowerId());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public double minLonOfCellRelatedTowerArea(Cell cell) {
        String query = String.format("SELECT MIN(gps_lon) FROM " + TowerConstant.detectedCellTable + " WHERE LAC = %d AND TOWER = %d", cell.getLac(), cell.getTowerId());
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        else {
            return 0;
        }
    }

    public boolean backupDB() {
        String[] exportTables =  new String[]{
            TowerConstant.detectedTowerTable,
            TowerConstant.detectedCellTable,
            TowerConstant.cellSignalHistoryTable,
        };

        try {
            //backup the tables
            for (String table : exportTables) {
                backup(table);
            }
            //backup the whole db
            exportDatabse("towerclient.sqlite");
            return true;
        } catch (Exception ioe) {
            Log.e(TAG, "BackupDB() Error: ");
            return false;
        }
    }

    private void backup(String tableName) {
        String externalFilesDirPath = mContext.getExternalFilesDir(null) + File.separator;
        File dir = new File(externalFilesDirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Backup(): Cannot create directory structure to " + dir.getAbsolutePath());
            }
        }  // We should probably add some more error handling here.
        File file = new File(dir, "Tower-" + tableName + ".csv");

        try {
            Log.i(TAG, "Backup(): Backup file was created? " + file.createNewFile());
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Log.d(TAG, "DB backup() tableName: " + tableName);

            Cursor c = mDb.rawQuery("SELECT * FROM " + tableName, new String[0]);

            csvWrite.writeNext(c.getColumnNames());
            String[] rowData = new String[c.getColumnCount()];
            int size = c.getColumnCount();

            while (c.moveToNext()) {
                for (int i = 0; i < size; i++) {
                    String columnName = c.getColumnName(i);
                    if (columnName.equals("gps_lat") || columnName.equals("gps_lon")) {
                        rowData[i] = String.valueOf(c.getDouble(i));
                    }
                    else {
                        rowData[i] = c.getString(i);
                    }
                }
                csvWrite.writeNext(rowData);
            }
            csvWrite.close();
            c.close();

        } catch (Exception e) {
            Log.e(TAG, "Error exporting table: " + tableName, e);
        }
        Log.i(TAG, "Backup(): Successfully exported DB table to: " + file);
    }

    public void exportDatabse(String databaseName) {
        try {
            if (Environment.getExternalStorageDirectory().canWrite()) {
                String currentDBPath = "";
                if(android.os.Build.VERSION.SDK_INT >= 17){
                    currentDBPath = mContext.getApplicationInfo().dataDir + "/databases/" + databaseName;
                }
                else
                {
                    currentDBPath = "/data/data/" + mContext.getPackageName() + "/databases/" + databaseName;
                }
                String backupDBPath = "backup_towerclient.sqlite";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(mContext.getExternalFilesDir(null), backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
    }
}
