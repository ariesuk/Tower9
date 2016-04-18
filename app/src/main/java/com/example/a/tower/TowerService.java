package com.example.a.tower;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by a on 2016/4/7.
 */
public class TowerService extends Service  implements LocationListener{
    public static final String TAG = "TowerService";
    private static final long GPS_MIN_UPDATE_TIME = 10000;
    private static final float GPS_MIN_UPDATE_DISTANCE = 10;
    private final Handler timerHandler = new Handler();
    private final TowerBinder mBinder = new TowerBinder();
    private CellTracker mCellTracker;
    private LocationManager locationManager;
    private LatLng lastPhoneLocation;
    private LatLng newPhoneLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
        mCellTracker = new CellTracker(this);

        //the timer is not used now, maybe useful in the future
        // startTraceTimer();
        listenLocationChanges();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
        //mCellTracker.stop();
        //stopTraceTimer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class TowerBinder extends Binder {
        public TowerService getService() {
                return TowerService.this;
        }

        public void doSomething() {
            Log.d("TAG", "doSomething() executed");
        }
    }

    //Start listen to Location Changes
    public void listenLocationChanges() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_UPDATE_TIME,
                    GPS_MIN_UPDATE_DISTANCE, this);
        } catch (SecurityException e) {
            Log.d(TAG,"GPS location provider doesnt exist");
        }

/*
        try {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, GPS_MIN_UPDATE_TIME,
                    GPS_MIN_UPDATE_DISTANCE, this);
        } catch (SecurityException e) {
            Log.d(TAG, "Passive location provider doesnt exist");
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_MIN_UPDATE_TIME,
                    GPS_MIN_UPDATE_DISTANCE, this);
        } catch (SecurityException e) {
            Log.d(TAG, "Network location provider doesnt exist");
        }
*/

    }
    //LocationListener
    @Override
    public void onLocationChanged(Location location) {
        if(location == null) return;
        Log.d("Latitude::", String.valueOf(location.getLatitude()));
        Toast toast=Toast.makeText(getApplicationContext(),String.valueOf(location.getLatitude() + "  " + String.valueOf(location.getLongitude())), Toast.LENGTH_SHORT);
        toast.show();

        // get the cell info
        getCellTracker().refreshDevice();
        Cell cell = getCell();
        cell.setLon(location.getLongitude());       // gpsd_lon
        cell.setLat(location.getLatitude());        // gpsd_lat

        //insert or update the cell info to the db.
        DataBaseAdapter mDbHelper = new DataBaseAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        //table CELLSIGNALHISTROY
        mDbHelper.insertCellSingalHistory(cell);

        if (!mDbHelper.cellInDbiBts(cell.getLac(), cell.getCid())) {
            mDbHelper.insertBTS(cell);
        }
        else  {
            // calculate the Cell's Locationg by below algorithm
            double maxLat = mDbHelper.maxLatOfCellArea(cell);
            double minLat = mDbHelper.minLatOfCellArea(cell);
            double maxLon = mDbHelper.maxLonOfCellArea(cell);
            double minLon = mDbHelper.minLonOfCellArea(cell);
            if (maxLat>0 && maxLon>0 && minLat>0 && minLon>0 ) {
                LatLng newCellLL = new LatLng((maxLat+minLat)/2, (maxLon+minLon)/2);
                cell.setLat((maxLat+minLat)/2);
                cell.setLon((maxLon+minLon)/2);
                mDbHelper.updateBTS(cell);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    // we define a runnable, is it useful in the future?
    private final Runnable tracePhoneLocationRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.d("trace some thing in service...","........");
            //
            //record the cell info to the table CELLSIGNALHISTROY
            //
            timerHandler.postDelayed(this, 5000);
        }
    };

    public void startTraceTimer() {
        stopTraceTimer();
        timerHandler.postDelayed(tracePhoneLocationRunnable,5000);
    }

    public void stopTraceTimer() {
        timerHandler.removeCallbacks(tracePhoneLocationRunnable);
    }

    // some get methods
    public CellTracker getCellTracker() {
        return mCellTracker;
    }
    public Cell getCell() {
        return mCellTracker.getDevice().mCell;
    }
}
