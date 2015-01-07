package com.infinitedimensions.somanami;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

    ListView gridView;
    SimpleDBHandler dbHandler;
    private ArrayList<Card> cards;
    CardGridArrayAdapter mCardArrayAdapter;
    List<NotificationGCM> notificationGCMList;

    private GifAnimationDrawable little;

    private SharedPreferences pref;
    private String user_id;
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";

    private MenuItem friendIcon = null;
    private Drawable friendDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        user_id = pref.getString("user_id", "0");

        rootView = inflater.inflate(R.layout.fragment_messages, container, false);
        gridView = (ListView) rootView.findViewById(R.id.messagesList);

        dbHandler = new SimpleDBHandler(getActivity().getApplicationContext(), null, null, 1);

        notificationGCMList = dbHandler.getNotifications();


        final AdapterClass2 adClass = new AdapterClass2(getActivity(), notificationGCMList);
        gridView.setAdapter(adClass);



        return rootView;
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


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER), "");
    }


    public class AdapterClass2  extends ArrayAdapter<NotificationGCM> {
        Context context;
        private List<NotificationGCM> TextValue;

        public AdapterClass2(Context context, List<NotificationGCM> TextValue) {
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

            holder.message.setText(notificationGCMList.get(position).getMesage());

            LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
            //check if it is a status message then remove background, and change text color.

            //Check whether message is mine to show green background and align to right
            if(notificationGCMList.get(position).getType().equals("0"))
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