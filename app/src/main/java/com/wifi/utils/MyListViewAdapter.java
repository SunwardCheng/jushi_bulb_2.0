package com.wifi.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wifi.main.R;

import java.util.List;
import java.util.Map;

/**
 * 	/**
 * ListView的Item显示
 * Created by 17993 on 2018/2/2.
 */

public class MyListViewAdapter  extends BaseAdapter {
    LayoutInflater inflater;
    List<Map<String, Object>> list;

    public MyListViewAdapter(Context context, List<Map<String, Object>> list) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //忽略指定的警告
    @SuppressLint({ "ViewHolder", "InflateParams" })
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //针对每一个数据（即每一个List ID）创建一个ListView实例，
        View view = null;
        view = inflater.inflate(R.layout.wifi_listitem, null);
        Map<String, Object> result = list.get(position);
        TextView wifi_ssid=(TextView) view.findViewById(R.id.ssid);
        ImageView wifi_level=(ImageView) view.findViewById(R.id.wifi_level);
        wifi_ssid.setText(result.get("Service_Name")+"");
        wifi_level.setImageResource(R.mipmap.wifi);
        return view;
    }

}