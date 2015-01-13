package com.lorenzobraghetto.ghexe.controller;

import android.app.Application;
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
 * Created by monossido on 12/01/15.
 */
public class GhexeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String regid = CurrentUser.getInstance().getRegistrationId(this);

            if (regid.isEmpty()) {
                CurrentUser.getInstance().registerInBackground(gcm, this);
            }
        }
    }

}
