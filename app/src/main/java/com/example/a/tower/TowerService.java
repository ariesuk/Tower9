package com.example.a.tower;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by a on 2016/4/7.
 */
public class TowerService extends Service {
    public static final String TAG = "TowerService";
    private final TowerBinder mBinder = new TowerBinder();
    private CellTracker mCellTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
        mCellTracker = new CellTracker(this);

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

    public CellTracker getCellTracker() {
        return mCellTracker;
    }
}
