package com.infinitedimensions.somanami;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.infinitedimensions.somanami.network.SyncAlarm;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);


        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        startAlarmService();
    }



    public void startAlarmService(){
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
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
