package com.sketchproject.infogue.modules;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.PostActivity;
import com.sketchproject.infogue.models.Article;

/**
 * Sketch Project Studio
 * Created by Angga on 07/05/2016 15.15.
 */
public class NotificationsListenerService extends GcmListenerService {

    private static final String TAG = "Infogue/GcmListener";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String slug = data.getString(Article.SLUG);
        String title = data.getString(Article.TITLE);
        String featured = data.getString(Article.FEATURED_REF);
        int id = data.getInt(Article.ID);

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Slug: " + slug);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            if (new SessionManager(getBaseContext()).getSessionData(SessionManager.KEY_NOTIFICATION, false)) {
                sendNotification(message, id, slug, title, featured);
            }
        } else {
            sendNotification(message, id, slug, title, featured);
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, int id, String slug, String title, String featured) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra(Article.ID, id);
        intent.putExtra(Article.SLUG, slug);
        intent.putExtra(Article.FEATURED, featured);
        intent.putExtra(Article.TITLE, title);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_whatshot)
                .setContentTitle("Infogue.id Update")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
