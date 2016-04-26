package com.example.a.tower;

/**
 * Created by a on 2016/4/26.
 */


import android.telephony.TelephonyManager;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Tower {

    @Setter
    private int tid;

    /**
     * Location Area Code
     */
    @Setter
    private int lac;

    /**
     * Mobile Country Code
     */
    @Setter
    private int mcc;

    /**
     * Mobile Network Code
     */
    @Setter
    private int mnc;


    /**
     * Timestamp of current cell information
     */
    @Setter
    private long timestamp;

    // Tracked Cell Specific Variables
    /**
     * Current Network Type
     */
    @Setter
    private int netType;

    /**
     * Longitude Geolocation
     */
    @Setter
    private double lon;

    /**
     * Latitude
     */
    @Setter
    private double lat;

    {
        mcc = Integer.MAX_VALUE;
        mnc = Integer.MAX_VALUE;
        lac = Integer.MAX_VALUE;
        tid = Integer.MAX_VALUE;
        netType = Integer.MAX_VALUE;
        lon = 0.0;
        lat = 0.0;
    }

    public Tower() {
    }


    // get readable string from mnc
    public String getReadMNC() {
        switch (getMnc()) {
            case 0:
                return "中国移动";
            case 1:
                return  "中国联通";
            case 2:
                return  "中国电信";
            default:
                return "中国XX";
        }
    }

    public String getRat() {
        return getRatFromInt(this.netType);
    }

    public static String getRatFromInt(int netType) {
        switch (netType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
            default:
                return String.valueOf(netType);
        }
    }
}
