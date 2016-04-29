package com.example.a.tower;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import au.com.bytecode.opencsv.CSVWriter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private TowerService mTowerService;
    private boolean mBound;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mContext = getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, TowerService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStart (){
        super.onStart();
        setTitle(R.string.base_map);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, new FragmentBaseMap())
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            mContext.unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_phone_info) {
            // Handle the phone info action
            setTitle(R.string.phone_info);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new FragmentPhoneInfo())
                    .commit();
        } else if (id == R.id.nav_current_network) {
            setTitle(R.string.current_network_info);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new FragmentCurrentNetwork())
                    .commit();
        } else if (id == R.id.nav_base_map) {
            setTitle(R.string.base_map);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new FragmentBaseMap())
                    .commit();

        } else if (id == R.id.nav_detected_cells) {
            setTitle(R.string.detected_cells_info);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new FragmentDetectedStations())
                    .commit();

        } else if (id == R.id.nav_export_db_table) {
            Toast toast=Toast.makeText(getApplicationContext(),"开始导出数据...", Toast.LENGTH_SHORT);
            toast.show();
            exportDBTablesInBackground();

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mTowerService = ((TowerService.TowerBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(getClass().getName(), "Service Disconnected");
            mBound = false;
        }
    };

    public TowerService getTowerService() {
        return mTowerService;
    }

    // export the db tables
    public void exportDBTablesInBackground() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                //# mDb.open();
                if(mTowerService!=null) {
                    return  mTowerService.getSingletonDbAdapater().backupDB();
                }
                else {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
               if (result) {
                   Toast toast=Toast.makeText(getApplicationContext(),"导出数据完成！", Toast.LENGTH_SHORT);
                   toast.show();
               }
               else {
                   Toast toast=Toast.makeText(getApplicationContext(),"导出数据失败！", Toast.LENGTH_SHORT);
                   toast.show();
               }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
