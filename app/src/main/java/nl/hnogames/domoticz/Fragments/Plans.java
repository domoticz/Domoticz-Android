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

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Adapters.PlansAdapter;
import nl.hnogames.domoticz.Containers.PlanInfo;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.PlansReceiver;
import nl.hnogames.domoticz.PlanActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.app.DomoticzCardFragment;

public class Plans extends DomoticzCardFragment implements DomoticzFragmentListener {

    @SuppressWarnings("unused")
    private static final String TAG = Plans.class.getSimpleName();

    private Context mContext;
    private RecyclerView mRecyclerView;
    private SharedPrefUtil mSharedPrefs;
    private PlansAdapter mAdapter;
    private ArrayList<PlanInfo> mPlans;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void refreshFragment() {
        processPlans();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void processPlans() {

        mDomoticz.getPlans(new PlansReceiver() {

            @Override
            public void OnReceivePlans(ArrayList<PlanInfo> plans) {
                successHandling(plans.toString(), false);

                Plans.this.mPlans = plans;

                Collections.sort(plans, new Comparator<PlanInfo>() {
                    @Override
                    public int compare(PlanInfo left, PlanInfo right) {
                        return left.getOrder() - right.getOrder();
                    }
                });

                mAdapter = new PlansAdapter(plans, getActivity());
                mAdapter.setOnItemClickListener(new PlansAdapter.onClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        //Toast.makeText(getActivity(), "Clicked " + mPlans.get(position).getName(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), PlanActivity.class);
                        //noinspection SpellCheckingInspection
                        intent.putExtra("PLANNAME", mPlans.get(position).getName());
                        //noinspection SpellCheckingInspection
                        intent.putExtra("PLANID", mPlans.get(position).getIdx());
                        startActivity(intent);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mSharedPrefs = new SharedPrefUtil(mContext);
        getActionBar().setTitle(R.string.title_plans);
    }

    @Override
    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error);
            }
        }
    }

    public ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onConnectionOk() {
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        if (mSharedPrefs.darkThemeEnabled()) {
            mRecyclerView.setBackgroundColor(getResources().getColor(R.color.background_dark));
        }

        processPlans();
    }
}