package com.example.a.tower;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by a on 2016/4/7.
 */
public class CellTracker {
    public static Cell mMonitorCell;
    private final Device mDevice = new Device();
    public static int PHONE_TYPE;
    private static TelephonyManager tm;
    private PhoneStateListener mPhoneStateListener;
    private static Context context;

    public CellTracker(Context context) {
        this.context = context;
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PHONE_TYPE = tm.getPhoneType(); // PHONE_TYPE_GSM/CDMA/SIP/NONE
        mDevice.refreshDeviceInfo(tm, context); // Telephony Manager
        mMonitorCell = new Cell();
    }

    public Device getDevice() {
        return mDevice;
    }

    public void refreshDevice() {
        mDevice.refreshDeviceInfo(tm, context);
    }
}
