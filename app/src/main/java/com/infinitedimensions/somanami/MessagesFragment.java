package com.infinitedimensions.somanami;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitedimensions.somanami.gcm.NotificationGCM;
import com.infinitedimensions.somanami.gcm.SimpleDBHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;

public class MessagesFragment extends Fragment {

    private View rootView;
    private static final String ARG_SECTION_NUMBER = "section_number";

    List<Book> contentList;

    CardGridArrayAdapter mCardArrayAdapter;
    private int stackSize = 0;


    private GPSTracker gpsT;
    private double latitude;
    private double longitude;
    private String location="(0, 0)";


    private ConnectionDetector cd;
    private Boolean isInternetPresent;

    private String last_item = "0";
    private ArrayList<Card> cards;
    SharedPreferences prefs;

    SharedPreferences.Editor editor;
    ListView gridView;

    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";


    private int mStackLevel = 0;

    private SimpleDBHandler dbHandler;

    private List<NotificationGCM> messagesList;

    private MenuItem friendIcon = null;

    private Drawable friendDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {
        dbHandler = new SimpleDBHandler(getActivity().getApplicationContext(), null, null, 1);


        rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        gridView = (ListView)rootView.findViewById(R.id.listView1);


        checkConditions();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.friend, menu);

        friendIcon = menu.getItem(0);

        new setIcon().execute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_message) {

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, FriendBooksFragment.newInstance(8, getArguments().getString(USER_ID), getArguments().getString(USER_NAME)))
                    .commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class setIcon extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
        protected String doInBackground(String... args) {

            String fid = getArguments().getString(USER_ID);
            String image_value = "http://graph.facebook.com/"+fid+"/picture?type=normal";

            Log.d("im", "im: " + image_value);

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

            try {
                URL url = new URL(final_image_value);
                InputStream is = url.openStream();
                friendDrawable = Drawable.createFromStream(is, "src");

            } catch (MalformedURLException e) {
                // e.printStackTrace();
            } catch (IOException e) {
                // e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute(String file_url) {

            if(friendIcon!=null){
                friendIcon.setIcon(friendDrawable);
            }

        }
    }



    public void checkConditions(){
        //check if has network
        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity().getApplicationContext());

        //get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        if(!isInternetPresent){

            Toast.makeText(getActivity().getApplicationContext(), "Check your internet settings!", Toast.LENGTH_LONG).show();
            //TODO: show cache


            //TODO:timer to check settings

        }else{
            //check location
            gpsT = new GPSTracker(getActivity().getApplicationContext());

            // check if GPS enabled
            if(gpsT.canGetLocation()){

                latitude = gpsT.getLatitude();
                longitude = gpsT.getLongitude();

                location = "("+latitude+", "+longitude+")";

                //new Thread(this).run();
                new getContent().execute();
            }else{

                //gpsT.showSettingsAlert();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                // Create and show the dialog.
                allowLocationDialog newFragment = new allowLocationDialog ();


                newFragment.show(ft, "dialog");

                //TODO:timer to check settings

            }
        }

    }


    public static class allowLocationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("GPS Settings")
                    .setMessage("Do you want to go to settings menu? You also need a view of the sky for it work properly.")
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // cancel

                        }
                    })
                    .setPositiveButton(android.R.string.yes,  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getActivity().getApplicationContext().startActivity(intent);
                        }
                    })
                    .create();
        }
    }

    public static MessagesFragment newInstance(int sectionNumber, String _friend_id, String _friend_name) {
        MessagesFragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(USER_ID, _friend_id);
        args.putString(USER_NAME, _friend_name);

        fragment.setHasOptionsMenu(true);

        fragment.setArguments(args);
        return fragment;
    }

    public MessagesFragment() {
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER), "");
    }


    class getContent extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
        protected String doInBackground(String... args) {
            messagesList = dbHandler.getNotifications();

            return null;
        }
        protected void onPostExecute(String file_url) {

            List<String> friend_names = new ArrayList<String>();

            for(int i =0; i<messagesList.size(); i++){
                friend_names.add((messagesList.get(i)).getMesage());
            }

            AdapterClass2 adClass = new AdapterClass2(getActivity(), friend_names);
            gridView.setAdapter(adClass);;

        }
    }

    public class AdapterClass2  extends ArrayAdapter<String> {
        Context context;
        private List<String> TextValue;

        public AdapterClass2(Context context, List<String> TextValue) {
            super(context, R.layout.drawer_list_footer_row, TextValue);
            this.context = context;
            this.TextValue= TextValue;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            ViewHolder holder;
            if(convertView == null)
            {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.messages_row, parent, false);
                holder.message = (TextView) convertView.findViewById(R.id.message_text);
                convertView.setTag(holder);
            }
            else
                holder = (ViewHolder) convertView.getTag();

            holder.message.setText(messagesList.get(position).getMesage());

            LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
            //check if it is a status message then remove background, and change text color.

                //Check whether message is mine to show green background and align to right
                if(messagesList.get(position).getType().equals("0"))
                {
                    holder.message.setBackgroundResource(R.drawable.bubble_a);
                    lp.gravity = Gravity.RIGHT;
                }
                //If not mine then it is from sender to show orange background and align to left
                else
                {
                    holder.message.setBackgroundResource(R.drawable.bubble_b);
                    lp.gravity = Gravity.LEFT;
                }

                holder.message.setLayoutParams(lp);
                //holder.message.setTextColor(getResources().getColor(R.color.counter_text_color));

            return convertView;

        }
        private class ViewHolder
        {
            TextView message;
        }

        @Override
        public long getItemId(int position) {
            //Unimplemented, because we aren't using Sqlite.
            return position;
        }

    }


}