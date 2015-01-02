package com.infinitedimensions.somanami;

import android.app.Dialog;
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
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;


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
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            //
            mNavigationDrawerFragment.closeNavDrawer();
        }
    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {

        if (position == 4) {
            callFacebookLogout(getApplicationContext());

        }else if(position ==0){
            //show library
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, LibraryFragment.newInstance(position + 1))
                    .commit();

        }else if(position ==1){
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, MyBooksFragment.newInstance(position + 1))
                    .commit();
        }else if(position == 3){
            sendRequestDialog();
        }
    }
    private void sendRequestDialog() {
        Bundle params = new Bundle();
        params.putString("message", "Join Somanasi to share your books with your friends and borrow theirs");

        WebDialog requestsDialog = (
                new WebDialog.RequestsDialogBuilder(this,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error != null) {
                            if (error instanceof FacebookOperationCanceledException) {
                                Toast.makeText(getApplicationContext(),
                                        "Request cancelled",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Network Error",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            final String requestId = values.getString("request");
                            if (requestId != null) {
                                Toast.makeText(getApplicationContext(),
                                        "Request sent",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Request cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                })
                .build();
        requestsDialog.show();
    }
    public void onSectionAttached(int number) {
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
            default:
                mTitle = "";
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

        if (BuildConfig.DEBUG) {
            tvQuery.setText("Chaos");
        }

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

       finish();

    }
}
