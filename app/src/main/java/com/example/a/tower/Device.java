package com.example.a.tower;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**
 * Created by a on 2016/4/7.
 */
public class Device {

    public Cell mCell;
    private int mPhoneID = -1;
    private String mCellInfo;
    private String mDataState;
    private String mDataStateShort;
    private String mNetName;
    private String mMncmcc;
    private String mSimCountry;
    private String mPhoneType;
    private String mIMEI;
    private String mIMEIV;
    private String mSimOperator;
    private String mSimOperatorName;
    private String mSimSerial;
    private String mSimSubs;
    private String mDataActivityType;
    private String mDataActivityTypeShort;
    private boolean mRoaming;

    private Location mLastLocation;

    /**
     * Refreshes all device specific details
     */
    public void refreshDeviceInfo(TelephonyManager tm, Context context) {

        //Phone type and associated details
        mIMEI = tm.getDeviceId();
        mIMEIV = tm.getDeviceSoftwareVersion();
        mPhoneID = tm.getPhoneType();
        mRoaming = tm.isNetworkRoaming();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            DeviceApi18.loadCellInfo(tm, this);
        }

        if (mCell == null) {
            mCell = new Cell();
        }

        switch (mPhoneID) {

            case TelephonyManager.PHONE_TYPE_NONE:
            case TelephonyManager.PHONE_TYPE_SIP:
            case TelephonyManager.PHONE_TYPE_GSM:
                mPhoneType = "GSM";
                mMncmcc = tm.getNetworkOperator();
                if (mMncmcc != null && mMncmcc.length() >= 5) {
                    try {
                        if (mCell.getMCC() == Integer.MAX_VALUE) {
                            mCell.setMCC(Integer.parseInt(tm.getNetworkOperator().substring(0, 3)));
                        }
                        if (mCell.getMNC() == Integer.MAX_VALUE) {
                            mCell.setMNC(Integer.parseInt(tm.getNetworkOperator().substring(3, 5)));
                        }
                    } catch (Exception e) {
                        Log.d("", "MncMcc parse exception: ", e);
                    }
                }
                mNetName = tm.getNetworkOperatorName();
                if (!mCell.isValid()) {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                    if (gsmCellLocation != null) {
                        mCell.setCID(gsmCellLocation.getCid());
                        mCell.setLAC(gsmCellLocation.getLac());
                        mCell.setPSC(gsmCellLocation.getPsc());
                    }
                }
                break;

            case TelephonyManager.PHONE_TYPE_CDMA:
                mPhoneType = "CDMA";
                if (!mCell.isValid()) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) tm.getCellLocation();
                    if (cdmaCellLocation != null) {
                        mCell.setCID(cdmaCellLocation.getBaseStationId());
                        mCell.setLAC(cdmaCellLocation.getNetworkId());
                        mCell.setSID(cdmaCellLocation.getSystemId()); // one of these must be a bug !!
                        // See: http://stackoverflow.com/questions/8088046/android-how-to-identify-carrier-on-cdma-network
                        // and: https://github.com/klinker41/android-smsmms/issues/26
                        mCell.setMNC(cdmaCellLocation.getSystemId()); // todo: check! (Also CellTracker.java)

                        //Retrieve MCC through System Property
                        /*
                        String homeOperator = Helpers.getSystemProp(context,
                                "ro.cdma.home.operator.numeric", "UNKNOWN");
                        if (!homeOperator.contains("UNKNOWN")) {
                            try {
                                if (mCell.getMCC() == Integer.MAX_VALUE) {
                                    mCell.setMCC(Integer.valueOf(homeOperator.substring(0, 3)));
                                }
                                if (mCell.getMNC() == Integer.MAX_VALUE) {
                                    mCell.setMNC(Integer.valueOf(homeOperator.substring(3, 5)));
                                }
                            } catch (Exception e) {
                                Log.d("", "HomeOperator parse exception - " + e.getMessage(), e);
                            }
                        }
                        */
                    }
                }
                break;
        }

        // SIM Information
        mSimCountry = getSimCountry(tm);
        // Get the operator code of the active SIM (MCC + MNC)
        mSimOperator = getSimOperator(tm);
        mSimOperatorName = getSimOperatorName(tm);
        mSimSerial = getSimSerial(tm);
        mSimSubs = getSimSubs(tm);

        mDataActivityType = getDataActivity(tm);
        mDataState = getDataState(tm);
    }

    public String getCellInfo() {
        return mCellInfo;
    }

    public void setCellInfo(String cellInfo) {
        mCellInfo = cellInfo;
    }

    /**
     * Phone Type ID
     *
     * @return integer representation of Phone Type
     */
    public int getPhoneID() {
        return mPhoneID;
    }

    /**
     * SIM Country
     *
     * @return string of SIM Country data
     */
    public String getSimCountry(final TelephonyManager tm) {
        return tm.getSimCountryIso();
    }

    /**
     * SIM Country data
     */
    public String getSimCountry() {
        return mSimCountry;
    }

    /**
     * SIM Operator
     *
     * @return string of SIM Operator data
     */
    public String getSimOperator(final TelephonyManager tm) {
        return tm.getSimOperator();
    }

    public String getSimOperator() {
        return mSimOperator;
    }

    /**
     * SIM Operator Name
     *
     * @return string of SIM Operator Name
     */
    public String getSimOperatorName(final TelephonyManager tm) {
        return  tm.getSimOperatorName();
    }

    public String getSimOperatorName() {
        return mSimOperatorName;
    }

    /**
     * SIM Subscriber ID
     *
     * @return string of SIM Subscriber ID data
     */
    public String getSimSubs(final TelephonyManager tm) {
        return  tm.getSubscriberId();
    }

    public String getSimSubs() {
        return mSimSubs;
    }

    /**
     * SIM Serial Number
     *
     * @return string of SIM Serial Number data
     */
    public String getSimSerial(final TelephonyManager tm) {
        return tm.getSimSerialNumber();
    }

    public String getSimSerial() {
        return mSimSerial;
    }

    public String getPhoneType() {
        return mPhoneType;
    }

    /**
     * IMEI
     *
     * @return string representing device IMEI
     */
    public String getIMEI() {
        return mIMEI;
    }

    /**
     * IMEI Version / Device Software Version
     *
     * @return string representing device IMEI Version
     */
    public String getIMEIv() {
        return mIMEIV;
    }

    /**
     * Sets Network Operator Name
     *
     */
    public void setNetworkName(String networkName) {
        mNetName = networkName;
    }

    public String getNetworkName() {
        return mNetName;
    }

    /**
     * Network Operator
     *
     * @return string representing the Network Operator
     */
    public String getMncMcc() {
        return mMncmcc;
    }

    /**
     * Network Type
     *
     * @return string representing device Network Type
     */
    public String getNetworkTypeName() {
        if (mCell == null) {
            return "Unknown";
        }

        return mCell.getRAT();
    }

    String getDataActivity(TelephonyManager tm) {
        int direction = tm.getDataActivity();
        mDataActivityTypeShort = "un";
        mDataActivityType = "undef";

        switch (direction) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                mDataActivityTypeShort = "No";
                mDataActivityType = "None";
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                mDataActivityTypeShort = "In";
                mDataActivityType = "In";
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                mDataActivityTypeShort = "Ou";
                mDataActivityType = "Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                mDataActivityTypeShort = "IO";
                mDataActivityType = "In-Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                mDataActivityTypeShort = "Do";
                mDataActivityType = "Dormant";
                break;
        }

        return mDataActivityType;
    }

    public String getDataActivity() {
        return mDataActivityType;
    }

    String getDataState(TelephonyManager tm) {
        int state = tm.getDataState();
        mDataState = "undef";
        mDataStateShort = "un";
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                mDataState = "Disconnected";
                mDataStateShort = "Di";
                break;
            case TelephonyManager.DATA_CONNECTING:
                mDataState = "Connecting";
                mDataStateShort = "Ct";
                break;
            case TelephonyManager.DATA_CONNECTED:
                mDataState = "Connected";
                mDataStateShort = "Cd";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                mDataState = "Suspended";
                mDataStateShort = "Su";
                break;
        }

        return mDataState;
    }

    public String getDataState() {
        return mDataState;
    }

    public String getDataActivityTypeShort() {
        return mDataActivityTypeShort;
    }

    public void setDataActivityTypeShort(String dataActivityTypeShort) {
        mDataActivityTypeShort = dataActivityTypeShort;
    }

    public String getDataStateShort() {
        return mDataStateShort;
    }

    public void setDataStateShort(String dataStateShort) {
        mDataStateShort = dataStateShort;
    }

    public void setDataActivityType(String dataActivityType) {
        mDataActivityType = dataActivityType;
    }

    public void setDataState(String dataState) {
        mDataState = dataState;
    }

    public void setSignalDbm(int signalDbm) {
        mCell.setDBM(signalDbm);
    }

    public int getSignalDBm() {
        return mCell.getDBM();
    }

    /**
     * Update Network Type
     */
    public void setNetID(TelephonyManager tm) {
        mCell.setNetType(tm.getNetworkType());
    }

    /**
     * Mobile Roaming
     *
     * @return string representing Roaming status (True/False)
     */
    public String isRoaming() {
        return String.valueOf(mRoaming);
    }

    public void setLastLocation(Location location) {
        mLastLocation = location;
    }

    /**
     * Attempts to retrieve the Last Known Location from the device
     *
     * @return Cell object representing last known location
     */
    public Location getLastLocation() {
        return mLastLocation;
    }
}