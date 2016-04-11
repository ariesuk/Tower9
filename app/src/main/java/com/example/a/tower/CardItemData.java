package com.example.a.tower;

import com.example.a.tower.Cell;
/**
 * Created by a on 2016/4/9.
 */
public class CardItemData {
    // OLD (in old DB tables)
    private final String cellId;
    private final String lac;
    private final String mcc;
    private final String mnc;
    private final String net;
    private final String signal;
    private final String avgSigStr;
    private final String samples;
    private final String lat;
    private final String lon;
    private final String country;
    private final String psc;
    private final String timestamp;
    private final String recordId;

    // NEW (in new DB tables)
/*
    private final String mtime;
    private final String mLAC;
    private final String mCID;
    private final String mPSC;
    private final String mgpsd_lat;
    private final String mgpsd_lon;
    private final String mgpsd_accu;
    private final String mDF_id;
    private final String mDF_description;
*/


    // OLD items in old DB table structure

    public CardItemData(Cell cell, String recordId) {

        if (cell.getCid() != Integer.MAX_VALUE && cell.getCid() != -1) {
            cellId = "CID: " + cell.getCid() + "  (0x" + Integer.toHexString(cell.getCid()) + ")";
        } else {
            cellId = "N/A";
        }

        if (cell.getLac() != Integer.MAX_VALUE && cell.getLac() != -1) {
            lac = "LAC: " + cell.getLac();
        } else {
            lac = "N/A";
        }

        if (cell.getMcc() != Integer.MAX_VALUE && cell.getMcc() != 0) {
            mcc = "MCC: " + cell.getMcc();
        } else {
            mcc = "N/A";
        }

        if (cell.getMnc() != Integer.MAX_VALUE && cell.getMnc() != 0) {
            mnc = "MNC: " + cell.getMnc();
        } else {
            mnc = "N/A";
        }

        if (cell.getNetType() != Integer.MAX_VALUE && cell.getNetType() != -1) {
            net = "Type: " + cell.getNetType() + " - " + cell.getRat();
        } else {
            net = "N/A";
        }

        if (cell.getPsc() != Integer.MAX_VALUE && cell.getPsc() != -1) {
            psc = "PSC: " + cell.getPsc();
        } else {
            psc = "N/A";
        }

        if (cell.getRssi() != Integer.MAX_VALUE && cell.getRssi() != -1) {
            signal = "RSSI: " + cell.getRssi();
        } else if (cell.getDbm() != Integer.MAX_VALUE && cell.getDbm() != -1) {
            signal = "dBm: " + cell.getDbm();
        } else {
            signal = "N/A";
        }
        // NEW (in new DB tables)


        // end New

        lat = "N/A";
        lon = "N/A";
        avgSigStr = "N/A";
        samples = "N/A";
        country = "N/A";
        timestamp = "N/A";
        this.recordId = recordId;

        // NEW (in new DB tables)

        // end New

    }

    public CardItemData(String cellID, String lac, String mcc, String mnc, String lat, String lng,
                        String avgSigStr, String samples, String recordId) {
        cellId = cellID;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        net = "Network Type: N/A";
        this.lat = lat;
        lon = lng;
        signal = "Signal: N/A";
        this.avgSigStr = avgSigStr;
        this.samples = samples;
        psc = "PSC: N/A";
        country = "Country: N/A";
        timestamp = "Timestamp: N/A";
        this.recordId = recordId;
    }

    public CardItemData(String cellID, String psc, String mcc, String mnc, String signal,
                        String recordId) {
        cellId = cellID;
        lac = "LAC: N/A";
        this.mcc = mcc;
        this.mnc = mnc;
        lat = "Latitude: N/A";
        lon = "Longitude: N/A";
        net = "Network Type: N/A";
        avgSigStr = "Avg Signal: N/A";
        samples = "Samples: N/A";
        this.signal = signal;
        this.psc = psc;
        country = "Country: N/A";
        timestamp = "Timestamp: N/A";
        this.recordId = recordId;
    }

    public CardItemData(String cellID, String lac, String mcc, String mnc, String signal,
                        String psc, String timestamp, String recordId) {
        cellId = cellID;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        lat = "Latitude: N/A";
        lon = "Longitude: N/A";
        net = "Network Type: N/A";
        this.signal = signal;
        this.psc = psc;
        avgSigStr = "Avg Signal: N/A";
        samples = "Samples: N/A";
        this.timestamp = timestamp;
        country = "Country: N/A";
        this.recordId = recordId;
    }

    public CardItemData(int type, String cellID, String lac, String mcc, String mnc, String signal,
                        String timestamp, String recordId) {
        cellId = cellID;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        lat = "Latitude: N/A";
        lon = "Longitude: N/A";
        net = "Network Type: N/A";
        this.signal = signal;
        avgSigStr = "Avg Signal: N/A";
        samples = "Samples: N/A";
        this.timestamp = timestamp;
        psc = "PSC: N/A";
        country = "Country: N/A";
        this.recordId = recordId;
    }

    public CardItemData(String cellID, String lac, String net, String lat, String lng,
                        String signal, String recordId) {
        cellId = cellID;
        this.lac = lac;
        this.net = net;
        mcc = "MCC: N/A";
        mnc = "MNC: N/A";
        this.lat = lat;
        lon = lng;
        this.signal = signal;
        avgSigStr = "Avg Signal: N/A";
        samples = "Samples: N/A";
        psc = "PSC: N/A";
        country = "Country: N/A";
        timestamp = "Timestamp: N/A";
        this.recordId = recordId;
    }

    public CardItemData(String country, String mcc, String lat, String lng, String recordId) {
        cellId = "CellID: N/A";
        lac = "LAC: N/A";
        this.country = country;
        this.mcc = mcc;
        mnc = "MNC: N/A";
        net = "Network Type: N/A";
        signal = "Signal: N/A";
        this.lat = lat;
        lon = lng;
        avgSigStr = "Avg Signal: N/A";
        samples = "Samples: N/A";
        psc = "PSC: N/A";
        timestamp = "Timestamp: N/A";
        this.recordId = recordId;
    }


    public String getCellId() {
        return cellId;
    }

    public String getLac() {
        return lac;
    }

    public String getMcc() {
        return mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public String getNet() {
        return net;
    }

    public String getSignal() {
        return signal;
    }

    public String getAvgSigStr() {
        return avgSigStr;
    }

    public String getSamples() {
        return samples;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getCountry() {
        return country;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getPsc() {
        return psc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // NEW (in new DB tables)
    // EventLog

    //public String getAccu() {
    //    return mAccu;
    //}

}
