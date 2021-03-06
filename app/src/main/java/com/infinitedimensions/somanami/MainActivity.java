package com.infinitedimensions.somanami;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
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


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    public List<Book> contentList;
    public List<TrayItem> trayList;
    public ConnectionDetector cd;
    public boolean isInternetPresent;
    private SimpleDBHandler dbHandler;
    SharedPreferences prefs;
    String user_id = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        checkCreds();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        cd = new ConnectionDetector(getApplicationContext());
        //get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        if(isInternetPresent) {
            new getContent().execute();
        }else{
            //TODO
        }

    }

    public void checkCreds(){
        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String logged_in = pref.getString("logged_in", "0");
        if(logged_in.equals("0")) {
            Intent i = new Intent(getApplicationContext(), FacebookLogin.class);
            startActivity(i);
            finish();
        }
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





    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            //
            mNavigationDrawerFragment.closeNavDrawer();
        }else{

            finish();
        }
    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        supportInvalidateOptionsMenu();

        if(position == 2){

            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, MyBooksFragment.newInstance(position))
                    .commit();
        }else if(position == 3){
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, TrayFragment.newInstance(position))
                    .commit();
        }else{
            if(getIntent().hasExtra("note_type")){
                //show notifications
                getIntent().removeExtra("note_type");
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, NotificationsFragment.newInstance(9))
                        .commit();
            }else{

                //show library
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, LibraryFragment.newInstance(1))
                        .commit();
           }

        }

    }
    public void setActionBarTitle(String title){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    public void onSectionAttached(int number, String somevalue) {
        switch (number) {
            case 1:
                mTitle = getResources().getStringArray(R.array.nav_items)[0];
                break;
            case 2:
                mTitle = getResources().getStringArray(R.array.nav_items)[1];
                break;
            case 3:
                mTitle = getResources().getStringArray(R.array.nav_items)[2];
                break;
            case 8:

                mTitle = somevalue;
                break;
            case 9:
                mTitle = "My notifications";
                break;

        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {

            addDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addDialog(){
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Add your book");


        dialog.setContentView(R.layout.dialog_add);

        final TextView tvQuery = (TextView)dialog.findViewById(R.id.eTSearch);

        //set onclicklisteners
        dialog.findViewById(R.id.button_discard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(getApplicationContext(), AddBook.class);


                String query = (tvQuery).getText().toString();

                i.putExtra("query", query);
                startActivity(i);
                dialog.cancel();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    /**
     * Logout From Facebook
     */
    public void callFacebookLogout(Context context) {
        Session session = Session.getActiveSession();
        if (session != null) {

            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
                //clear your preferences if saved
            }
        } else {

            session = new Session(context);
            Session.setActiveSession(session);

            session.closeAndClearTokenInformation();
            //clear your preferences if saved

        }

        Intent i = new Intent(this, FacebookLogin.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);

       finish();

    }
}
