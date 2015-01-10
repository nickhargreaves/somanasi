package com.infinitedimensions.somanami.network;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.infinitedimensions.somanami.Defaults;
import com.infinitedimensions.somanami.helpers.ConnectionDetector;
import com.infinitedimensions.somanami.helpers.SimpleDBHandler;
import com.infinitedimensions.somanami.models.Book;
import com.infinitedimensions.somanami.models.TrayItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 1/9/15.
 */
public class ContentService extends Service {

    public List<Book> contentList;
    public List<TrayItem> trayList;
    public ConnectionDetector cd;
    public boolean isInternetPresent;
    private SimpleDBHandler dbHandler;
    SharedPreferences prefs;
    String user_id = "0";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dbHandler = new SimpleDBHandler(getApplicationContext(), null, null, 1);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        user_id = prefs.getString("user_id", "0");

        cd = new ConnectionDetector(getApplicationContext());
        //get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        if(isInternetPresent) {
            new getContent().execute();
        }else{
            //TODO
        }

        return Service.START_NOT_STICKY;
    }
    class getContent extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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

        }

    }

}
