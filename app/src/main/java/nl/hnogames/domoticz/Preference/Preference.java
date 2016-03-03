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

package nl.hnogames.domoticz.Preference;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.GeoSettingsActivity;
import nl.hnogames.domoticz.Interfaces.MobileDeviceReceiver;
import nl.hnogames.domoticz.NFCSettingsActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ServerListSettingsActivity;
import nl.hnogames.domoticz.ServerSettingsActivity;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.UI.SimpleTextDialog;
import nl.hnogames.domoticz.UpdateActivity;
import nl.hnogames.domoticz.Utils.DeviceUtils;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.ServerUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;

public class Preference extends PreferenceFragment {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = Preference.class.getSimpleName();
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG_IMPORT = "Import Settings";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG_EXPORT = "Export Settings";
    private SharedPrefUtil mSharedPrefs;
    private File SettingsFile;
    private Context mContext;
    private Domoticz mDomoticz;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mContext = getActivity();
        mSharedPrefs = new SharedPrefUtil(mContext);
        ServerUtil mServerUtil = new ServerUtil(mContext);
        mDomoticz = new Domoticz(mContext, mServerUtil);

        setPreferences();
        setStartUpScreenDefaultValue();
        setVersionInfo();
        handleImportExportButtons();
        handleInfoAndAbout();
    }

    private void setPreferences() {

        final android.preference.SwitchPreference MultiServerPreference = (android.preference.SwitchPreference) findPreference("enableMultiServers");
        android.preference.Preference ServerSettings = findPreference("server_settings");
        android.preference.ListPreference displayLanguage = (ListPreference) findPreference("displayLanguage");
        final android.preference.Preference registrationId = findPreference("notification_registration_id");
        android.preference.Preference GeoSettings = findPreference("geo_settings");
        android.preference.SwitchPreference WearPreference = (android.preference.SwitchPreference) findPreference("enableWearItems");
        android.preference.Preference NFCPreference = findPreference("nfc_settings");
        android.preference.SwitchPreference EnableNFCPreference = (android.preference.SwitchPreference) findPreference("enableNFC");
        MultiSelectListPreference drawerItems = (MultiSelectListPreference) findPreference("enable_menu_items");
        @SuppressWarnings("SpellCheckingInspection") android.preference.SwitchPreference AlwaysOnPreference = (android.preference.SwitchPreference) findPreference("alwayson");
        @SuppressWarnings("SpellCheckingInspection") android.preference.PreferenceScreen preferenceScreen = (android.preference.PreferenceScreen) findPreference("settingsscreen");
        android.preference.PreferenceCategory premiumCategory = (android.preference.PreferenceCategory) findPreference("premium_category");
        android.preference.Preference premiumPreference = findPreference("premium_settings");
        NotificationsMultiSelectListPreference notificationsMultiSelectListPreference = (NotificationsMultiSelectListPreference) findPreference("suppressNotifications");
        android.preference.SwitchPreference ThemePreference = (android.preference.SwitchPreference) findPreference("darkTheme");

        List<String> notifications = mSharedPrefs.getReceivedNotifications();
        if (notifications == null || notifications.size() <= 0) {
            notificationsMultiSelectListPreference.setEnabled(false);
        } else {
            notificationsMultiSelectListPreference.setEnabled(true);
        }

        drawerItems.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                try {
                    final HashSet selectedDrawerItems = (HashSet) newValue;
                    if (selectedDrawerItems.size() < 1) {
                        Toast.makeText(mContext, R.string.error_atLeastOneItemInDrawer,
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        ThemePreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_wear) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    ((SettingsActivity) getActivity()).reloadSettings();
                    return true;
                }
            }
        });

        MultiServerPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_wear) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });


        ServerSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (!MultiServerPreference.isChecked()) {
                    Intent intent = new Intent(mContext, ServerSettingsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, ServerListSettingsActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });

        displayLanguage.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                showRestartMessage();
                return true;
            }
        });

        registrationId.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessDeviceState(mContext)) {
                        requestPermissions(PermissionsUtil.INITIAL_DEVICE_PERMS, PermissionsUtil.INITIAL_DEVICE_REQUEST);
                    } else {
                        pushGCMRegistrationIds();
                    }
                } else {
                    pushGCMRegistrationIds();
                }
                return true;
            }
        });

        GeoSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.geofence) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    Intent intent = new Intent(mContext, GeoSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            }
        });

        EnableNFCPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_wear) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                }

                if (NfcAdapter.getDefaultAdapter(mContext) == null) {
                    Toast.makeText(mContext, getString(R.string.nfc_not_supported), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        NFCPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_nfc) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    Intent intent = new Intent(mContext, NFCSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            }
        });

        WearPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_wear) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        AlwaysOnPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_wear) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        //noinspection PointlessBooleanExpression
        if (!BuildConfig.LITE_VERSION) {
            preferenceScreen.removePreference(premiumCategory);
        } else {
            premiumPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.preference.Preference preference) {
                    String packageID = mContext.getPackageName() + ".premium";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageID)));
                    } catch (android.content.ActivityNotFoundException ignored) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageID)));
                    }

                    return true;
                }
            });
        }
    }

    private void pushGCMRegistrationIds() {
        final String UUID = DeviceUtils.getUniqueID(mContext);
        final String senderid = AppController.getInstance().getGCMRegistrationId();
        mDomoticz.CleanMobileDevice(UUID, new MobileDeviceReceiver() {
            @Override
            public void onSuccess() {
                //previous id cleaned
                mDomoticz.AddMobileDevice(UUID, senderid, new MobileDeviceReceiver() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(mContext, getString(R.string.notification_settings_pushed), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception error) {
                        Toast.makeText(mContext, getString(R.string.notification_settings_push_failed), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                //nothing to clean..
                mDomoticz.AddMobileDevice(UUID, senderid, new MobileDeviceReceiver() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(mContext, getString(R.string.notification_settings_pushed), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception error) {
                        Toast.makeText(mContext, getString(R.string.notification_settings_push_failed), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showRestartMessage() {
        new MaterialDialog.Builder(mContext)
                .title(R.string.restart_required_title)
                .content(mContext.getString(R.string.restart_required_msg)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + mContext.getString(R.string.restart_now))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UsefulBits.restartApplication(getActivity());
                    }
                })
                .show();
    }

    private void handleInfoAndAbout() {
        android.preference.Preference about = findPreference("info_about");
        about.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                SimpleTextDialog td = new SimpleTextDialog(mContext);
                td.setTitle(R.string.info_about);
                td.setText(R.string.welcome_info_domoticz);
                td.show();
                return true;
            }
        });
        android.preference.Preference credits = findPreference("info_credits");
        credits.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                String text = getString(R.string.info_credits_text);
                text = text + ":\n\n" + getString(R.string.info_credits_text_urls);

                SimpleTextDialog td = new SimpleTextDialog(mContext);
                td.setTitle(R.string.info_credits);
                td.setText(text);
                td.show();
                return false;
            }
        });
    }

    private void handleImportExportButtons() {
        SettingsFile = new File(Environment.getExternalStorageDirectory(),
                "/Domoticz/DomoticzSettings.txt");
        final String sPath = SettingsFile.getPath().
                substring(0, SettingsFile.getPath().lastIndexOf("/"));
        //noinspection unused
        boolean mkdirsResultIsOk = new File(sPath).mkdirs();

        android.preference.Preference exportButton = findPreference("export_settings");
        exportButton.setOnPreferenceClickListener(
                new android.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(android.preference.Preference preference) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!PermissionsUtil.canAccessStorage(mContext)) {
                                requestPermissions(PermissionsUtil.INITIAL_STORAGE_PERMS,
                                        PermissionsUtil.INITIAL_EXPORT_SETTINGS_REQUEST);
                            } else
                                exportSettings();
                        } else
                            exportSettings();

                        return false;
                    }
                });

        android.preference.Preference importButton = findPreference("import_settings");
        importButton.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessStorage(mContext)) {
                        requestPermissions(PermissionsUtil.INITIAL_STORAGE_PERMS,
                                PermissionsUtil.INITIAL_IMPORT_SETTINGS_REQUEST);
                    } else
                        importSettings();
                } else
                    importSettings();

                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.INITIAL_IMPORT_SETTINGS_REQUEST:
                if (PermissionsUtil.canAccessStorage(mContext)) {
                    importSettings();
                }
                break;
            case PermissionsUtil.INITIAL_EXPORT_SETTINGS_REQUEST:
                if (PermissionsUtil.canAccessStorage(mContext)) {
                    exportSettings();
                }
                break;
            case PermissionsUtil.INITIAL_DEVICE_REQUEST:
                if (PermissionsUtil.canAccessDeviceState(mContext))
                    pushGCMRegistrationIds();
                break;
        }
    }

    private void importSettings() {
        Log.v(TAG_IMPORT, "Importing settings from: " + SettingsFile.getPath());
        mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile);
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
            Toast.makeText(mContext,
                    R.string.settings_imported,
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext,
                    R.string.settings_import_failed,
                    Toast.LENGTH_SHORT).show();
    }

    private void exportSettings() {
        Log.v(TAG_EXPORT, "Exporting settings to: " + SettingsFile.getPath());
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
            Toast.makeText(mContext, R.string.settings_exported, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, R.string.settings_export_failed, Toast.LENGTH_SHORT).show();
    }

    private void setVersionInfo() {
        ServerUtil serverUtil = new ServerUtil(mContext);

        PackageInfo pInfo = null;
        try {
            pInfo = mContext
                    .getPackageManager()
                    .getPackageInfo(mContext
                            .getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appVersionStr = mContext.getString(R.string.unknown);
        if (pInfo != null) appVersionStr = pInfo.versionName;

        final android.preference.Preference appVersion = findPreference("version");
        appVersion.setSummary(appVersionStr);

        final android.preference.Preference domoticzVersion = findPreference("version_domoticz");

        String message;

        try {

            if (serverUtil.getActiveServer() != null) {
                if ((serverUtil.getActiveServer().getServerUpdateInfo() != null && serverUtil.getActiveServer().getServerUpdateInfo().isUpdateAvailable() && !UsefulBits.isEmpty(serverUtil.getActiveServer().getServerUpdateInfo().getCurrentServerVersion())) ||
                        mSharedPrefs.isDebugEnabled()) {

                    // Update is available or debugging is enabled
                    String version;
                    if (mSharedPrefs.isDebugEnabled())
                        version = mContext.getString(R.string.debug_test_text);
                    else
                        version = (serverUtil.getActiveServer().getServerUpdateInfo() != null) ? serverUtil.getActiveServer().getServerUpdateInfo().getUpdateRevisionNumber() : "";

                    message = String.format(getString(R.string.update_available_enhanced),
                            serverUtil.getActiveServer().getServerUpdateInfo().getCurrentServerVersion(),
                            version);
                    if (serverUtil.getActiveServer().getServerUpdateInfo() != null &&
                            serverUtil.getActiveServer().getServerUpdateInfo().getSystemName() != null &&
                            serverUtil.getActiveServer().getServerUpdateInfo().getSystemName().equalsIgnoreCase("linux")) {
                        // Only offer remote/auto update on Linux systems
                        message += UsefulBits.newLine() + mContext.getString(R.string.click_to_update_server);
                        domoticzVersion.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(android.preference.Preference preference) {
                                Intent intent = new Intent(mContext, UpdateActivity.class);
                                startActivity(intent);
                                return false;
                            }
                        });
                    }
                } else {
                    message = (serverUtil.getActiveServer().getServerUpdateInfo() != null &&
                            !UsefulBits.isEmpty(serverUtil.getActiveServer().getServerUpdateInfo().getUpdateRevisionNumber())) ? serverUtil.getActiveServer().getServerUpdateInfo().getUpdateRevisionNumber() : "";
                }
                domoticzVersion.setSummary(message);
            }

        } catch (Exception ex) {
            if (ex != null && !UsefulBits.isEmpty(ex.getMessage()))
                Log.e(TAG, ex.getMessage());
        }
    }

    private void setStartUpScreenDefaultValue() {
        int defaultValue = mSharedPrefs.getStartupScreenIndex();
        ListPreference startup_screen = (ListPreference) findPreference("startup_screen");
        startup_screen.setValueIndex(defaultValue);
    }
}