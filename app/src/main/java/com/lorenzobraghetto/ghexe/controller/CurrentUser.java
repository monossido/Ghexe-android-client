package com.lorenzobraghetto.ghexe.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by monossido on 14/12/14.
 */
public class CurrentUser {

    private static CurrentUser instance;
    private String second_name;
    private String first_name;
    private int id;

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
