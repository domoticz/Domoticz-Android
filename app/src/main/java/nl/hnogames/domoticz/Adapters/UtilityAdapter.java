/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.UtilitiesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.UtilityClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class UtilityAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = UtilityAdapter.class.getSimpleName();

    private final UtilityClickListener listener;
    public ArrayList<UtilitiesInfo> filteredData = null;
    private Context context;
    private ArrayList<UtilitiesInfo> data = null;
    private Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();

    private SharedPrefUtil mSharedPrefs;

    public UtilityAdapter(Context context,
                          Domoticz mDomoticz,
                          ArrayList<UtilitiesInfo> data,
                          UtilityClickListener listener) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;

        Collections.sort(data, new Comparator<UtilitiesInfo>() {
            @Override
            public int compare(UtilitiesInfo left, UtilitiesInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });
        this.data = data;
        this.filteredData = data;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int layoutResourceId;

        UtilitiesInfo mUtilitiesInfo = filteredData.get(position);
        final double setPoint = mUtilitiesInfo.getSetPoint();

        //if (convertView == null) {
        holder = new ViewHolder();
        if (Domoticz.UTILITIES_TYPE_THERMOSTAT.equalsIgnoreCase(mUtilitiesInfo.getType())) {
            convertView = CreateThermostatRow(parent, holder, mUtilitiesInfo, setPoint);
        } else {
            if (Domoticz.UTILITIES_SUBTYPE_TEXT.equalsIgnoreCase(mUtilitiesInfo.getSubType())) {
                convertView = CreateTextRow(parent, holder, mUtilitiesInfo);
            } else
                convertView = CreateDefaultRow(parent, holder, mUtilitiesInfo);
        }
        convertView.setTag(holder);

        if (mSharedPrefs.darkThemeEnabled()) {
            (convertView.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));

            if ((convertView.findViewById(R.id.on_button)) != null)
                (convertView.findViewById(R.id.on_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
            if ((convertView.findViewById(R.id.off_button)) != null)
                (convertView.findViewById(R.id.off_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
            if ((convertView.findViewById(R.id.set_button)) != null)
                (convertView.findViewById(R.id.set_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
        }
        return convertView;
    }


    @NonNull
    private View CreateTextRow(ViewGroup parent, ViewHolder holder, UtilitiesInfo mUtilitiesInfo) {
        int layoutResourceId;
        View convertView;
        layoutResourceId = R.layout.utilities_row_text;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.isProtected = mUtilitiesInfo.isProtected();
        holder.name = (TextView) convertView.findViewById(R.id.utilities_name);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);

        holder.buttonLog = (Button) convertView.findViewById(R.id.log_button);

        holder.data = (TextView) convertView.findViewById(R.id.utilities_data);
        holder.hardware = (TextView) convertView.findViewById(R.id.utilities_hardware);

        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.append(": " + mUtilitiesInfo.getData());
        holder.hardware.append(": " + mUtilitiesInfo.getHardwareName());

        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0)
            holder.data.setText(context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage());
        if (mUtilitiesInfo.getCounterToday() != null && mUtilitiesInfo.getCounterToday().length() > 0)
            holder.data.append(" " + context.getString(R.string.today) + ": " + mUtilitiesInfo.getCounterToday());
        if (mUtilitiesInfo.getCounter() != null && mUtilitiesInfo.getCounter().length() > 0 &&
                !mUtilitiesInfo.getCounter().equals(mUtilitiesInfo.getData()))
            holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounter());
        if (mSharedPrefs.darkThemeEnabled()) {
            holder.buttonLog.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
        }

        holder.buttonLog.setId(mUtilitiesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        Picasso.with(context).load(domoticz.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
        return convertView;
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    @NonNull
    private View CreateDefaultRow(ViewGroup parent, ViewHolder holder, UtilitiesInfo mUtilitiesInfo) {
        int layoutResourceId;
        View convertView;
        layoutResourceId = R.layout.utilities_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.isProtected = mUtilitiesInfo.isProtected();
        holder.name = (TextView) convertView.findViewById(R.id.utilities_name);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);

        holder.dayButton = (Button) convertView.findViewById(R.id.day_button);
        holder.monthButton = (Button) convertView.findViewById(R.id.month_button);
        holder.yearButton = (Button) convertView.findViewById(R.id.year_button);
        holder.weekButton = (Button) convertView.findViewById(R.id.week_button);

        if (mSharedPrefs.darkThemeEnabled()) {
            holder.dayButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.monthButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.yearButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.weekButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
        }

        holder.data = (TextView) convertView.findViewById(R.id.utilities_data);
        holder.hardware = (TextView) convertView.findViewById(R.id.utilities_hardware);

        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.append(": " + mUtilitiesInfo.getData());
        holder.hardware.append(": " + mUtilitiesInfo.getHardwareName());

        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0)
            holder.data.setText(context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage());
        if (mUtilitiesInfo.getCounterToday() != null && mUtilitiesInfo.getCounterToday().length() > 0)
            holder.data.append(" " + context.getString(R.string.today) + ": " + mUtilitiesInfo.getCounterToday());
        if (mUtilitiesInfo.getCounter() != null && mUtilitiesInfo.getCounter().length() > 0 &&
                !mUtilitiesInfo.getCounter().equals(mUtilitiesInfo.getData()))
            holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounter());

        holder.dayButton.setId(mUtilitiesInfo.getIdx());
        holder.dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.DAY);
                }
            }
        });
        holder.monthButton.setId(mUtilitiesInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.MONTH);
                }
            }
        });
        holder.weekButton.setId(mUtilitiesInfo.getIdx());
        holder.weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.WEEK);
                }
            }
        });

        holder.yearButton.setId(mUtilitiesInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.YEAR);
                }
            }
        });

        Picasso.with(context).load(domoticz.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
        return convertView;
    }

    @NonNull
    private View CreateThermostatRow(ViewGroup parent, ViewHolder holder, UtilitiesInfo mUtilitiesInfo, final double setPoint) {
        int layoutResourceId;
        View convertView;
        layoutResourceId = R.layout.utilities_row_thermostat;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.name = (TextView) convertView.findViewById(R.id.thermostat_name);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        holder.lastSeen = (TextView) convertView.findViewById(R.id.thermostat_lastSeen);
        holder.setPoint = (TextView) convertView.findViewById(R.id.thermostat_set_point);
        holder.isProtected = mUtilitiesInfo.isProtected();
        holder.on_button = (Button) convertView.findViewById(R.id.on_button);
        if (holder.isProtected)
            holder.on_button.setEnabled(false);

        holder.on_button.setText(context.getString(R.string.set_temperature));
        holder.on_button.setId(mUtilitiesInfo.getIdx());
        holder.on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleThermostatClick(v.getId());
            }
        });
        if (mSharedPrefs.darkThemeEnabled()) {
            holder.on_button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
        }
        holder.name.setText(mUtilitiesInfo.getName());
        holder.lastSeen.setText(mUtilitiesInfo.getLastUpdate());
        holder.setPoint.setText(context.getString(R.string.set_point) + ": " + String.valueOf(setPoint));
        Picasso.with(context).load(domoticz.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
        return convertView;
    }

    public void handleThermostatClick(int idx) {
        listener.onThermostatClick(idx);
    }

    static class ViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        TextView lastSeen;
        TextView setPoint;
        ImageView iconRow;
        Boolean isProtected;

        Button dayButton;
        Button monthButton;
        Button yearButton;
        Button weekButton;
        Button buttonLog;
        Button on_button;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<UtilitiesInfo> list = data;

            int count = list.size();
            final ArrayList<UtilitiesInfo> nlist = new ArrayList<UtilitiesInfo>(count);

            UtilitiesInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableObject);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<UtilitiesInfo>) results.values;
            notifyDataSetChanged();
        }
    }

}