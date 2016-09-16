package com.sketchproject.infogue.modules;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.activities.ConversationActivity;
import com.sketchproject.infogue.activities.PostActivity;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Message;

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
        Log.i("Infogue/Notification", from);

        if (from.startsWith("/topics/")) {
            SessionManager session = new SessionManager(this);
            // message received from some topic.
            if (from.startsWith("/topics/article")) {
                Log.i("Infogue/Notification", "new article");
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
                //String message = data.getString("message");
                String slug = data.getString(Article.SLUG);
                String title = data.getString(Article.TITLE);
                String featured = data.getString(Article.FEATURED_REF);
                int id = Integer.parseInt(data.getString(Article.ID));

                Log.d(TAG, "From: " + from);
                //Log.d(TAG, "Message: " + message);
                Log.d(TAG, "Slug: " + slug);
                Log.d(TAG, "Title: " + title);
                Log.d(TAG, "Featured: " + featured);

                if (session.isLoggedIn()) {
                    if (session.getSessionData(SessionManager.KEY_NOTIFICATION, false)) {
                        sendNotificationArticle(title, id, slug, title, featured);
                    }
                } else {
                    sendNotificationArticle(title, id, slug, title, featured);
                }
            } else if (from.startsWith("/topics/message")) {
                Log.i("Infogue/Notification", "new message");

                int id = Integer.parseInt(data.getString(Contributor.ID));
                String name = data.getString(Contributor.NAME);
                String username = data.getString(Contributor.USERNAME);
                String avatar = data.getString(Contributor.AVATAR);
                String gcmToken = data.getString(Contributor.GCM_TOKEN);
                String message = data.getString("conversation");

                if (session.isLoggedIn()) {
                    if (session.getSessionData(SessionManager.KEY_NOTIFICATION, false)) {
                        if (session.getSessionData(SessionManager.KEY_TOKEN_GCM, "").equals(gcmToken)) {
                            sendNotificationMessage(message, id, name, username, avatar);
                        }
                    }
                }
            }
        } else {
            // normal downstream message.
            Log.i("Infogue/Notification", "Downstream message");
        }
    }

    /**
     * Send notification when user got message, and redirect to conversation activity
     * when user tap the notification.
     *
     * @param message  new message from people
     * @param id       user id of contributor
     * @param name     contributor name
     * @param username contributor username
     * @param avatar   profile image
     */
    private void sendNotificationMessage(String message, int id, String name, String username, String avatar) {
        Intent conversationIntent = new Intent(this, ConversationActivity.class);
        conversationIntent.putExtra(Message.USERNAME, username);
        conversationIntent.putExtra(Message.CONTRIBUTOR_ID, id);
        conversationIntent.putExtra(Message.NAME, name);
        conversationIntent.putExtra(Message.AVATAR, avatar);
        conversationIntent.putExtra(ConversationActivity.NEW_CONVERSATION, false);
        conversationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, conversationIntent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_mail)
                .setContentTitle("New message from " + name)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message  article summary
     * @param id       article id
     * @param slug     title slug reference
     * @param title    article title
     * @param featured image article feature
     */
    private void sendNotificationArticle(String message, int id, String slug, String title, String featured) {
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

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
