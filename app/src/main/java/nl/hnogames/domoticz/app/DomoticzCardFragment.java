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

package nl.hnogames.domoticz.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.List;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.WifiSSIDListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PhoneConnectionUtil;
import nl.hnogames.domoticz.Utils.ServerUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class DomoticzCardFragment extends Fragment {

    public Domoticz mDomoticz;
    private DomoticzFragmentListener listener;
    private String fragmentName;
    private SharedPrefUtil mSharedPrefs;
    private TextView debugText;
    private boolean debug;
    private ViewGroup root;

    public DomoticzCardFragment() {
    }

    public void refreshFragment() {
    }


    public void setTheme() {
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(getActivity());
        if (mSharedPrefs.darkThemeEnabled()) {
            if (root.findViewById(R.id.my_recycler_view) != null)
                (root.findViewById(R.id.my_recycler_view)).setBackgroundColor(getResources().getColor(R.color.background_dark));
            if ((root.findViewById(R.id.debugLayout)) != null)
                (root.findViewById(R.id.debugLayout)).setBackgroundColor(getResources().getColor(R.color.background_dark));
            if ((root.findViewById(R.id.coordinatorLayout)) != null)
                (root.findViewById(R.id.coordinatorLayout)).setBackgroundColor(getResources().getColor(R.color.background_dark));
            if (root.findViewById(R.id.errorImage) != null)
                ((ImageView) root.findViewById(R.id.errorImage)).setImageDrawable(getResources().getDrawable(R.drawable.sad_smiley_dark));
        }
    }

    public ServerUtil getServerUtil() {
        return ((MainActivity) getActivity()).geServerUtil();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_cameras, null);
        setTheme();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDomoticz = new Domoticz(getActivity(), getServerUtil());
        debug = mDomoticz.isDebugEnabled();

        if (debug) showDebugLayout();

        checkConnection();
    }

    /**
     * Connects to the attached fragment to cast the DomoticzFragmentListener to.
     * Throws ClassCastException if the fragment does not implement the DomoticzFragmentListener
     *
     * @param fragment fragment to cast the DomoticzFragmentListener to
     */
    public void onAttachFragment(Fragment fragment) {

        fragmentName = fragment.toString();

        try {
            listener = (DomoticzFragmentListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    fragment.toString() + " must implement DomoticzFragmentListener");
        }
    }

    /**
     * Checks for a active connection
     */
    public void checkConnection() {

        List<Fragment> fragments = getFragmentManager().getFragments();
        onAttachFragment(fragments.get(0));                           // Get only the last fragment

        PhoneConnectionUtil mPhoneConnectionUtil = new PhoneConnectionUtil(getActivity(), new WifiSSIDListener() {
            @Override
            public void ReceiveSSIDs(CharSequence[] entries) {
            }
        });

        if (mPhoneConnectionUtil.isNetworkAvailable()) {
            addDebugText("Connection OK");
            listener.onConnectionOk();
        } else setErrorMessage(getString(R.string.error_notConnected));
    }

    /**
     * Handles the success messages
     *
     * @param result Result text to handle
     */
    public void successHandling(String result, boolean displayToast) {
        if (result.equalsIgnoreCase(Domoticz.Result.ERROR))
            Toast.makeText(getActivity(), R.string.action_failed, Toast.LENGTH_SHORT).show();
        else if (result.equalsIgnoreCase(Domoticz.Result.OK)) {
            if (displayToast)
                Toast.makeText(getActivity(), R.string.action_success, Toast.LENGTH_SHORT).show();
        } else {
            if (displayToast)
                Toast.makeText(getActivity(), R.string.action_unknown, Toast.LENGTH_SHORT).show();
        }
        if (debug) addDebugText("- Result: " + result);
    }

    /**
     * Handles the error messages
     *
     * @param error Exception
     */
    public void errorHandling(Exception error) {
        hideRecyclerView();
        error.printStackTrace();

        String errorMessage = mDomoticz.getErrorMessage(error);

        if (error instanceof JSONException
                && errorMessage.equalsIgnoreCase("No value for result")) {
            setMessage(getString(R.string.no_data_on_domoticz));
        } else setErrorMessage(errorMessage);
    }

    public ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void setErrorMessage(String message) {

        if (debug) addDebugText(message);
        else {
            Logger(fragmentName, message);
            setErrorLayoutMessage(message);
        }
    }

    public void addDebugText(String text) {
        Logger(fragmentName, text);

        if (debug) {
            if (debugText != null) {
                String temp = debugText.getText().toString();
                if (temp.isEmpty() || temp.equals("")) debugText.setText(text);
                else {
                    temp = temp + "\n";
                    temp = temp + text;
                    debugText.setText(temp);
                }
            } else throw new RuntimeException(
                    "Layout should have a TextView defined with the ID \"debugText\"");
        }
    }

    private void setErrorLayoutMessage(String message) {
        RelativeLayout errorLayout = (RelativeLayout) root.findViewById(R.id.errorLayout);
        if (errorLayout != null) {
            errorLayout.setVisibility(View.VISIBLE);
            TextView errorTextMessage = (TextView) root.findViewById(R.id.errorTextMessage);
            errorTextMessage.setText(message);
        } else throw new RuntimeException(
                "Layout should have a RelativeLayout defined with the ID of errorLayout");
    }

    private void setMessage(String message) {
        RelativeLayout errorLayout = (RelativeLayout) root.findViewById(R.id.errorLayout);
        if (errorLayout != null) {
            errorLayout.setVisibility(View.VISIBLE);

            ImageView errorImage = (ImageView) root.findViewById(R.id.errorImage);
            errorImage.setImageResource(R.drawable.empty);
            errorImage.setAlpha(0.5f);
            errorImage.setVisibility(View.VISIBLE);

            TextView errorTextWrong = (TextView) root.findViewById(R.id.errorTextWrong);
            errorTextWrong.setVisibility(View.GONE);

            TextView errorTextMessage = (TextView) root.findViewById(R.id.errorTextMessage);
            errorTextMessage.setText(message);
        } else throw new RuntimeException(
                "Layout should have a RelativeLayout defined with the ID of errorLayout");
    }

    private void hideRecyclerView() {
        android.support.v7.widget.RecyclerView recyclerView = (android.support.v7.widget.RecyclerView) root.findViewById(R.id.my_recycler_view);
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void showDebugLayout() {
        try {
            if (root != null) {
                LinearLayout debugLayout = (LinearLayout) root.findViewById(R.id.debugLayout);
                if (debugLayout != null) {
                    debugLayout.setVisibility(View.VISIBLE);

                    debugText = (TextView) root.findViewById(R.id.debugText);
                    if (debugText != null) {
                        debugText.setMovementMethod(new ScrollingMovementMethod());
                        debugText.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                mDomoticz.debugTextToClipboard(debugText);
                                return false;
                            }
                        });
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public void Logger(String tag, String text) {
        if (!UsefulBits.isEmpty(tag) && !UsefulBits.isEmpty(text))
            Log.d(tag, text);
    }

    public void Filter(String text) {
    }
}