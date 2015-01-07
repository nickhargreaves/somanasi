package com.infinitedimensions.somanami.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.infinitedimensions.somanami.MainActivity;
import com.infinitedimensions.somanami.R;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), "", "", "");
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(),"", "", "");
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i=0; i<5; i++) {
                    Log.i(TAG, "Working... " + (i+1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

                String user = "";
                String name = "";
                String note_type = "";
                //add notification to db
                if(intent.hasExtra("notification")){
                    String message = extras.getString("Notice");
                    user = extras.getString("user");
                    name = extras.getString("name");
                    String type = extras.getString("type");
                    String book = extras.getString("book");
                    note_type = "1";

                    NotificationGCM notificationGCM = new NotificationGCM();
                    notificationGCM.setType(type);
                    notificationGCM.setMessage(message);
                    notificationGCM.setUser(user);
                    notificationGCM.setBook(book);

                    SimpleDBHandler db = new SimpleDBHandler(getApplicationContext(), null, null, 1);
                    db.addNotification(notificationGCM);

                }

                //add message to db
                if(intent.hasExtra("message")){
                    String messageText = extras.getString("Notice");
                    user = extras.getString("user");
                    name = extras.getString("name");
                    note_type = "2";

                    Message message = new Message();
                    message.setMessage(messageText);
                    message.setUser(user);
                    message.setIsMine("0");

                    SimpleDBHandler db = new SimpleDBHandler(getApplicationContext(), null, null, 1);
                    db.addMessage(message);

                }

                Log.i(TAG, "Received: " + extras.toString());

                // Post notification of received message.
                sendNotification(extras.getString("Notice"), user, name, note_type);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String user_id, String name, String note_type) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(this, MainActivity.class);

        if(!note_type.equals("")){
            i.putExtra("note_type", note_type);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

        String title = "Somanasi";

        if(!name.equals("")){
            title = name;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        // .setSmallIcon(R.drawable.ic_stat_gcm)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.app_icon)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}