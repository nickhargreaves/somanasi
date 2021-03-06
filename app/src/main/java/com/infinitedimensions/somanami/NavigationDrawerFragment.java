package com.infinitedimensions.somanami;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.infinitedimensions.somanami.helpers.RoundedImageView;
import com.infinitedimensions.somanami.helpers.SimpleDBHandler;
import com.infinitedimensions.somanami.models.Friend;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private String[] nav_items;

    private TypedArray nav_icons;

    private ArrayList<String> friend_names = new ArrayList<String>();
    private ArrayList<String> friend_ids = new ArrayList<String>();

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }
    private void sendRequestDialog() {
        Bundle params = new Bundle();
        params.putString("message", "Join Somanasi to share your books with your friends and borrow theirs");

        WebDialog requestsDialog = (
                new WebDialog.RequestsDialogBuilder(getActivity(),
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error != null) {
                            if (error instanceof FacebookOperationCanceledException) {
                                Toast.makeText(getActionBar().getThemedContext(),
                                        "Request cancelled",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActionBar().getThemedContext(),
                                        "Network Error",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            final String requestId = values.getString("request");
                            if (requestId != null) {
                                Toast.makeText(getActionBar().getThemedContext(),
                                        "Request sent",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActionBar().getThemedContext(),
                                        "Request cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                })
                .build();
        requestsDialog.show();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        //display friends
        setUpFriendsList();
        /*
        mDrawerListView.setAdapter(new ArrayAdapter<String>(
                getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                getResources().getStringArray(R.array.nav_items)
                ));
        */
        nav_items = getResources().getStringArray(R.array.nav_items);

        nav_icons = getResources().obtainTypedArray(R.array.nav_icons);

        AdapterClass adClass = new AdapterClass(getActionBar().getThemedContext(), nav_items, nav_icons);

        mDrawerListView.setAdapter(adClass);

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        addDrawerHeader();

        return mDrawerListView;
    }


    private void addDrawerHeader(){
        LayoutInflater inflater =  (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View header = (View) inflater.inflate(R.layout.drawer_header_view,
                mDrawerListView, false);
        LinearLayout notifB = (LinearLayout)header.findViewById(R.id.notificationsL);
        notifB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                closeNavDrawer();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, NotificationsFragment.newInstance(9))
                        .commit();
            }
        });
        LinearLayout logout = (LinearLayout)header.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                closeNavDrawer();

                callFacebookLogout(getActionBar().getThemedContext());
            }
        });
        mDrawerListView.addHeaderView(header, null, false);
    }

    public void setUpFriendsList() {

        SimpleDBHandler dbHandler = new SimpleDBHandler(getActionBar().getThemedContext(), null, null, 1);
        List<Friend> friends = dbHandler.getFriends();

        for(int i = 0; i<friends.size(); i++){

            friend_ids.add(friends.get(i).getFid());

            friend_names.add(friends.get(i).getName());
        }

        LayoutInflater inflater =  (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View footer_divider = (View) inflater.inflate(
                R.layout.drawer_list_footer_divider, null, false);
        RelativeLayout inviteRL = (RelativeLayout)footer_divider.findViewById(R.id.inviteL);
        inviteRL.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendRequestDialog();
            }
        });

        mDrawerListView.addFooterView(footer_divider, null, false);

        // Set up view
        View footer = (View) inflater.inflate(R.layout.drawer_list_footer_view,
                mDrawerListView, false);

        int minHeight = 3*100;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, minHeight);
        footer.setLayoutParams(params);

        ListView fl = (ListView)footer.findViewById(R.id.friendslist);

        fl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //get friend id
                String friend_id = friend_ids.get(position);
                String friend_name = friend_names.get(position);

                //close nav
                closeNavDrawer();

                //open user's book list
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, FriendBooksFragment.newInstance(8, friend_id, friend_name))
                        .commit();
            }
        });
        //set adapter

        AdapterClass2 adClass = new AdapterClass2(getActionBar().getThemedContext(), friend_names, friend_ids);

        fl.setAdapter(adClass);

        mDrawerListView.addFooterView(footer, null, true);

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

        //set logged out
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        editor = pref.edit();
        editor.putString("logged_in", "0");
        editor.commit();

        Intent i = new Intent(context, FacebookLogin.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);

        getActivity().finish();

    }
    public class AdapterClass2  extends ArrayAdapter<String> {
        Context context;
        private List<String> TextValue;
        private List<String> ImageValue;

        public AdapterClass2(Context context, List<String> TextValue, List<String> Image) {
            super(context, R.layout.drawer_list_footer_row, TextValue);
            this.context = context;
            this.TextValue= TextValue;
            this.ImageValue = Image;

        }

        @Override
        public View getView(int position, View coverView, ViewGroup parent) {
            // TODO Auto-generated method stub

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.drawer_list_footer_row,
                    parent, false);

            TextView text1 = (TextView)rowView.findViewById(R.id.textView1);
            text1.setText(TextValue.get(position));

            String id = ImageValue.get(position);

            String image_value = "http://graph.facebook.com/"+id+"/picture?type=normal";

            RoundedImageView imv1 = (RoundedImageView)rowView.findViewById(R.id.roundendImageView);

            String final_image_value="";

            new setPicture(image_value, final_image_value, imv1).execute();
            return rowView;

        }

    }
    class setPicture extends AsyncTask<String, String, String> {

        String image_value;
        String final_image_value;
        RoundedImageView imv1;

        public setPicture(String _image_value, String _final_image_value, RoundedImageView _imv1){
            super();

            this.image_value = _image_value;
            this.final_image_value = _final_image_value;
            this.imv1 = _imv1;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String... args) {
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

            return null;
        }
        protected void onPostExecute(String file_url) {
            if (final_image_value.trim().length() != 0) {
                Picasso.with(getActivity())
                        .load(final_image_value)
                        .placeholder(R.drawable.default_thumb)
                        .error(R.drawable.cancel)
                        .into(imv1);
            }
        }
    }

    public class AdapterClass  extends ArrayAdapter<String> {
        Context context;
        private String[] TextValue;
        private TypedArray ImageValue;

        public AdapterClass(Context context, String[] TextValue, TypedArray Image) {
            super(context, R.layout.nav_drawer_row, TextValue);
            this.context = context;
            this.TextValue= TextValue;
            this.ImageValue = Image;

        }

        @Override
        public View getView(int position, View coverView, ViewGroup parent) {
            // TODO Auto-generated method stub

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.nav_drawer_row,
                    parent, false);

            TextView text1 = (TextView)rowView.findViewById(R.id.textView1);
            text1.setText(TextValue[position]);

            int imageResource = ImageValue.getResourceId(position, -1);//getResources().getIdentifier(ImageValue[position], null, context.getPackageName());


            ImageView imv1 = (ImageView)rowView.findViewById(R.id.imageView1);
            Log.d("imv", "imv: " + imv1);
            //Drawable image = getResources().getDrawable(imageResource);
            imv1.setImageResource(imageResource);



            return rowView;

        }

    }
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);


    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    public void closeNavDrawer(){
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            //inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }


}