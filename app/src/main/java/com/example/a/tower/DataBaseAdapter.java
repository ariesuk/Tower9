package com.example.a.tower;

import java.io.IOException;
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

    public  Cursor getStationsByGpsScope(LatLng ll_West_South, LatLng ll_East_North)
    {
        try
        {
            String condition = " LATITUDE > " + String.valueOf(ll_West_South.latitude) + " AND LATITUDE < " + String.valueOf(ll_East_North.latitude) +  " AND LONGITUDE > " + String.valueOf(ll_West_South.longitude) + " AND LONGITUDE < " + String.valueOf(ll_East_North.longitude);
            String sql ="SELECT * FROM REGISTEREDSTATION5 WHERE " + condition;

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
}
