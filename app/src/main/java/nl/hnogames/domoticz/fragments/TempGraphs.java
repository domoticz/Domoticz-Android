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

package nl.hnogames.domoticz.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.utils.MaterialColorPalette;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.GraphPointInfo;
import nl.hnogames.domoticzapi.Containers.TemperatureInfo;
import nl.hnogames.domoticzapi.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticzapi.Interfaces.TemperatureReceiver;


public class TempGraphs extends Fragment implements DomoticzFragmentListener {

    @SuppressWarnings("unused")
    private static final String TAG = TempGraphs.class.getSimpleName();

    private Context context;
    private ArrayList<TemperatureInfo> mTempInfos;
    private boolean graphTemp = true, graphChill = false, graphHum = false, graphBaro = false, graphDew = false, graphSet = false;
    private Integer[] selectedDevices;
    private List<String> filterLabels;
    private HashMap<String, ArrayList<GraphPointInfo>> mGraphList;
    private LineChart chart;
    private View root;
    private Context mContext;
    private SharedPrefUtil mSharedPrefs;
    private String range = "minutes";

    @Override
    public void onConnectionOk() {}

    @Override
    public void onConnectionFailed() {}

    @Override

    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        onAttachFragment(this);
        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        StaticHelper.getDomoticz(mContext).getTemperatures(new TemperatureReceiver() {
            @Override
            public void onReceiveTemperatures(ArrayList<TemperatureInfo> mTemperatureInfos) {
                mTempInfos = mTemperatureInfos;
                String[] items = new String[mTempInfos.size()];
                for(int i =0; i<mTempInfos.size(); i++)
                    items[i] = mTempInfos.get(i).getName();

                new MaterialDialog.Builder(context)
                        .title(context.getString(R.string.filter_devices))
                        .items(items)
                        .itemsCallbackMultiChoice(selectedDevices, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                selectedDevices = which;
                                mGraphList = new HashMap<>();
                                if (which != null && which.length > 0) {
                                    for (int c : which)
                                        getGraphs(mTempInfos.get(c).getIdx(), mTempInfos.get(c).getName());
                                } else {
                                    Toast.makeText(context, context.getString(R.string.filter_graph_empty), Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }
                        })
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .show();
            }

            @Override
            public void onError(Exception error) {}
        });
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_temp_graphs, null);
        chart = root.findViewById(R.id.chart);

        XAxis xAxis = chart.getXAxis();
        YAxis yAxis = chart.getAxisLeft();

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.graphTextColor, typedValue, true);
        xAxis.setTextColor(typedValue.data);
        yAxis.setTextColor(typedValue.data);
        chart.getLegend().setTextColor(typedValue.data);

        chart.setDrawGridBackground(true);

        chart.getDescription().setEnabled(false);
        xAxis.setDrawGridLines(false); // no grid lines
        chart.getAxisRight().setEnabled(false); // no right axis
        chart.setDragDecelerationFrictionCoef(0.9f);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        if (range.equals("day")) {
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((long) value);
                    return String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MINUTE));
                }
            });
        } else {
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((long) value);

                    //int mYear = calendar.get(Calendar.YEAR);
                    int mMonth = calendar.get(Calendar.MONTH) + 1;
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    int mHours = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinutes = calendar.get(Calendar.MINUTE);

                    String xValue;
                    if (mHours <= 0 && mMinutes <= 0)
                        xValue = String.format(Locale.getDefault(), "%02d", mHours) + ":" + String.format(Locale.getDefault(), "%02d", mMinutes);
                    else
                        xValue = mDay + "/" + mMonth + " " + String.format(Locale.getDefault(), "%02d", mHours) + ":" + String.format(Locale.getDefault(), "%02d", mMinutes);
                    return xValue;
                }
            });
        }

        xAxis.setLabelRotationAngle(90);
        xAxis.setLabelCount(15);
        return root;
    }

    private void getGraphs(final int idx, final String name) {
        new Thread() {
            @Override
            public void run() {
                StaticHelper.getDomoticz(context).getTempGraphData(idx, "2020-07-29T2020-08-05", 1,
                graphTemp, graphChill, graphHum, graphBaro, graphDew, graphSet, new GraphDataReceiver() {
                    @Override
                    public void onReceive(ArrayList<GraphPointInfo> grphPoints) {
                        try {
                            mGraphList.put(name, grphPoints);
                            LineData columnData = generateData(root);
                            if (columnData != null) {
                                chart.setData(columnData);
                                chart.invalidate(); // refresh

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chart.setVisibility(View.VISIBLE);
                                        chart.animateX(1000);

                                        if (getActivity() != null)
                                            getActivity().invalidateOptionsMenu();
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            if (ex.getMessage() != null)
                                Log.e(this.getClass().getSimpleName(), ex.getMessage());
                        }
                    }

                    @Override

                    public void onError(Exception ex) {
                    }
                });
            }
        }.start();
    }

    @SuppressWarnings("SpellCheckingInspection")
    private LineData generateData(View view) {
        try {
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            for (Map.Entry<String, ArrayList<GraphPointInfo>> list : mGraphList.entrySet()) {
                List<LineDataSet> entries = new ArrayList<>();

                List<Entry> valuest = new ArrayList<>();
                List<Entry> valuestMin = new ArrayList<>();
                List<Entry> valuestMax = new ArrayList<>();

                List<Entry> valuesse = new ArrayList<>();
                List<Entry> valueshu = new ArrayList<>();
                List<Entry> valuesba = new ArrayList<>();

                boolean addHumidity = false;
                boolean addBarometer = false;
                boolean addTemperature = false;
                boolean addTemperatureRange = false;
                boolean addSetpoint = false;
                Calendar mydate = Calendar.getInstance();

                for (GraphPointInfo g : list.getValue()) {
                        try {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                mydate.setTime(sdf.parse(g.getDateTime()));
                            } catch (ParseException e) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                mydate.setTime(sdf.parse(g.getDateTime()));
                            }

                            if (!Float.isNaN(g.getTemperature())) {
                                addTemperature = true;
                                valuest.add(new Entry(mydate.getTimeInMillis(), g.getTemperature()));

                                if (g.hasTemperatureRange()) {
                                    addTemperatureRange = true;
                                    valuestMax.add(new Entry(mydate.getTimeInMillis(), g.getTemperatureMax()));
                                    valuestMin.add(new Entry(mydate.getTimeInMillis(), g.getTemperatureMin()));
                                }
                            }

                            if (!Float.isNaN(g.getSetPoint())) {
                                addSetpoint = true;
                                valuesse.add(new Entry(mydate.getTimeInMillis(), g.getSetPoint()));
                            }

                            if (g.getBarometer() != null && g.getBarometer().length() > 0) {
                                addBarometer = true;
                                try {
                                    valuesba.add(new Entry(mydate.getTimeInMillis(), Integer.parseInt(g.getBarometer())));
                                } catch (Exception ex) {
                                    valuesba.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getBarometer())));
                                }
                            }

                            if (g.getHumidity() != null && g.getHumidity().length() > 0) {
                                addHumidity = true;
                                try {
                                    valueshu.add(new Entry(mydate.getTimeInMillis(), Integer.parseInt(g.getHumidity())));
                                } catch (Exception ex) {
                                    valuesba.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getHumidity())));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }

                if (addTemperature) {
                    LineDataSet dataSet = new LineDataSet(valuest, list.getKey() + " " + getString(R.string.temperature)); // add entries to dataset
                    dataSet.setColor(MaterialColorPalette.getRandomColor("600"));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    entries.add(dataSet);

                    if (addSetpoint) {
                        dataSet = new LineDataSet(valuesse, list.getKey() + " " + getString(R.string.set_point)); // add entries to dataset
                        dataSet.setColor(MaterialColorPalette.getRandomColor("600"));
                        dataSet.setDrawCircles(false);
                        dataSet.setMode(LineDataSet.Mode.LINEAR);
                        entries.add(dataSet);
                    }

                    if (addTemperatureRange) {
                        dataSet = new LineDataSet(valuestMax, list.getKey() + " " + "Max"); // add entries to dataset
                        dataSet.setColor(MaterialColorPalette.getRandomColor("600"));
                        dataSet.setDrawCircles(false);
                        dataSet.setMode(LineDataSet.Mode.LINEAR);
                        dataSet.setDrawFilled(true);
                        dataSet.setFillColor(MaterialColorPalette.getRandomColor("300"));
                        entries.add(dataSet);

                        dataSet = new LineDataSet(valuestMin, list.getKey() + " " + "Min"); // add entries to dataset
                        dataSet.setColor(MaterialColorPalette.getRandomColor("600"));
                        dataSet.setDrawCircles(false);
                        dataSet.setMode(LineDataSet.Mode.LINEAR);
                        dataSet.setFillAlpha(255);
                        dataSet.setFillColor(MaterialColorPalette.getRandomColor("300"));
                        dataSet.setDrawFilled(true);
                        entries.add(dataSet);
                    }
                }

                if (addHumidity) {
                    LineDataSet dataSet = new LineDataSet(valueshu, list.getKey() + " " + getString(R.string.humidity)); // add entries to dataset
                    dataSet.setColor(MaterialColorPalette.getRandomColor("600"));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    entries.add(dataSet);
                }

                if (addBarometer) {
                    LineDataSet dataSet = new LineDataSet(valuesba, list.getKey() + " " + getString(R.string.barometer)); // add entries to dataset
                    dataSet.setColor(MaterialColorPalette.getRandomColor("600"));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    entries.add(dataSet);
                }

                dataSets.addAll(entries);
            }

            LineData lineChartData = new LineData(dataSets);
            lineChartData.setHighlightEnabled(true);
            lineChartData.setDrawValues(false);
            return lineChartData;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
}