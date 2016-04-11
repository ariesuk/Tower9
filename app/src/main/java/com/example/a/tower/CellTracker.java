package com.example.a.tower;

import android.content.Context;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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

    public List<Cell> getAllCells() {
        int lCurrentApiVersion = android.os.Build.VERSION.SDK_INT;
        List<Cell> allCells = new ArrayList<>();
        try {
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            if (cellInfoList != null) {
                for (final CellInfo info : cellInfoList) {
                    Cell cell = new Cell();
                    //Network Type
                    //cell.setNetType(tm.getNetworkType());
                    if (info instanceof CellInfoGsm) {
                        cell.setNetType(TelephonyManager.NETWORK_TYPE_GPRS);
                        final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        final CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                        // Signal Strength
                        cell.setDbm(gsm.getDbm()); // [dBm]
                        // Cell Identity
                        cell.setCid(identityGsm.getCid());
                        cell.setMcc(identityGsm.getMcc());
                        cell.setMnc(identityGsm.getMnc());
                        cell.setLac(identityGsm.getLac());

                    } else if (info instanceof CellInfoCdma) {
                        cell.setNetType(TelephonyManager.NETWORK_TYPE_CDMA);
                        final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                        final CellIdentityCdma identityCdma = ((CellInfoCdma) info).getCellIdentity();
                        // Signal Strength
                        cell.setDbm(cdma.getDbm());
                        // Cell Identity
                        cell.setCid(identityCdma.getBasestationId());
                        cell.setMnc(identityCdma.getSystemId());
                        cell.setLac(identityCdma.getNetworkId());
                        cell.setSid(identityCdma.getSystemId());

                    } else if (info instanceof CellInfoLte) {
                        cell.setNetType(TelephonyManager.NETWORK_TYPE_LTE);
                        final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                        final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                        // Signal Strength
                        cell.setDbm(lte.getDbm());
                        cell.setTimingAdvance(lte.getTimingAdvance());
                        // Cell Identity
                        cell.setMcc(identityLte.getMcc());
                        cell.setMnc(identityLte.getMnc());
                        cell.setCid(identityLte.getCi());
                        cell.setLac(identityLte.getTac());
                    } else if  (lCurrentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2 && info instanceof CellInfoWcdma) {
                        // wcdma is NETWORK_TYPE_CDMA ?
                        cell.setNetType(TelephonyManager.NETWORK_TYPE_CDMA);
                        final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                        final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                        // Signal Strength
                        cell.setDbm(wcdma.getDbm());
                        // Cell Identity
                        cell.setLac(identityWcdma.getLac());
                        cell.setMcc(identityWcdma.getMcc());
                        cell.setMnc(identityWcdma.getMnc());
                        cell.setCid(identityWcdma.getCid());
                        cell.setPsc(identityWcdma.getPsc());

                    } else {
                        cell.setNetType(TelephonyManager.NETWORK_TYPE_UNKNOWN);
                        Log.d("", "Unknown type of cell signal!"
                                + "\n ClassName: " + info.getClass().getSimpleName()
                                + "\n ToString: " + info.toString());
                    }
                    if (cell.isValid()) {
                        allCells.add(cell);
                    }
                    else {
                        // just for test
                        allCells.add(cell);
                    }
                }
            }
        } catch (NullPointerException npe) {
            Log.d("", "loadCellInfo: Unable to obtain cell signal information: ", npe);
        }
        return allCells;
    }
}
