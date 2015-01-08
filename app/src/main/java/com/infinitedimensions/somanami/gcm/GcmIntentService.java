package com.infinitedimensions.somanami.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.infinitedimensions.somanami.MainActivity;
import com.infinitedimensions.somanami.R;
import com.infinitedimensions.somanami.models.Message;
import com.infinitedimensions.somanami.models.NotificationGCM;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

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
                new sendNotification(getApplicationContext(), "Send error: " + extras.toString(), "", "", "").execute();
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                new sendNotification(getApplicationContext(), "Deleted messages on server: " +
                        extras.toString(),"", "", "").execute();
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
                new sendNotification(getApplicationContext(), extras.getString("Notice"), user, name, note_type).execute();
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.


    private class sendNotification extends AsyncTask<String, Void, Bitmap> {

        Context ctx;
        String msg;
        String user_id;
        String name;
        String note_type;
        Bitmap bmp;

        public sendNotification(Context context, String _msg, String _user_id, String _name, String _note_type) {
            super();
            this.ctx = context;
            this.msg = _msg;
            this.user_id = _user_id;
            this.name = _name;
            this.note_type = _note_type;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String image_value = "http://graph.facebook.com/"+this.user_id+"/picture?type=normal";

            String final_image_value="";

            try
            {
                URL obj = new URL(image_value);
                URLConnection conn = obj.openConnection();
                Map<String, List<String>> map = conn.getHeaderFields();

                final_image_value = map.get("Location").toString();

                final_image_value = final_image_value.replace("[", "");

                final_image_value = final_image_value.replace("]", "");


            } catch (Exception e) {
                e.printStackTrace();
            }

            InputStream in;

            try {

                in = new URL(final_image_value).openStream();
                this.bmp = BitmapFactory.decodeStream(in);
                return bmp;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);
            try {
                mNotificationManager = (NotificationManager)
                        ctx.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent i = new Intent(ctx, MainActivity.class);

                if(!note_type.equals("")){
                    i.putExtra("note_type", note_type);
                }

                PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, i, 0);

                String title = "Somanasi";

                if(!name.equals("")){
                    title = name;
                }

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                // .setSmallIcon(R.drawable.ic_stat_gcm)
                                .setContentTitle(title)
                                .setSmallIcon(R.drawable.app_icon)
                                .setLargeIcon(bmp)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(msg))
                                .setContentText(msg);

                mBuilder.setContentIntent(contentIntent);

                Notification notification = mBuilder.build();

                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                mNotificationManager.notify(NOTIFICATION_ID, notification);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}