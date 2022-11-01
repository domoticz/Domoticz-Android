/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package nl.hnogames.domoticz.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.WifiInfo;
import nl.hnogames.domoticz.interfaces.WifiClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class WifiAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = WifiAdapter.class.getSimpleName();
    private final Context context;
    private final WifiClickListener listener;
    private final SharedPrefUtil mSharedPrefs;
    public ArrayList<WifiInfo> data = null;

    public WifiAdapter(Context context,
                       ArrayList<WifiInfo> data,
                       WifiClickListener l) {
        super();

        mSharedPrefs = new SharedPrefUtil(context);
        this.context = context;
        this.data = data;
        this.listener = l;
    }

    @Override
    public int getCount() {
        if (data == null)
            return 0;

        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        int layoutResourceId;

        final WifiInfo mWifiInfo = data.get(position);
        holder = new ViewHolder();

        layoutResourceId = R.layout.bluetooth_row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.enable = convertView.findViewById(R.id.enable);
        holder.name = convertView.findViewById(R.id.name);
        holder.tag_id = convertView.findViewById(R.id.tag_id);
        holder.switch_idx = convertView.findViewById(R.id.switchidx);
        holder.remove = convertView.findViewById(R.id.remove_button);

        holder.name.setText(mWifiInfo.getName());
        holder.tag_id.setText(mWifiInfo.getSSID());
        holder.tag_id.setVisibility(View.GONE);

        if (!UsefulBits.isEmpty(mWifiInfo.getSwitchName())) {
            holder.switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mWifiInfo.getSwitchName());
        } else if (mWifiInfo.getSwitchIdx() > 0) {
            holder.switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mWifiInfo.getSwitchIdx());
        } else {
            holder.switch_idx.setText(context.getString(R.string.connectedSwitch)
                    + ": " + context.getString(R.string.not_available));
        }

        if (!UsefulBits.isEmpty(mWifiInfo.getValue()))
            holder.switch_idx.setText(holder.switch_idx.getText() + " - " + mWifiInfo.getValue());

        holder.remove.setId(position);
        holder.remove.setOnClickListener(v -> handleRemoveButtonClick(data.get(v.getId())));

        holder.enable.setId(position);
        holder.enable.setChecked(mWifiInfo.isEnabled());
        holder.enable.setOnCheckedChangeListener((buttonView, isChecked) -> handleEnableChanged(data.get(buttonView.getId()), isChecked));

        convertView.setTag(holder);
        return convertView;
    }

    private void handleRemoveButtonClick(WifiInfo qr) {
        listener.onRemoveClick(qr);
    }

    private boolean handleEnableChanged(WifiInfo qr, boolean enabled) {
        return listener.onEnableClick(qr, enabled);
    }

    static class ViewHolder {
        TextView name;
        TextView tag_id;
        TextView switch_idx;
        CheckBox enable;
        Button remove;
    }
}