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

package nl.hnogames.domoticz.Utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.hnogames.domoticz.Containers.Language;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Containers.NFCInfo;
import nl.hnogames.domoticz.Containers.ServerUpdateInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.LanguageReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Service.GeofenceTransitionsIntentService;

@SuppressWarnings("SpellCheckingInspection")
public class SharedPrefUtil {

    private static final String PREF_MULTI_SERVER = "enableMultiServers";
    private static final String PREF_CUSTOM_WEAR = "enableWearItems";
    private static final String PREF_ENABLE_NFC = "enableNFC";
    private static final String PREF_CUSTOM_WEAR_ITEMS = "wearItems";
    private static final String PREF_ALWAYS_ON = "alwayson";
    private static final String PREF_NOTIFICATION_VIBRATE = "notification_vibrate";
    private static final String PREF_NOTIFICATION_SOUND = "notification_sound";
    private static final String PREF_DISPLAY_LANGUAGE = "displayLanguage";
    private static final String PREF_SAVED_LANGUAGE = "savedLanguage";
    private static final String PREF_SAVED_LANGUAGE_DATE = "savedLanguageDate";
    private static final String PREF_UPDATE_SERVER_AVAILABLE = "updateserveravailable";
    private static final String PREF_UPDATE_SERVER_SHOWN = "updateservershown";
    private static final String PREF_EXTRA_DATA = "extradata";
    private static final String PREF_STARTUP_SCREEN = "startup_screen";
    private static final String PREF_TASK_SCHEDULED = "task_scheduled";
    private static final String PREF_NAVIGATION_ITEMS = "enable_menu_items";
    private static final String PREF_NFC_TAGS = "nfc_tags";
    private static final String PREF_GEOFENCE_LOCATIONS = "geofence_locations";
    private static final String PREF_GEOFENCE_ENABLED = "geofence_enabled";
    private static final String PREF_GEOFENCE_STARTED = "geofence_started";
    private static final String PREF_ADVANCED_SETTINGS_ENABLED = "advanced_settings_enabled";
    private static final String PREF_DEBUGGING = "debugging";
    private static final int INVALID_IDX = 999999;
    private static final String PREF_SAVED_LANGUAGE_STRING = "savedLanguageString";
    private static final String PREF_FIRST_START = "isFirstStart";
    private static final String PREF_WELCOME_SUCCESS = "welcomeSuccess";
    private static final String PREF_ENABLE_NOTIFICATIONS = "enableNotifications";
    private static final String PREF_OVERWRITE_NOTIFICATIONS = "overwriteNotifications";
    private static final String PREF_SUPPRESS_NOTIFICATIONS = "suppressNotifications";
    private static final String PREF_RECEIVED_NOTIFICATIONS = "receivedNotifications";
    private static final String PREF_CHECK_UPDATES = "checkForSystemUpdates";
    private final String TAG = "Shared Pref util";
    private final String PREF_SORT_LIKESERVER = "sort_dashboardLikeServer";
    private final String PREF_DARK_THEME = "darkTheme";
    private final String PREF_SWITCH_BUTTONS = "switchButtons";

    private Context mContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private GoogleApiClient mApiClient = null;

    @SuppressLint("CommitPrefEdits")
    public SharedPrefUtil(Context mContext) {
        this.mContext = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
    }

    public boolean darkThemeEnabled() {
        return prefs.getBoolean(PREF_DARK_THEME, false);
    }

    public boolean showSwitchesAsButtons() {
        return prefs.getBoolean(PREF_SWITCH_BUTTONS, false);
    }

    public boolean checkForUpdatesEnabled() {
        return prefs.getBoolean(PREF_CHECK_UPDATES, false);
    }

    public boolean isMultiServerEnabled() {
        return prefs.getBoolean(PREF_MULTI_SERVER, false);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(PREF_ENABLE_NOTIFICATIONS, true);
    }

    public boolean OverWriteNotifications() {
        return prefs.getBoolean(PREF_OVERWRITE_NOTIFICATIONS, false);
    }

    public boolean isDashboardSortedLikeServer() {
        return prefs.getBoolean(PREF_SORT_LIKESERVER, true);
    }

