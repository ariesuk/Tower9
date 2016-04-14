package com.example.a.tower;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by a on 2016/4/1.
 */
public class FragmentCurrentNetwork extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    public static final int STOCK_REQUEST = 1;
    private RelativeLayout swipeRefreshLayout;
    private Context mContext;
    private boolean mBound;
    private TowerService mTowerService;
    private List<Cell> allCells;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_network,container,false);

        mContext = getActivity().getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, TowerService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        swipeRefreshLayout = (RelativeLayout) getActivity().findViewById(R.id.currentNetworkRefreshLayout);
        //swipeRefreshLayout.setOnRefreshListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(mContext, TowerService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
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

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mTowerService = ((TowerService.TowerBinder) service).getService();
            mBound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    public void onRefresh() {
        updateUI();
        //swipeRefreshLayout.setRefreshing(false);
    }

    private void updateUI() {
        if (mBound) {
            new CellAsyncTask().execute(STOCK_REQUEST);
        }
    }


    private class CellAsyncTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... type) {
            switch (type[0]) {
                case STOCK_REQUEST:
                    return getStockNeighbouringCells();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                updateStockNeighbouringCells();
            }
        }
    }

    boolean getStockNeighbouringCells() {
        if (mBound) {
            allCells = mTowerService.getCellTracker().getAllCells();
            return allCells.size() > 0;
        }
        return false;
    }

    void updateStockNeighbouringCells() {
        ListView lv = (ListView)  getView().findViewById(R.id.list_view);
        //@InjectView(R.id.neighbouring_cells)
        TextView mNeighbouringCells = (TextView)  getView().findViewById(R.id.neighbouring_cells);

        //@InjectView(R.id.neighbouring_number)
        TextView mNeighbouringTotal = (TextView)  getView().findViewById(R.id.neighbouring_number);

        //@InjectView(R.id.neighbouring_total)
        TableRow mNeighbouringTotalView = (TableRow)  getView().findViewById(R.id.neighbouring_total);

        if (allCells.size() != 0) {
            BaseInflaterAdapter<CardItemData> adapter
                    = new BaseInflaterAdapter<>(new CellCardInflater());
            int i = 1;
            int total = allCells.size();
            for (Cell cell : allCells) {
                CardItemData data = new CardItemData(cell, i++ + " / " + total);
                adapter.addItem(data, false);
            }
            lv.setAdapter(adapter);
            mNeighbouringCells.setVisibility(View.GONE);
            mNeighbouringTotalView.setVisibility(View.VISIBLE);
        }
    }

}
