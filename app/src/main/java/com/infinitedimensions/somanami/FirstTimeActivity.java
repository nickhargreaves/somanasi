package com.infinitedimensions.somanami;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.infinitedimensions.somanami.helpers.ConnectionDetector;
import com.infinitedimensions.somanami.helpers.GifAnimationDrawable;
import com.infinitedimensions.somanami.helpers.SimpleDBHandler;
import com.infinitedimensions.somanami.models.Book;
import com.infinitedimensions.somanami.models.Friend;
import com.infinitedimensions.somanami.models.TrayItem;
import com.infinitedimensions.somanami.network.SyncAlarm;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class FirstTimeActivity extends ActionBarActivity {
    List<Book> contentList;
    public List<TrayItem> trayList;
    private ImageView loading_gif;
    private GifAnimationDrawable little;

    private RelativeLayout rlloading;

    private ConnectionDetector cd;
    private Boolean isInternetPresent;

    private TextView notification;

    String user_id = "0";
    SimpleDBHandler dbHandler;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_time);

        //set not first time
        setNotFirstTime();
        //getBooks
        notification = (TextView)findViewById(R.id.notification);

        loading_gif = (ImageView)findViewById(R.id.ivLoading);
        rlloading = (RelativeLayout)findViewById(R.id.rlLoading);
        rlloading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConditions();
            }
        });
        // and add the GifAnimationDrawable
        try{

            little = new GifAnimationDrawable(getResources().openRawResource(R.raw.loading_anim));
            little.setOneShot(false);
            loading_gif.setImageDrawable(little);
        }catch(IOException ioe){

        }

        checkConditions();

    }
    public void checkConditions(){
        notification.setText("Setting up...");
        //check if has network
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        //get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        if(!isInternetPresent){

            Toast.makeText(getApplicationContext(), "Check your internet settings!", Toast.LENGTH_LONG).show();

            //TODO:timer to check settings
            notification.setText("Tap to retry!");

        }else{
                new getContent().execute();
        }

    }

    public void setNotFirstTime(){

        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();
        editor.putString("first_time", "0");
        editor.commit();

    }

    public void startAlarmService(){
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
    }

    class getContent extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dbHandler = new SimpleDBHandler(getApplicationContext(), null, null, 1);

            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            user_id = prefs.getString("user_id", "0");

            contentList = new ArrayList<Book>();
            trayList = new ArrayList<TrayItem>();

        }

        protected String doInBackground(String... args) {

            //getting library


            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                String url = Defaults.API_URL + "public/listbooks";
                HttpResponse response = httpClient
                        .execute(new HttpGet(url));
                Log.d("url", "url:" + url);

                InputStream is = response.getEntity().getContent();

                JsonFactory factory = new JsonFactory();

                JsonParser jsonParser = factory.createJsonParser(is);

                JsonToken token = jsonParser.nextToken();

                // Expected JSON is an array so if current token is "[" then while
                // we don't get
                // "]" we will keep parsing
                if (token == JsonToken.START_ARRAY) {
                    while (token != JsonToken.END_ARRAY) {
                        // Inside array there are many objects, so it has to start
                        // with "{" and end with "}"
                        token = jsonParser.nextToken();
                        if (token == JsonToken.START_OBJECT) {

                            while (token != JsonToken.END_OBJECT) {
                                // Each object has a name which we will use to
                                // identify the type.
                                token = jsonParser.nextToken();
                                if (token == JsonToken.FIELD_NAME) {
                                    String objectName = jsonParser.getCurrentName();
                                    // jsonParser.nextToken();
                                    //if (0 == objectName.compareToIgnoreCase("CONTENT")) {
                                    Book book = new Book();
                                    book.parse(jsonParser);
                                    //create card for this content
                                    contentList.add(book);

                                    //}
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

            //get tray
            httpClient = new DefaultHttpClient();
            try {
                String url = Defaults.API_URL + "public/my_tray/" + user_id;
                HttpResponse response = httpClient
                        .execute(new HttpGet(url));
                Log.d("url", "url:" + url);

                InputStream is = response.getEntity().getContent();

                JsonFactory factory = new JsonFactory();

                JsonParser jsonParser = factory.createJsonParser(is);

                JsonToken token = jsonParser.nextToken();

                // Expected JSON is an array so if current token is "[" then while
                // we don't get
                // "]" we will keep parsing
                if (token == JsonToken.START_ARRAY) {
                    while (token != JsonToken.END_ARRAY) {
                        // Inside array there are many objects, so it has to start
                        // with "{" and end with "}"
                        token = jsonParser.nextToken();
                        if (token == JsonToken.START_OBJECT) {

                            while (token != JsonToken.END_OBJECT) {
                                // Each object has a name which we will use to
                                // identify the type.
                                token = jsonParser.nextToken();
                                if (token == JsonToken.FIELD_NAME) {
                                    String objectName = jsonParser.getCurrentName();
                                    // jsonParser.nextToken();
                                    //if (0 == objectName.compareToIgnoreCase("CONTENT")) {
                                    TrayItem trayItem = new TrayItem();
                                    trayItem.parse(jsonParser);
                                    //create card for this content
                                    trayList.add(trayItem);

                                    //}
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {

            for(int i = 0; i<contentList.size(); i++) {

                Book b = contentList.get(i);

                if (!dbHandler.bookExists(b.getId())) {
                    //add book to db
                    dbHandler.addBook(b);
                }
            }

            for(int i = 0; i<trayList.size(); i++) {

                TrayItem t = trayList.get(i);

                if (!dbHandler.trayItemExists(t.getId())) {
                    //add book to db
                    dbHandler.addTrayItem(t);
                }

            }
            setUpFriends();

            //start alarm service for fetching content
            startAlarmService();

            //redirect to home screen
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i);
            finish();

        }

    }

    public void setUpFriends(){
        //get friends
        if(Session.getActiveSession()!=null)
            requestMyAppFacebookFriends(Session.getActiveSession());
    }

    private void requestMyAppFacebookFriends(Session session) {
        Request friendsRequest = createRequest(session);
        friendsRequest.setCallback(new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                List<GraphUser> friends = getResults(response);

                for (int i = 0; i < friends.size(); i++) {
                    GraphUser user = friends.get(i);

                    Friend friend = new Friend();
                    friend.setName(user.getName());
                    friend.setFid(user.getId());
                    dbHandler.addFriends(friend);
                }
            }
        });
        friendsRequest.executeAndWait();
    }

    private List<GraphUser> getResults(Response response) {
        GraphMultiResult multiResult = response
                .getGraphObjectAs(GraphMultiResult.class);
        GraphObjectList<GraphObject> data = multiResult.getData();
        return data.castToListOf(GraphUser.class);
    }

    private Request createRequest(Session session) {
        Request request = Request.newGraphPathRequest(session, "me/friends", null);

        Set<String> fields = new HashSet<String>();
        String[] requiredFields = new String[] { "id", "name", "picture",
                "installed" };
        fields.addAll(Arrays.asList(requiredFields));

        Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);

        return request;
    }

}
