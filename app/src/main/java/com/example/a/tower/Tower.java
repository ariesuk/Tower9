package com.example.a.tower;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by a on 2016/3/31.
 */
public class Tower extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
    }
}
