package com.example.a.tower;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by a on 2016/4/9.
 */
public interface IAdapterViewInflater<T> {
    View inflate(BaseInflaterAdapter<T> adapter, int pos, View convertView, ViewGroup parent);
}
