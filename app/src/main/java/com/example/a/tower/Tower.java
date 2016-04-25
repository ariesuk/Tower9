package com.example.a.tower;

import android.app.Application;
import android.os.Environment;

import com.baidu.mapapi.SDKInitializer;

import java.io.File;
import java.io.IOException;

/**
 * Created by a on 2016/3/31.
 */
public class Tower extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        saveLogcatToFile();

        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
    }

    private void saveLogcatToFile() {

        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/Tower" );
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec( "logcat -c");
                // note *:D just save all debug log to log file. see logcat --help for how to filer log
                //process = Runtime.getRuntime().exec( "logcat -f " + logFile + " *:S com.example.a.tower.MainActivity:D");
                process = Runtime.getRuntime().exec( "logcat -f " + logFile + " *:D");
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else if ( isExternalStorageReadable() ) {
            // only readable
        } else {
            // not accessible
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }
}
