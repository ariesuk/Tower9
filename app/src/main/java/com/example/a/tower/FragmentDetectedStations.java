package com.example.a.tower;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
/**
 * Created by a on 2016/4/19.
 */
public class FragmentDetectedStations extends Fragment {
    private DataBaseAdapter mDb;
    private ListView lv;
    private View emptyView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDb = new DataBaseAdapter(activity.getBaseContext());
        //mDb.createDatabase();
        mDb.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detected_cells, container, false);
        return v;
    }

    @Override
    public void onStart() {

        lv = (ListView) getActivity().findViewById(R.id.list_view);
        emptyView = (TextView) getActivity().findViewById(R.id.db_list_empty);

        new AsyncTask<Void, Void, BaseInflaterAdapter>() {
            @Override
            protected BaseInflaterAdapter doInBackground(Void... params) {
                //# mDb.open();
                Cursor result;
                result = mDb.returnDetectedStations();
                BaseInflaterAdapter adapter = null;
                if (result != null) {
                    adapter = BuildTable(result);
                    result.close();
                }
                return adapter;
            }

            @Override
            protected void onPostExecute(BaseInflaterAdapter adapter) {
                if (getActivity() == null) {
                    return; // fragment detached
                }

                lv.setEmptyView(emptyView);
                if (adapter != null) {
                    lv.setAdapter(adapter);
                    lv.setVisibility(View.VISIBLE);
                } else {
                    lv.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                getActivity().setProgressBarIndeterminateVisibility(false);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        super.onStart();
    }

    private BaseInflaterAdapter BuildTable(Cursor tableData) {
        if (tableData != null && tableData.getCount() > 0) {
            BaseInflaterAdapter<UniqueBtsItemData> adapter
                    = new BaseInflaterAdapter<>(new UniqueBtsCardInflater());
            int count = tableData.getCount();
            tableData.moveToFirst();
            do {
                UniqueBtsItemData data = new UniqueBtsItemData(
                        String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_MCC))),   // MCC
                        String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_MNC))),   // MNC
                        String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LAC))),   // LAC
                        String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_CID))),   // CID
                        Cell.validatePscValue(
                                this.getActivity().getBaseContext(),
                                tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_PSC))),          // PSC
                        //String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_T3212))), // T3212
                        //String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_A5x))),   // A5x
                        //String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_ST_id))), // ST_id
                        tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_TIME_FIRST)),         // time_first
                        tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_TIME_LAST)),          // time_last
                        tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LAT)),                // gps_lat
                        tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LON)),                // gps_lon
                        (tableData.getPosition() + 1) + " / " + count                                               // item:  "n/X"
                );
                adapter.addItem(data, false);
            }while (tableData.moveToNext());
            if (!tableData.isClosed()) {
                tableData.close();
            }
            return adapter;
        }
        else  {
            return null;
        }
    }

}