    public boolean getAlwaysOn() {
        return prefs.getBoolean(PREF_ALWAYS_ON, false);
    }

    public void completeCard(String cardTag) {
        editor.putBoolean("CARD" + cardTag, true).apply();
    }

    public boolean isCardCompleted(String cardTag) {
        return prefs.getBoolean("CARD" + cardTag, false);
    }

    public void savePreviousColor(int idx, int color, int position) {
        editor.putInt("COLOR" + idx, color).apply();
        editor.putInt("COLORPOSITION" + idx, position).apply();
        editor.commit();
    }

    public int getPreviousColor(int idx) {
        return prefs.getInt("COLOR" + idx, 0);
    }

    public int getPreviousColorPosition(int idx) {
        return prefs.getInt("COLORPOSITION" + idx, 0);
    }

    public void setWidgetIDX(int widgetID, int idx, boolean isScene, String password) {
        editor.putInt("WIDGET" + widgetID, idx).apply();
        editor.putBoolean("WIDGETSCENE" + widgetID, isScene).apply();
        editor.putString("WIDGETPASSWORD" + widgetID, password).apply();
        editor.commit();
    }

    public int getWidgetIDX(int widgetID) {
        return prefs.getInt("WIDGET" + widgetID, INVALID_IDX);
    }

    public String getWidgetPassword(int widgetID) {
        return prefs.getString("WIDGETPASSWORD" + widgetID, null);
    }

    public boolean getWidgetisScene(int widgetID) {
        return prefs.getBoolean("WIDGETSCENE" + widgetID, false);
    }

    private void setWidgetIDforIDX(int widgetID, int idx, boolean isScene) {
        if (!isScene)
            editor.putInt("WIDGETIDX" + idx, widgetID).apply();
        else
            editor.putInt("WIDGETIDXSCENE" + idx, widgetID).apply();
    }

    private int getWidgetIDforIDX(int idx, boolean isScene) {
        if (!isScene)
            return prefs.getInt("WIDGETIDX" + idx, INVALID_IDX);
        else
            return prefs.getInt("WIDGETIDXSCENE" + idx, INVALID_IDX);
    }

    public boolean isFirstStart() {
        return prefs.getBoolean(PREF_FIRST_START, true);
    }

    public void setFirstStart(boolean firstStart) {
        editor.putBoolean(PREF_FIRST_START, firstStart).apply();
    }

    public boolean isWelcomeWizardSuccess() {
        return prefs.getBoolean(PREF_WELCOME_SUCCESS, false);
    }

    public void setWelcomeWizardSuccess(boolean success) {
        editor.putBoolean(PREF_WELCOME_SUCCESS, success).apply();
    }

    /**
     * Get's the users preference to vibrate on notifications
     *
     * @return true to vibrate
     */
    public boolean getNotificationVibrate() {
        return prefs.getBoolean(PREF_NOTIFICATION_VIBRATE, true);
    }

    /**
     * Get's the URL for the notification sound
     *
     * @return Notification sound URL
     */
    public String getNotificationSound() {
        return prefs.getString(PREF_NOTIFICATION_SOUND, null);
    }

    /**
     * Get's a list of suppressed notifications
     *
     * @return list of suppressed notifications
     */
    public List<String> getSuppressedNotifications() {
        if (!prefs.contains(PREF_SUPPRESS_NOTIFICATIONS)) return null;

        Set<String> notifications = prefs.getStringSet(PREF_SUPPRESS_NOTIFICATIONS, null);
        if (notifications != null) {
            List<String> notificationsValues = new ArrayList<>();

            for (String s : notifications) {
                notificationsValues.add(s);
            }
            return notificationsValues;
        } else return null;
    }

    /**
     * Get's a list of received notifications
     *
     * @return List of received notifications
     */
    public List<String> getReceivedNotifications() {
        if (!prefs.contains(PREF_RECEIVED_NOTIFICATIONS)) return null;

        Set<String> notifications = prefs.getStringSet(PREF_RECEIVED_NOTIFICATIONS, null);
        if (notifications != null) {
            List<String> notificationsValues = new ArrayList<>();

            for (String s : notifications) {
                notificationsValues.add(s);
            }
            java.util.Collections.sort(notificationsValues);
            return notificationsValues;
        } else return null;
    }

