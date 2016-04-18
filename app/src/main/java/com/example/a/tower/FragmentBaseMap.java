package com.example.a.tower;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.VersionInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.inner.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Math;
/**
 * Created by a on 2016/4/1.
 */

public class FragmentBaseMap extends Fragment {
    private static final String LTAG = MainActivity.class.getSimpleName();
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    boolean isFirstLoc = true; // 是否首次定位
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;

    public static final int BASE_STATIONS_REQUEST = 1;
    public StationsAsyncTask stationsAsyncTask;
    private List<RegisteredBaseStation> mAllRegisteredStation;
    private List<Marker> mStationMakerList;

    static double BAIDU_OFFSET_LAT = 0.00374531687912;
    static double BAIDU_OFFSET_LONG = 0.01200;
    // draw fake base station
    private Marker mMarkerA;
    private Marker mMarkerB;
    private Marker mMarkerC;
    private Marker mMarkerD;
    private InfoWindow mInfoWindow;

    // 初始化全局 bitmap 信息，不用时及时 recycle
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_orange_base_station);
    BitmapDescriptor bdB = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_normal_base_station);
    BitmapDescriptor bdC = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_normal_base_station);
    BitmapDescriptor bdD = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_orange_base_station);
    BitmapDescriptor bd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_orange_base_station);
    BitmapDescriptor bdGround = BitmapDescriptorFactory
            .fromResource(R.drawable.ground_overlay);

    BitmapDescriptor bd_YG = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_yg_base_station);
    BitmapDescriptor bd_YL = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_yl_base_station);
    BitmapDescriptor bd_YS = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_ys_base_station);

    BitmapDescriptor bd_LG = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_lg_base_station);
    BitmapDescriptor bd_LC = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_lc_base_station);
    BitmapDescriptor bd_LW = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_lw_base_station);
    BitmapDescriptor bd_LL = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_ll_base_station);
    BitmapDescriptor bd_LS = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_ls_base_station);


    BitmapDescriptor bd_DG = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_dg_base_station);
    BitmapDescriptor bd_DC = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_dc_base_station);
    BitmapDescriptor bd_DW = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_dw_base_station);
    BitmapDescriptor bd_DL = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_dl_base_station);
    BitmapDescriptor bd_DS = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_ds_base_station);

    BitmapDescriptor bd_UNKNOWN = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_unknown_base_station);

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            Log.d(LTAG, "action: " + s);
            TextView text = (TextView) getActivity().findViewById(R.id.map_connect_info);
            text.setTextColor(Color.RED);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                text.setText("key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (s
                    .equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                text.setText("key 验证成功! 功能可以正常使用");
                text.setTextColor(Color.GREEN);
            }
            else if (s
                    .equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                text.setText("网络出错");
            }
        }
    }

    private SDKReceiver mReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
        View v = inflater.inflate(R.layout.fragment_base_map,container,false);
        return v;
    }

    @Override
    public void onStart() {
        Log.d(getClass().getName(), "onStart");
        TextView text = (TextView) getActivity().findViewById(R.id.map_connect_info);
        text.setTextColor(Color.GREEN);
        text.setText("欢迎使用百度地图Android SDK v" + VersionInfo.getApiVersion());

        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        getActivity().registerReceiver(mReceiver, iFilter);

        //SDKInitializer.initialize(getApplicationContext());

        mMapView = (MapView) getActivity().findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        startMyLoc();
        super.onStart();
    }


    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        // 取消监听 SDK 广播
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void  startMyLoc() {
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(getActivity());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(16.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                //initOverlay(ll);
                mBaiduMap.setOnMapStatusChangeListener(statusListener);
                mBaiduMap.setOnMarkerClickListener(markerListener);
            }
        }
        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
    private class StationsAsyncTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... type) {
            switch (type[0]) {
                case BASE_STATIONS_REQUEST:
                    return getRegisteredBaseStations();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (getActivity() == null) {
                return; // fragment detached
            }
            if (result) {
                updateRegisteredBaseStations();
            }
        }
    }

    public boolean getRegisteredBaseStations() {
        DataBaseAdapter mDbHelper = new DataBaseAdapter(getActivity().getBaseContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        mAllRegisteredStation = new ArrayList<>();
        LatLng ll_West_South = mBaiduMap.getProjection().fromScreenLocation(new Point(0,mBaiduMap.getMapStatus().targetScreen.y*2));
        LatLng ll_East_North = mBaiduMap.getProjection().fromScreenLocation(new Point(mBaiduMap.getMapStatus().targetScreen.x * 2, 0));
        //Cursor cursor = mDbHelper.getStationsByGpsScope(ll_West_South, ll_East_North);
        Cursor cursor = mDbHelper.getStationsByGpsScope(TowerConstant.registeredStationTable,new LatLng(ll_West_South.latitude-BAIDU_OFFSET_LAT, ll_West_South.longitude-BAIDU_OFFSET_LONG), new LatLng(ll_East_North.latitude-BAIDU_OFFSET_LAT,ll_East_North.longitude-BAIDU_OFFSET_LAT));
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                i++;
                RegisteredBaseStation station = new RegisteredBaseStation();
                station.STATIONID = cursor.getInt(0);
                station.TID = cursor.getString(1);
                station.SID = cursor.getString(2);
                station.BELONGING = cursor.getString(3);
                station.LINKMAN = cursor.getString(4);
                station.PHONE = cursor.getString(5);
                station.TEL = cursor.getInt(6);
                station.NETNAME = cursor.getString(7);
                station.SERVICEATTRIBUTE = cursor.getString(8);
                station.TECNAME = cursor.getString(9);
                station.NAME = cursor.getString(10);
                station.LOCATION = cursor.getString(11);
                station.LONGITUDE = cursor.getDouble(12);
                station.LATITUDE = cursor.getDouble(13);
                station.HEIGHT = cursor.getString(14);
                station.EQUIPMENT = cursor.getInt(15);
                station.STATE = cursor.getString(16);
                station.STARTDATA = cursor.getString(17);
                station.STARTFRE = cursor.getDouble(18);
                station.ENDFRE = cursor.getDouble(19);

                //Log.d("data..........H", String.valueOf(cursor.getDouble(12)) + String.valueOf(cursor.getDouble(13)));
                mAllRegisteredStation.add(station);
            } while (cursor.moveToNext());
        }
        mDbHelper.close();
        return  mAllRegisteredStation.size()>0;
    }

    public void updateRegisteredBaseStations() {
        if (mAllRegisteredStation==null) return;
        if (mAllRegisteredStation.size() > 390) {
            // if we got too much stations, just use the random numbers of 390
            // do something
            //some wrong whith below code
            /*
            int listSize = mAllRegisteredStation.size();
            Random randomGenerator = new Random();
            while (listSize>390){
                int randomInt = randomGenerator.nextInt(listSize-1);
                Log.d("random number:", String.valueOf(randomInt));
                mAllRegisteredStation.remove(randomInt);
                listSize= mAllRegisteredStation.size();
            }
            */
            // just simple return now, improve later
            return;
        }
        // clear the markers firstly
        if (mStationMakerList!=null) {
            for (Marker marker : mStationMakerList) {
                marker.remove();
            }
        }
        mStationMakerList = new ArrayList<>();
        for(RegisteredBaseStation station : mAllRegisteredStation) {
            LatLng llS = new LatLng(station.LATITUDE+ BAIDU_OFFSET_LAT, station.LONGITUDE + BAIDU_OFFSET_LONG);
            String netName = station.NETNAME;
            String tecName = station.TECNAME;
            MarkerOptions ooS = new MarkerOptions().position(llS).icon(bd_UNKNOWN).zIndex(9).draggable(true);
            if (netName.contains("移动")) {
                if (tecName.contains("LTE")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_YL)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("GSM")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_YG)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("SCDMA")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_YS)
                            .zIndex(9).draggable(true);
                }
            }
            else if (netName.contains("联通")) {
                if (tecName.contains("WCDMA")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_LW)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("GSM")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_LG)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("SCDMA")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_LS)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("LTE")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_LL)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("CDMA")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_LC)
                            .zIndex(9).draggable(true);
                }
            }
            else if (netName.contains("电信")) {
                if (tecName.contains("GSM")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_DG)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("SCDMA")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_DS)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("LTE")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_DL)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("WCDMA")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_DW)
                            .zIndex(9).draggable(true);
                }
                else if (tecName.contains("CDMA")) {
                    ooS = new MarkerOptions().position(llS).icon(bd_DC)
                            .zIndex(9).draggable(true);
                }
            }

            if (true) {
                //掉下动画
                ooS.animateType(MarkerOptions.MarkerAnimateType.none);
            }
            mStationMakerList.add((Marker) (mBaiduMap.addOverlay(ooS)));
        }

    }

    public void initOverlay(LatLng myLL) {
        // add marker overlay
        /*
        LatLng llA = new LatLng(myLL.latitude + 0.003, myLL.longitude + 0.003);
        LatLng llB = new LatLng(myLL.latitude - 0.004, myLL.longitude + 0.004);
        LatLng llC = new LatLng(myLL.latitude + 0.006, myLL.longitude - 0.006);
        LatLng llD = new LatLng(myLL.latitude - 0.005, myLL.longitude - 0.005);
        */

        LatLng llA = new LatLng(30.51335 + 0.00374531687912,114.42093333333 + 0.01200 );
        LatLng llB = new LatLng(30.51351666666 + 0.00374531687912,114.4208833333333 + 0.01200 );
        LatLng llC = new LatLng(30.51351666666 + 0.00374531687912,114.4202833333333  + 0.01200);
        LatLng llD = new LatLng(30.51336666666 + 0.00374531687912,114.4202833333333 + 0.01200 );

        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdB)
                .zIndex(9).draggable(true);
        if (true) {
            //掉下动画
            ooA.animateType(MarkerOptions.MarkerAnimateType.drop);
        }
        mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
        MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdB)
                .zIndex(5);
        if (true) {
            //掉下动画
            ooB.animateType(MarkerOptions.MarkerAnimateType.none);
        }
        mMarkerB = (Marker) (mBaiduMap.addOverlay(ooB));
        MarkerOptions ooC = new MarkerOptions().position(llC).icon(bdB)
                .perspective(false).anchor(0.5f, 0.5f).rotate(30).zIndex(7);
        if (true) {
            //生长动画
            ooC.animateType(MarkerOptions.MarkerAnimateType.grow);
        }
        mMarkerC = (Marker) (mBaiduMap.addOverlay(ooC));
        ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
        giflist.add(bdA);
        giflist.add(bdB);
        giflist.add(bdD);
        MarkerOptions ooD = new MarkerOptions().position(llD).icons(giflist)
                .zIndex(0).period(10);
        if (true) {
            //生长动画
            ooD.animateType(MarkerOptions.MarkerAnimateType.grow);
        }
        mMarkerD = (Marker) (mBaiduMap.addOverlay(ooD));

        // add ground overlay
        LatLng southwest = new LatLng(myLL.latitude - 0.0015, myLL.longitude + 0.0015);
        LatLng northeast = new LatLng(myLL.latitude - 0.0155, myLL.longitude + 0.0155);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
                .include(southwest).build();

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);
    }

    public BaiduMap.OnMapStatusChangeListener statusListener = new BaiduMap.OnMapStatusChangeListener() {
        /**
         * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
         * @param status 地图状态改变开始时的地图状态
         */
        public void onMapStatusChangeStart(MapStatus status){
        }
        /**
         * 地图状态变化中
         * @param status 当前地图状态
         */
        public void onMapStatusChange(MapStatus status){
        }
        /**
         * 地图状态改变结束
         * @param status 地图状态改变结束后的地图状态
         */
        public void onMapStatusChangeFinish(MapStatus status){
            if (stationsAsyncTask != null && stationsAsyncTask.getStatus()!=AsyncTask.Status.FINISHED) {
                stationsAsyncTask.cancel(true);
            }
             stationsAsyncTask = new StationsAsyncTask();
             stationsAsyncTask.execute(BASE_STATIONS_REQUEST);
            }
    };

    public  BaiduMap.OnMarkerClickListener markerListener = new BaiduMap.OnMarkerClickListener() {
        public boolean onMarkerClick(final Marker marker) {
            int markerPos = mStationMakerList.indexOf(marker);
            RegisteredBaseStation station = ((RegisteredBaseStation) (mAllRegisteredStation.get(markerPos)));
            String text = station.SID + "\n" +station.NETNAME + "\n"
                    + "频段：" + station.STARTFRE + "-" + station.ENDFRE + "\n"
                    + "经度：" + station.LONGITUDE + "\n"
                    + "纬度：" + station.LATITUDE + "\n"
                    + "高度：" + station.HEIGHT + "\n"
                    + station.STARTDATA + "\n"
                    + station.LOCATION;
            Button button = new Button(getActivity().getApplicationContext());
            //button.setBackgroundResource(R.drawable.popup);
            //InfoWindow.OnInfoWindowClickListener listener = null;
            button.setText(text);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //marker.setIcon(bd);
                    mBaiduMap.hideInfoWindow();
                }
            });
            LatLng ll = marker.getPosition();
            mInfoWindow = new InfoWindow(button, ll, -47);
            mBaiduMap.showInfoWindow(mInfoWindow);
            return true;
        }
    };
}
