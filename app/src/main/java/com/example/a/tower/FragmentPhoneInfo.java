package com.example.a.tower;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.security.acl.LastOwnerException;

/**
 * Created by a on 2016/4/1.
 */
public class FragmentPhoneInfo extends Fragment {
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity().getBaseContext();
        View v = inflater.inflate(R.layout.fragment_phone_info,container,false);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void updateUI() {
        Log.d(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
        TextView content;
        TowerService ts = ((MainActivity) getActivity()).getTowerService();
        ts.getCellTracker().refreshDevice();
        Device mDevice = ts.getCellTracker().getDevice();

        switch (mDevice.getPhoneID()) {

            case TelephonyManager.PHONE_TYPE_NONE:  // Maybe bad!
            case TelephonyManager.PHONE_TYPE_SIP:   // Maybe bad!
            case TelephonyManager.PHONE_TYPE_GSM: {
                content = (TextView)  getView().findViewById(R.id.network_lac);
                content.setText(String.valueOf(ts.getCell().getLac()));//updateText(String.valueOf(mTowerService.getCell().getLAC()), ani);
                //tr = (TableRow) getView().findViewById(R.id.gsm_cellid);
                //tr.setVisibility(View.VISIBLE);
                content = (TextView)  getView().findViewById(R.id.network_cellid);
                content.setText(String.valueOf(ts.getCell().getCid()));//updateText(String.valueOf(mTowerService.getCell().getCID()), ani);
                break;
            }
        }

        String notAvailable = getString(R.string.n_a);

        content = (TextView)  getView().findViewById(R.id.sim_country);
        content.setText(mDevice.getSimCountry());//updateText(mDevice.getSimCountry().orElse(notAvailable), ani);
        content = (TextView)  getView().findViewById(R.id.sim_operator_id);
        content.setText(mDevice.getSimOperator());//updateText(mDevice.getSimOperator().orElse(notAvailable), ani);
        content = (TextView) getView().findViewById(R.id.sim_operator_name);
        content.setText(mDevice.getSimOperatorName());//updateText(mDevice.getSimOperatorName().orElse(notAvailable), ani);
        content = (TextView)  getView().findViewById(R.id.sim_imsi);
        content.setText(mDevice.getSimSubs());//updateText(mDevice.getSimSubs().orElse(notAvailable), ani);
        content = (TextView)  getView().findViewById(R.id.sim_serial);
        content.setText(mDevice.getSimSerial());//updateText(mDevice.getSimSerial().orElse(notAvailable), ani);

        content = (TextView)  getView().findViewById(R.id.device_type);
        content.setText(mDevice.getPhoneType());
        content = (TextView)  getView().findViewById(R.id.device_imei);
        content.setText(mDevice.getIMEI());//updateText(mDevice.getIMEI(), ani);
        content = (TextView)  getView().findViewById(R.id.device_version);
        content.setText(mDevice.getIMEIv());//updateText(mDevice.getIMEIv(), ani);
        content = (TextView)  getView().findViewById(R.id.network_name);
        content.setText(mDevice.getNetworkName());//updateText(mDevice.getNetworkName(), ani);
        content = (TextView)  getView().findViewById(R.id.network_code);
        content.setText(mDevice.getMncMcc());//updateText(mDevice.getMncMcc(), ani);
        content = (TextView)  getView().findViewById(R.id.network_type);
        content.setText(mDevice.getNetworkTypeName());//updateText(mDevice.getNetworkTypeName(), ani);

        content = (TextView)  getView().findViewById(R.id.data_activity);
        content.setText(mDevice.getDataActivity());//updateText(mDevice.getDataActivity(), ani);
        content = (TextView)  getView().findViewById(R.id.data_status);
        content.setText(mDevice.getDataState());//updateText(mDevice.getDataState(), ani);
        content = (TextView)  getView().findViewById(R.id.network_roaming);
        content.setText(mDevice.isRoaming());//updateText(mDevice.isRoaming(), ani);
    }

}