    /**
     * Adds the notification to the list of received notifications
     *
     * @param notification Notification string to add
     */
    public void addReceivedNotification(String notification) {
        if (UsefulBits.isEmpty(notification))
            return;
        Set<String> notifications;
        if (!prefs.contains(PREF_RECEIVED_NOTIFICATIONS)) {
            notifications = new HashSet<>();
            notifications.add(notification);
            editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS, notifications).apply();
        } else {
            notifications = prefs.getStringSet(PREF_RECEIVED_NOTIFICATIONS, null);
            if (notifications == null)
                notifications = new HashSet<>();
            if (!notifications.contains(notification)) {
                notifications.add(notification);
                editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS, notifications).apply();
            }
        }
    }

    public void removeWizard() {
        // 1 if start up screen is 0 (wizard) change to dashboard
        if (getStartupScreenIndex() == 0) setStartupScreenIndex(1);

        //2 remove wizard from navigation
        String removeWizard = "";
        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections != null) {
            for (String s : selections) {
                if (s.equals(allNames[0])) {
                    removeWizard = allNames[0];
                    break;
                }
            }
            if (removeWizard.length() > 0) {
                selections.remove(removeWizard);
                editor.putStringSet(PREF_NAVIGATION_ITEMS, selections).apply();
                editor.commit();
            }
        }
    }

    public int getStartupScreenIndex() {
        String startupScreenSelectedValue = prefs.getString(PREF_STARTUP_SCREEN, null);
        if (startupScreenSelectedValue == null) return 0;
        else {
            String[] startupScreenValues =
                    mContext.getResources().getStringArray(R.array.drawer_actions);
            int i = 0;

            for (String screen : startupScreenValues) {
                if (screen.equalsIgnoreCase(startupScreenSelectedValue)) {
                    return i;
                }
                i++;
            }

            //fix, could not find startup screen
            setStartupScreenIndex(0);
            return 0;
        }
    }

    public void setStartupScreenIndex(int position) {
        String[] startupScreenValues =
                mContext.getResources().getStringArray(R.array.drawer_actions);
        String startupScreenValue;

        try {
            startupScreenValue = startupScreenValues[position];
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            startupScreenValue = startupScreenValues[0];
        }

        editor.putString(PREF_STARTUP_SCREEN, startupScreenValue).apply();
    }

    public String[] getWearSwitches() {
        if (!prefs.contains(PREF_CUSTOM_WEAR_ITEMS)) return null;

        Set<String> selections = prefs.getStringSet(PREF_CUSTOM_WEAR_ITEMS, null);

        if (selections != null) {
            String[] selectionValues = new String[selections.size()];

            int i = 0;
            for (String s : selections) {
                selectionValues[i] = s;
                i++;
            }
            return selectionValues;
        } else return null;
    }

    public String[] getNavigationFragments() {
        if (!prefs.contains(PREF_NAVIGATION_ITEMS))
            setNavigationDefaults();

        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allValues = mContext.getResources().getStringArray(R.array.drawer_fragments);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections == null)
            return allValues;
        else {
            String[] selectionValues = new String[selections.size()];
            int i = 0;
            int index = 0;

            for (String v : allNames) {
                for (String s : selections) {
                    if (s.equals(v)) {
                        selectionValues[i] = allValues[index];
                        i++;
                    }
                }
                index++;
            }

            return selectionValues;
        }
    }

    public String[] getNavigationActions() {
        if (!prefs.contains(PREF_NAVIGATION_ITEMS))
            setNavigationDefaults();

        try {
            Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
            String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

            if (selections == null) //default
                return allNames;
            else {
                int i = 0;
                String[] selectionValues = new String[selections.size()];
                for (String v : allNames) {
                    for (String s : selections) {
                        if (s.equals(v)) {
                            selectionValues[i] = v;
                            i++;
                        }
                    }
                }

                if (i < selections.size()) {
                    setNavigationDefaults();
                    return getNavigationActions();
                } else
                    return selectionValues;
            }
        } catch (Exception ex) {
            if (!UsefulBits.isEmpty(ex.getMessage()))
                Log.e(TAG, ex.getMessage());
            setNavigationDefaults();//try to correct the issue
        }

        return null; //failed, can't show the menu (can be a translation issue if this happens!!)
    }

    public void setNavigationDefaults() {
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);
        Set<String> selections = new HashSet<>(Arrays.asList(allNames));
        editor.putStringSet(PREF_NAVIGATION_ITEMS, selections).apply();
    }

    public int[] getNavigationIcons() {
        if (!prefs.contains(PREF_NAVIGATION_ITEMS)) setNavigationDefaults();

        TypedArray icons = mContext.getResources().obtainTypedArray(R.array.drawer_icons);
        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections != null) {

            int[] selectedICONS = new int[selections.size()];
            int iconIndex = 0;
            int index = 0;
            for (String v : allNames) {
                for (String s : selections) {
                    if (s.equals(v)) {
                        selectedICONS[iconIndex] = icons.getResourceId(index, 0);
                        iconIndex++;
                    }
                }
                index++;
            }
            icons.recycle();
            return selectedICONS;
        } else {
            icons.recycle();
            return null;
        }
    }

    public boolean isDebugEnabled() {
        return prefs.getBoolean(PREF_DEBUGGING, false);
    }

    public boolean isAdvancedSettingsEnabled() {
        return prefs.getBoolean(PREF_ADVANCED_SETTINGS_ENABLED, false);
    }

    public void setAdvancedSettingsEnabled(boolean enabled) {
        editor.putBoolean(PREF_ADVANCED_SETTINGS_ENABLED, enabled).apply();
    }

    public boolean showExtraData() {
        return prefs.getBoolean(PREF_EXTRA_DATA, true);
    }

    public boolean showCustomWear() {
        return prefs.getBoolean(PREF_CUSTOM_WEAR, false);
    }

    public boolean isNFCEnabled() {
        return prefs.getBoolean(PREF_ENABLE_NFC, false);
    }

    public boolean isServerUpdateAvailable() {
        return prefs.getBoolean(PREF_UPDATE_SERVER_AVAILABLE, false);
    }

    public String getLastUpdateShown() {
        return prefs.getString(PREF_UPDATE_SERVER_SHOWN, "");
    }

    public void setLastUpdateShown(String revisionNb) {
        editor.putString(PREF_UPDATE_SERVER_SHOWN, revisionNb);
        editor.commit();
    }

    public boolean isGeofenceEnabled() {
        return prefs.getBoolean(PREF_GEOFENCE_ENABLED, false);
    }

    public void setGeofenceEnabled(boolean enabled) {
        editor.putBoolean(PREF_GEOFENCE_ENABLED, enabled).apply();
    }

    public void saveNFCList(List<NFCInfo> list) {
        Gson gson = new Gson();
        editor.putString(PREF_NFC_TAGS, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<NFCInfo> getNFCList() {
        ArrayList<NFCInfo> oReturnValue = new ArrayList<>();
        List<NFCInfo> nfcs;
        if (prefs.contains(PREF_NFC_TAGS)) {
            String jsonNFCs = prefs.getString(PREF_NFC_TAGS, null);
            Gson gson = new Gson();
            NFCInfo[] item = gson.fromJson(jsonNFCs,
                    NFCInfo[].class);
            nfcs = Arrays.asList(item);
            for (NFCInfo n : nfcs) {
                oReturnValue.add(n);
            }
        } else
            return null;

        return oReturnValue;
    }

    public void saveLocations(List<LocationInfo> locations) {
        Gson gson = new Gson();
        String jsonLocations = gson.toJson(locations);
        editor.putString(PREF_GEOFENCE_LOCATIONS, jsonLocations);
        editor.commit();
    }

    public ArrayList<LocationInfo> getLocations() {
        List<LocationInfo> returnValue = new ArrayList<>();
        List<LocationInfo> locations;
        boolean incorrectDetected = false;

        if (prefs.contains(PREF_GEOFENCE_LOCATIONS)) {
            String jsonLocations = prefs.getString(PREF_GEOFENCE_LOCATIONS, null);
            Gson gson = new Gson();
            LocationInfo[] locationItem = gson.fromJson(jsonLocations,
                    LocationInfo[].class);
            locations = Arrays.asList(locationItem);

            for (LocationInfo l : locations) {
                if (l.toGeofence() != null) {
                    returnValue.add(l);
                } else {
                    incorrectDetected = true;
                }
            }
            if (incorrectDetected) {
                saveLocations(returnValue);
                Toast.makeText(mContext,
                        R.string.geofence_error_recreateLocations,
                        Toast.LENGTH_LONG).show();
            }
        } else
            return null;

        return (ArrayList<LocationInfo>) returnValue;
    }

    public LocationInfo getLocation(int id) {
        List<LocationInfo> locations = getLocations();
        for (LocationInfo l : locations) {
            if (l.getID() == id)
                return l;
        }

        return null;
    }

    public void addLocation(LocationInfo location) {
        List<LocationInfo> locations = getLocations();
        if (locations == null)
            locations = new ArrayList<>();
        locations.add(location);
        saveLocations(locations);
    }

    public void updateLocation(LocationInfo location) {
        List<LocationInfo> locations = getLocations();
        if (locations == null)
            locations = new ArrayList<>();

        int i = 0;
        for (LocationInfo l : locations) {
            if (l.getID() == location.getID()) {
                locations.set(i, location);
            }
            i++;
        }
        saveLocations(locations);
    }

    public void removeLocation(LocationInfo location) {
        ArrayList<LocationInfo> locations = getLocations();
        ArrayList<LocationInfo> removeLocations = new ArrayList<>();
        if (locations != null) {
            for (LocationInfo l : locations) {
                if (l.getID() == location.getID())
                    removeLocations.add(l);
            }
            for (LocationInfo l : removeLocations) {
                locations.remove(l);
            }

            saveLocations(locations);
        }
    }

    public boolean saveSharedPreferencesToFile(File dst) {
        boolean isServerUpdateAvailableValue = false;

        ServerUpdateInfo mServerUpdateInfo = new ServerUtil(mContext).getActiveServer().getServerUpdateInfo();

        // Before saving to file set server update available preference to false
        if (isServerUpdateAvailable()) {
            isServerUpdateAvailableValue = true;
            mServerUpdateInfo.setUpdateAvailable(false);
        }

        boolean result = false;

        if (dst.exists()) result = dst.delete();

        if (result) {
            ObjectOutputStream output = null;

            //noinspection TryWithIdenticalCatches
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                output.writeObject(this.prefs.getAll());
                result = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        // Write original settings to preferences
        if (isServerUpdateAvailableValue) mServerUpdateInfo.setUpdateAvailable(true);
        return result;
    }

    @SuppressWarnings({"UnnecessaryUnboxing", "unchecked"})
    public boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        //noinspection TryWithIdenticalCatches
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            editor.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    editor.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    editor.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    editor.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    editor.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    editor.putString(key, ((String) v));
                else if (v instanceof Set)
                    editor.putStringSet(key, ((Set<String>) v));
                else
                    Log.v(TAG, "Could not load pref: " + key + " | " + v.getClass());
            }
            editor.commit();
            res = true;

            if (isGeofenceEnabled()) enableGeoFenceService();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get the user prefered display language
     *
     * @return Language string
     */
    public String getDisplayLanguage() {
        return prefs.getString(PREF_DISPLAY_LANGUAGE, "");
    }

    /**
     * Get's the date (in milliseconds) when the language files where saved
     *
     * @return time in milliseconds
     */
    public long getSavedLanguageDate() {
        return prefs.getLong(PREF_SAVED_LANGUAGE_DATE, 0);
    }

    /**
     * Set's the date (in milliseconds) when the language files are saved
     *
     * @param timeInMillis time in milliseconds
     */
    public void setSavedLanguageDate(long timeInMillis) {
        editor.putLong(PREF_SAVED_LANGUAGE_DATE, timeInMillis).apply();
    }

    /**
     * Save language to shared preferences
     *
     * @param language The translated strings to save to shared preferences
     */
    public void saveLanguage(Language language) {
        if (language != null) {
            Gson gson = new Gson();
            try {
                String jsonLocations = gson.toJson(language);
                editor.putString(PREF_SAVED_LANGUAGE, jsonLocations).apply();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    /**
     * Get the saved language from shared preferences
     *
     * @return Language with tranlated strings
     */
    public Language getSavedLanguage() {
        Language returnValue;

        if (prefs.contains(PREF_SAVED_LANGUAGE)) {
            String languageStr = prefs.getString(PREF_SAVED_LANGUAGE, null);
            if (languageStr != null) {
                Gson gson = new Gson();
                try {
                    returnValue = gson.fromJson(languageStr, Language.class);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            } else return null;
        } else
            return null;

        return returnValue;
    }

    /**
     * Get's the translated strings from the server and saves them to shared preferences
     *
     * @param langToDownload Language to get from the server
     * @param server         ServerUtil
     */
    public boolean getLanguageStringsFromServer(final String langToDownload, ServerUtil server) {

        final boolean[] result = new boolean[1];

        if (!UsefulBits.isEmpty(langToDownload)) {
            new Domoticz(mContext, server).getLanguageStringsFromServer(langToDownload, new LanguageReceiver() {
                @Override
                public void onReceiveLanguage(Language language) {
                    Log.d(TAG, "Language " + langToDownload + " downloaded from server");
                    Calendar now = Calendar.getInstance();
                    saveLanguage(language);
                    // Write to shared preferences so we can use it to check later
                    setDownloadedLanguage(langToDownload);
                    setSavedLanguageDate(now.getTimeInMillis());
                    result[0] = true;
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Unable to get the language from the server: " + langToDownload);
                    error.printStackTrace();
                    result[0] = false;
                }
            });
        } else {
            Log.d(TAG, "Aborting: Language to download not specified");
            result[0] = false;
        }
        return result[0];
    }

    public String getDownloadedLanguage() {
        return prefs.getString(PREF_SAVED_LANGUAGE_STRING, "");
    }

    public void setDownloadedLanguage(String language) {
        editor.putString(PREF_SAVED_LANGUAGE_STRING, language).apply();
    }

    public boolean getTaskIsScheduled() {
        return prefs.getBoolean(PREF_TASK_SCHEDULED, false);
    }

    public void setTaskIsScheduled(boolean isScheduled) {
        editor.putBoolean(PREF_TASK_SCHEDULED, isScheduled).apply();
    }

    public boolean isGeofencingStarted() {
        return prefs.getBoolean(PREF_GEOFENCE_STARTED, false);
    }

    public void setGeofencingStarted(boolean started) {
        editor.putBoolean(PREF_GEOFENCE_STARTED, started).apply();
    }

    public List<Geofence> getEnabledGeofences() {
        final List<Geofence> mGeofenceList = new ArrayList<>();
        final ArrayList<LocationInfo> locations = getLocations();

        if (locations != null) {
            for (LocationInfo locationInfo : locations)
                if (locationInfo.getEnabled())
                    mGeofenceList.add(locationInfo.toGeofence());
            return mGeofenceList;
        } else return null;
    }

    public void enableGeoFenceService() {
        if (isGeofenceEnabled()) {
            //only continue when we have the correct permissions!
            if (PermissionsUtil.canAccessLocation(mContext)) {
                final List<Geofence> mGeofenceList = getEnabledGeofences();
                if (mGeofenceList != null && mGeofenceList.size() > 0) {
                    mApiClient = new GoogleApiClient.Builder(mContext)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    PendingIntent mGeofenceRequestIntent =
                                            getGeofenceTransitionPendingIntent();

                                    // First remove all GeoFences
                                    try {
                                        LocationServices.GeofencingApi.removeGeofences(mApiClient,
                                                mGeofenceRequestIntent);
                                    } catch (Exception ignored) {
                                    }

                                    //noinspection ResourceType
                                    LocationServices
                                            .GeofencingApi
                                            .addGeofences(mApiClient,
                                                    getGeofencingRequest(mGeofenceList),
                                                    mGeofenceRequestIntent);
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                }
                            })
                            .build();
                    mApiClient.connect();
                } else {
                    // No enabled geofences, disabling
                    setGeofenceEnabled(false);
                }
            }
        }
    }

    public void stopGeofenceService() {
        if (mApiClient != null) {
            // If mApiClient is null enableGeofenceService was not called
            // thus there is nothing to stop
            PendingIntent mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
        }
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> mGeofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     *
     * @return Intent which will be called
     */
    public PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}