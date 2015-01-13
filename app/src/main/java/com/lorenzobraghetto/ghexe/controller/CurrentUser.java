package com.lorenzobraghetto.ghexe.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by monossido on 14/12/14.
 */
public class CurrentUser {

    private static CurrentUser instance;
    private String second_name;
    private String first_name;
    private int id;

    private static final String SENDER_ID = "1039215285235";

    public static CurrentUser getInstance() {
        if (instance == null)
            instance = new CurrentUser();
        return instance;
    }

    public void setAuthenticated(Context context, int id, String first_name, String second_name, String access_token, String refresh_token, int expires_in) {
        this.id = id;
        this.first_name = first_name;
        this.second_name = second_name;
        setTokens(context, access_token, refresh_token, expires_in);
    }

    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String registrationId = prefs.getString("reg_id", "");
        if (registrationId.isEmpty()) {
            Log.i("GHEXE", "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt("app_version", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GHEXE", "App version changed.");
            return "";
        }
        return registrationId;
    }

    public void registerInBackground(final GoogleCloudMessaging gcmTemp, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                GoogleCloudMessaging gcm = gcmTemp;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    String regid = gcm.register(SENDER_ID);

                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                }
                return null;
            }
        }.execute();
    }

    private void storeRegistrationId(Context context, String regId) {
        Log.v("GHEXE", "storeRegistrationId, regid=" + regId);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("GHEXE", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("reg_id", regId);
        editor.putInt("app_version", appVersion);
        editor.commit();
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public void setTokens(Context context, String access_token, String refresh_token, int expires_in) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("access_token", access_token);
        edit.putString("refresh_token", refresh_token);
        edit.putInt("expires_in", expires_in);
        edit.commit();
    }

    public String getSecond_name() {
        return second_name;
    }

    public void setSecond_name(String second_name) {
        this.second_name = second_name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExpires_in(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt("expires_in", 0);
    }

    public String getAccess_token(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("access_token", "");
    }

    public String getRefresh_token(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("refresh_token", "");
    }
}
