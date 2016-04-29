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
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

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
    private static TelephonyManager tm;
    private Location mCurrentLocation;
    private Cell mCurrentCell;
    private Device mDevice;
    private DataBaseAdapter mDbAdaper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
        mCellTracker = new CellTracker(this);

        //the timer is not used now, maybe useful in the future
        // startTraceTimer();
        mDbAdaper = new DataBaseAdapter(getApplicationContext());
        mDbAdaper.createDatabase();
        mDbAdaper.open();

        listenLocationChanges();

        tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(phoneStatelistener,
                PhoneStateListener.LISTEN_CELL_LOCATION |         // gpsd_lat/lon ?
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |      // rx_signal
                        PhoneStateListener.LISTEN_DATA_ACTIVITY |         // No,In,Ou,IO,Do
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | // Di,Ct,Cd,Su
                        PhoneStateListener.LISTEN_CELL_INFO               // !? (Need API 17)
        );
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
        mCurrentLocation = location;
        recordNetworkCellOnLocation(location);
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
        timerHandler.postDelayed(tracePhoneLocationRunnable, 5000);
    }

    public void stopTraceTimer() {
        timerHandler.removeCallbacks(tracePhoneLocationRunnable);
    }

    final PhoneStateListener phoneStatelistener = new PhoneStateListener() {
        private void handle() {
        }
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            handle();
        }
        @Override
        public void onDataConnectionStateChanged(int state) {
            handle();
        }
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            handle();
        }
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            handle();
        }
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            handle();
        }
        @Override
        public void onCellLocationChanged(CellLocation location) {
            handle();
            if (mCurrentLocation == null) return; //if we didn't get location, dismiss this time.
            if (mCurrentCell == null) return; //if we didn't get any cell, dismiss now, we will get the first cell by onLocationChanged

            if (mDevice==null) {
                getCellTracker().refreshDevice();
                mDevice = getCellTracker().getDevice();
            }
            switch (mDevice.getPhoneID()) {
                case TelephonyManager.PHONE_TYPE_NONE:
                case TelephonyManager.PHONE_TYPE_SIP:
                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                    if (gsmCellLocation != null) {
                        //device.cell.setLac(gsmCellLocation.getLac());     // LAC
                        //device.cell.setCid(gsmCellLocation.getCid());     // CID
                        // if the cell is diff from mCurrentCell, record it to db
                        if (mCurrentCell.getCid() != gsmCellLocation.getCid() || mCurrentCell.getLac() != gsmCellLocation.getLac()) {
                            recordNetworkCellOnLocation(mCurrentLocation);
                            Toast toast=Toast.makeText(getApplicationContext(),"Found cell by onCellLocationChanged", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                    if (cdmaCellLocation != null) {
                        //device.cell.setLac(cdmaCellLocation.getNetworkId());      // NID
                        //device.cell.setCid(cdmaCellLocation.getBaseStationId());  // BID
                        // if the cell is diff from mCurrentCell, record it to db
                        if (mCurrentCell.getCid() != cdmaCellLocation.getBaseStationId() || mCurrentCell.getLac() != cdmaCellLocation.getNetworkId()) {
                            recordNetworkCellOnLocation(mCurrentLocation);
                        }
                    }
            }
        }

    };

    // some get methods
    public CellTracker getCellTracker() {
        return mCellTracker;
    }
    public Cell getCell() {
        //return
        List<Cell> allCellList = mCellTracker.getAllCells();
        if (allCellList!=null && allCellList.size()>0) {
            return allCellList.get(0);
        }
        else  {
            return  mCellTracker.getDevice().mCell;
        }
    }

    // The DBHelper should be singleton across a app
    public DataBaseAdapter getSingletonDbAdapater() {
        return mDbAdaper;
    }

    private void recordNetworkCellOnLocation(Location location) {
        if(location == null) return;
        Toast toast=Toast.makeText(getApplicationContext(),String.valueOf(location.getLatitude() + "  " + String.valueOf(location.getLongitude())), Toast.LENGTH_SHORT);
        toast.show();

        // get the device info, only for one time
        if (mDevice==null) {
            getCellTracker().refreshDevice();
            mDevice = getCellTracker().getDevice();
        }

        int lastTowerId = 0;
        int lastCellId = 0;
        if (mCurrentCell!=null) {
            lastTowerId = mCurrentCell.getTowerId();
            lastCellId = mCurrentCell.getCid();
        }
        // get the new cell info
        mCurrentCell = getCell();
        mCurrentCell.setLon(location.getLongitude());       // gpsd_lon
        mCurrentCell.setLat(location.getLatitude());        // gpsd_lat

        //insert or update the cell info to the db.


        //table CELLSIGNALHISTROY
        mDbAdaper.insertCellSingalHistory(mCurrentCell,mDevice.getIMEI());

        //table DETECTEDCELLS
        if (!mDbAdaper.cellInDbiBts(mCurrentCell.getLac(), mCurrentCell.getCid())) {
            mDbAdaper.insertBTS(mCurrentCell,mDevice.getIMEI());
        }
        else  {
            // calculate the Cell's Locationg by below algorithm
            double maxLat = mDbAdaper.maxLatOfCellArea(mCurrentCell);
            double minLat = mDbAdaper.minLatOfCellArea(mCurrentCell);
            double maxLon = mDbAdaper.maxLonOfCellArea(mCurrentCell);
            double minLon = mDbAdaper.minLonOfCellArea(mCurrentCell);
            if (maxLat>0 && maxLon>0 && minLat>0 && minLon>0 ) {
                //LatLng newCellLL = new LatLng((maxLat+minLat)/2, (maxLon+minLon)/2);
                mCurrentCell.setLat((maxLat+minLat)/2);
                mCurrentCell.setLon((maxLon+minLon)/2);
                mDbAdaper.updateBTS(mCurrentCell);
            }
        }

        //table DETECTEDTOWERS
        if (!mDbAdaper.cellRelatedTowerInTable(mCurrentCell)){
            mDbAdaper.insertCellRelatedTower(mCurrentCell, mDevice.getIMEI());
        }
        else {
            // calculate the Cell's Location by below algorithm
            double maxLat = mDbAdaper.maxLatOfCellRelatedTowerArea(mCurrentCell);
            double minLat = mDbAdaper.minLatOfCellRelatedTowerArea(mCurrentCell);
            double maxLon = mDbAdaper.maxLonOfCellRelatedTowerArea(mCurrentCell);
            double minLon = mDbAdaper.minLonOfCellRelatedTowerArea(mCurrentCell);
            if (maxLat>0 && maxLon>0 && minLat>0 && minLon>0 ) {
                mCurrentCell.setLat((maxLat+minLat)/2);
                mCurrentCell.setLon((maxLon+minLon)/2);
                mDbAdaper.updateCellRelatedTower(mCurrentCell);
            }
        }

        if(lastTowerId != mCurrentCell.getTowerId()) {
            //切换基站
            Toast toast1=Toast.makeText(getApplicationContext(),"切换基站！", Toast.LENGTH_SHORT);
            toast1.show();
            sendUpdateMapBroadcast();
        }
        else if (lastCellId != mCurrentCell.getCid()) {
            Toast toast2=Toast.makeText(getApplicationContext(),"切换蜂窝！", Toast.LENGTH_SHORT);
            toast2.show();
        }
    }

    private void sendUpdateMapBroadcast() {
        Intent intent = new Intent("TowerLocalEvent");
        // add data
        intent.putExtra("com.example.a.tower.LocalEvent", "updateMap");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
