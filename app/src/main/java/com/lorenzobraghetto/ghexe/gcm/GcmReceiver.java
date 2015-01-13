package com.lorenzobraghetto.ghexe.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.lorenzobraghetto.ghexe.R;
import com.lorenzobraghetto.ghexe.controller.GhexeRESTClient;
import com.lorenzobraghetto.ghexe.controller.HttpCallback;
import com.lorenzobraghetto.ghexe.view.LoginActivity;

import java.util.List;

public class GcmReceiver extends BroadcastReceiver {

    private final static String REVERT_PRESENCE = "INTENT_GHEXE_REVERT_PRESENCE";
    private static NotificationManager mNotificationManager;

    public GcmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notification", true)) {
            Bundle extras = intent.getExtras();
            String event_title = extras.getString("event_title");
            int presence_id = Integer.parseInt(extras.getString("presence_id"));
            boolean presence = Boolean.parseBoolean(extras.getString("presence"));

            mNotificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, LoginActivity.class), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("GCM Notification");
            if (presence) {
                String msg = String.format(context.getString(R.string.notification_true), event_title);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg);
            } else {
                String msg = String.format(context.getString(R.string.notification_false), event_title);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg);
            }

            mBuilder.setContentIntent(contentIntent);
            Intent revertIntent = new Intent(REVERT_PRESENCE);
            revertIntent.putExtra("presence", presence);
            revertIntent.putExtra("presence_id", presence_id);
            PendingIntent revertPendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            0,
                            revertIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.revert_presence), revertPendingIntent);
            mNotificationManager.notify(12345, mBuilder.build());
        }
    }

    public static class RevertReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GhexeRESTClient.getInstance().updatePresence(context, intent.getIntExtra("presence_id", -1), !intent.getBooleanExtra("presence", true), new HttpCallback() {

                @Override
                public void onSuccess(List<Object> resultList) {
                    mNotificationManager.cancelAll();
                }

                @Override
                public void onFailure() {

                }
            });
        }
    }
}
