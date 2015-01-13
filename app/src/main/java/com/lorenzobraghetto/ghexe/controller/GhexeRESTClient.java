package com.lorenzobraghetto.ghexe.controller;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.lorenzobraghetto.ghexe.model.Event;
import com.lorenzobraghetto.ghexe.model.Presence;
import com.lorenzobraghetto.ghexe.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by monossido on 14/12/14.
 */
public class GhexeRESTClient {

    private static GhexeRESTClient instance;

    public static GhexeRESTClient getInstance() {
        if (instance != null)
            return instance;
        else return new GhexeRESTClient();
    }

    public void postAuthenticate(final Context context, final String first_name, String password, final HttpCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("grant_type", "password");
        json.addProperty("email", first_name);
        json.addProperty("password", password);
        json.addProperty("scope", "user");

        Ion.with(context)
                .load(GhexeParams.LOGIN_URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.v("GHEXE", "postAuthenticate result=" + result);
                        Log.v("GHEXE", "postAuthenticate e=" + e);
                        if (e == null) {
                            try {
                                String access_token = result.get("access_token").getAsString();
                                String refresh_token = result.get("refresh_token").getAsString();
                                int expires_in = result.get("expires_in").getAsInt();

                                getMe(context, access_token, refresh_token, expires_in, callback);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                callback.onFailure();
                            }
                        } else
                            callback.onFailure();
                    }

                });

    }

    synchronized public void refreshToken(final Context context, final HttpCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("grant_type", "refresh_token");
        json.addProperty("refresh_token", CurrentUser.getInstance().getRefresh_token(context));
        Log.i("GHEXE", "refreshtoke body=" + json);

        Ion.with(context)
                .load(GhexeParams.LOGIN_URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            Log.i("GHEXE", "refreshtoke result=" + result);
                            Log.i("GHEXE", "refreshtoke e=" + e);
                            try {
                                String access_token = result.get("access_token").getAsString();
                                String refresh_token = result.get("refresh_token").getAsString();
                                int expires_in = result.get("expires_in").getAsInt();
                                CurrentUser.getInstance().setTokens(context, access_token, refresh_token, expires_in);
                                if (callback != null)
                                    callback.onSuccess(null);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                if (callback != null)
                                    callback.onFailure();
                            }
                        } else {
                            if (callback != null)
                                callback.onFailure();
                        }
                    }

                });
    }


    public void getMe(final Context context, final String accessToken, final String refresToken, final int expiresIn, final HttpCallback callback) {
        String url = GhexeParams.ME_URL + "?access_token=" + accessToken;
        Log.v("GHEXE", "getMe url=" + url);
        Ion.with(context).load("GET", url).asJsonObject().withResponse().setCallback(new FutureCallback<Response<JsonObject>>() {
            @Override
            public void onCompleted(Exception e, Response<JsonObject> result) {
                if (result != null && result.getHeaders().code() == 401) {
                    refreshToken(context, new HttpCallback() {

                        @Override
                        public void onSuccess(List<Object> resultList) {
                            getMe(context, CurrentUser.getInstance().getAccess_token(context)
                                    , CurrentUser.getInstance().getRefresh_token(context)
                                    , CurrentUser.getInstance().getExpires_in(context), callback);
                        }

                        @Override
                        public void onFailure() {
                            callback.onFailure();
                        }
                    });
                    return;
                }
                Log.v("GHEXE", "getMe result=" + result.getResult());
                Log.v("GHEXE", "getMe e=" + e);
                JsonObject resultResult = result.getResult();
                CurrentUser.getInstance().setAuthenticated(context, resultResult.get("id").getAsInt(), resultResult.get("first_name").getAsString(), resultResult.get("second_name").getAsString()
                        , accessToken, refresToken, expiresIn);
                if (resultResult.get("gcm").isJsonNull() || resultResult.get("gcm").getAsString().equals(CurrentUser.getInstance().getRegistrationId(context)))
                    postRegistrationId(context.getApplicationContext());
                callback.onSuccess(null);
            }
        });
    }

    private void postRegistrationId(final Context context) {
        String url = GhexeParams.ME_URL + "?access_token=" + CurrentUser.getInstance().getAccess_token(context);
        Log.v("GHEXE", "postRegistrationId url=" + url);
        JsonObject body = new JsonObject();
        JsonObject userJson = new JsonObject();
        userJson.addProperty("gcm", CurrentUser.getInstance().getRegistrationId(context.getApplicationContext()));
        body.add("user", userJson);
        Ion.with(context).load("PUT", url).setJsonObjectBody(body).asJsonObject().withResponse().setCallback(new FutureCallback<Response<JsonObject>>() {
            @Override
            public void onCompleted(Exception e, Response<JsonObject> result) {
                Log.v("GHEXE", "postRegistrationId result=" + result);
                Log.v("GHEXE", "postRegistrationId e=" + e);
                if (result != null && result.getHeaders().code() == 401) {
                    refreshToken(context, new HttpCallback() {

                        @Override
                        public void onSuccess(List<Object> resultList) {
                            postRegistrationId(context);
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }
                if(result!=null) {
                    Log.v("GHEXE", "postRegistrationId result=" + result.getResult());
                    Log.v("GHEXE", "postRegistrationId e=" + e);
                }
            }
        });
    }

    public void getEvents(final Context context, final HttpCallback callback) {
        String url = GhexeParams.EVENTS_URL + "?access_token=" + CurrentUser.getInstance().getAccess_token(context);
        Log.v("GHEXE", "getEvents url=" + url);
        Ion.with(context).load("GET", url).asJsonObject().withResponse().setCallback(new FutureCallback<Response<JsonObject>>() {
            @Override
            public void onCompleted(Exception e, Response<JsonObject> result) {
                if (result != null && result.getHeaders().code() == 401) {
                    refreshToken(context, new HttpCallback() {

                        @Override
                        public void onSuccess(List<Object> resultList) {
                            getEvents(context, callback);
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                    return;
                }
                Log.v("GHEXE", "getEvents result=" + result.getResult());
                Log.v("GHEXE", "getEvents e=" + e);
                List<Object> events = new ArrayList<Object>();
                JsonArray jsonEvents = result.getResult().get("events").getAsJsonArray();
                for (int i = 0; i < jsonEvents.size(); i++) {
                    JsonObject jsonEvent = jsonEvents.get(i).getAsJsonObject();
                    JsonArray jsonPresences = jsonEvent.get("presences").getAsJsonArray();
                    int eventId = jsonEvent.get("id").getAsInt();
                    List<Presence> presences = new ArrayList<Presence>();
                    for (int z = 0; z < jsonPresences.size(); z++) {
                        JsonObject jsonPresence = jsonPresences.get(z).getAsJsonObject();
                        JsonObject jsonUser = jsonPresence.get("user").getAsJsonObject();

                        User user = new User(jsonUser.get("id").getAsInt(), jsonUser.get("first_name").getAsString(), jsonUser.get("second_name").getAsString());
                        Presence presence = new Presence(jsonPresence.get("id").getAsInt(), eventId, user, jsonPresence.get("presence").getAsBoolean());
                        presences.add(presence);
                    }
                    Event event = new Event(jsonEvent.get("id").getAsInt(), jsonEvent.get("title").getAsString(), jsonEvent.get("time").getAsString()
                            , jsonEvent.get("dayofweek").getAsInt(), presences);
                    events.add(event);
                }
                callback.onSuccess(events);
            }
        });
    }

    public void getPresence(final Context context, final HttpCallback callback) {
        String url = GhexeParams.PRESENCES_URL + "?access_token=" + CurrentUser.getInstance().getAccess_token(context);
        Ion.with(context).load("GET", url)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        if (result != null && result.getHeaders().code() == 401) {
                            refreshToken(context, new HttpCallback() {

                                @Override
                                public void onSuccess(List<Object> resultList) {
                                    getPresence(context, callback);
                                }

                                @Override
                                public void onFailure() {

                                }
                            });
                            return;
                        }
                        List<Object> presences = new ArrayList<Object>();
                        JsonArray jsonPresences = result.getResult().get("presences").getAsJsonArray();
                        for (int i = 0; i < jsonPresences.size(); i++) {
                            JsonObject jsonPresence = jsonPresences.get(i).getAsJsonObject();
                            JsonObject jsonEvent = jsonPresence.get("event").getAsJsonObject();
                            JsonObject jsonUser = jsonPresence.get("user").getAsJsonObject();

                            User user = new User(jsonUser.get("id").getAsInt(), jsonUser.get("first_name").getAsString(), jsonUser.get("second_name").getAsString());
                            Presence presence = new Presence(jsonPresence.get("id").getAsInt(), jsonEvent.get("id").getAsInt(), user, jsonPresence.get("presence").getAsBoolean());
                            presences.add(presence);
                        }
                        callback.onSuccess(presences);
                    }
                });
    }


    public void updatePresence(Context context, int id, boolean presence, final HttpCallback callback) {
        JsonObject json = new JsonObject();
        JsonObject jsonPresence = new JsonObject();
        jsonPresence.addProperty("presence", presence);
        json.add("presence", jsonPresence);
        Log.v("GHEXE", "json=" + json);
        String url = GhexeParams.PRESENCES_URL + "/" + id + "?access_token=" + CurrentUser.getInstance().getAccess_token(context);
        Ion.with(context).load("PUT", url)
                .setJsonObjectBody(json)
                .asJsonObject().withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        Log.v("GHEXE", "result=" + result.getResult());
                        callback.onSuccess(null);
                    }
                });
    }
}
