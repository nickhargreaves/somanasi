package com.infinitedimensions.somanami;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;

public class TrayFragment extends Fragment {

    private View rootView;
    private static final String ARG_SECTION_NUMBER = "section_number";

    List<TrayItem> contentList;

    CardGridArrayAdapter mCardArrayAdapter;
    private int stackSize = 0;

    private ImageView loading_gif;
    private GifAnimationDrawable little;

    private RelativeLayout rlloading;

    private GPSTracker gpsT;
    private double latitude;
    private double longitude;
    private String location="(0, 0)";


    private ConnectionDetector cd;
    private Boolean isInternetPresent;

    private TextView notification;

    private String last_item = "0";
    private ArrayList<Card> cards;
    SharedPreferences prefs;

    SharedPreferences.Editor editor;
    LinearLayout gridView;

    String user_id = "0";

    private int mStackLevel = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        user_id = prefs.getString("user_id", "0");

        rootView = inflater.inflate(R.layout.tray, container, false);
        gridView = (LinearLayout) rootView.findViewById(R.id.tray_fragment);
        notification = (TextView)rootView.findViewById(R.id.notification);

        loading_gif = (ImageView)rootView.findViewById(R.id.ivLoading);

        rlloading = (RelativeLayout)rootView.findViewById(R.id.rlLoading);
        rlloading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConditions(rootView);
            }
        });
        // and add the GifAnimationDrawable
        try{

            little = new GifAnimationDrawable(getResources().openRawResource(R.raw.loading_anim));
            little.setOneShot(false);
            loading_gif.setImageDrawable(little);
        }catch(IOException ioe){

        }

        checkConditions(rootView);
        return rootView;
    }

    public void checkConditions(View rootView){
        notification.setText("Getting tray items...");

        gridView.setVisibility(View.GONE);
        rlloading.setVisibility(View.VISIBLE);
        //check if has network
        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity().getApplicationContext());

        //get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        if(!isInternetPresent){

            Toast.makeText(getActivity().getApplicationContext(), "Check your internet settings!", Toast.LENGTH_LONG).show();
            //TODO: show cache


            //TODO:timer to check settings
            notification.setText("Tap to retry!");

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
                notification.setText("Tap to retry!");

            }
        }


    }

    class getContent extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            contentList = new ArrayList<TrayItem>();

        }

        protected String doInBackground(String... args) {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                String url = Defaults.API_URL + "public/mybooks/" + user_id;
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
                                    contentList.add(trayItem);

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
            setUpTrayList(rootView);
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

    public static TrayFragment newInstance(int sectionNumber) {
        TrayFragment fragment = new TrayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TrayFragment() {
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER), "");
    }


    private void setUpTrayList (View rootview)
    {
        gridView.setVisibility(View.VISIBLE);
        rlloading.setVisibility(View.GONE);

        int[] titles1 =
                {(R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name)
                };
        int[] messages1 =
                {(R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name)
                };




        MyAdapter adapter = new MyAdapter(getActivity().getSupportFragmentManager(), titles1,messages1);
        ViewPager pager = ((ViewPager)rootview.findViewById(R.id.pager1));

        pager.setId((int)(Math.random()*10000));
        pager.setOffscreenPageLimit(5);

        pager.setAdapter(adapter);

        //Bind the title indicator to the adapter
        CirclePageIndicator indicator = (CirclePageIndicator)rootview.findViewById(R.id.circles1);
        indicator.setViewPager(pager);
        indicator.setSnap(true);


        final float density = getResources().getDisplayMetrics().density;

        indicator.setRadius(5 * density);
        indicator.setFillColor(0xFFFF0000);
        indicator.setPageColor(0xFFaaaaaa);
        //indicator.setStrokeColor(0xFF000000);
        //indicator.setStrokeWidth(2 * density);


        int[] titles2 =
                {(R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name)
                };
        int[] messages2 =
                {(R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name),
                        (R.string.app_name)
                };

        MyAdapter adapter2 = new MyAdapter(getActivity().getSupportFragmentManager(), titles2,messages2);
        ViewPager pager2 = ((ViewPager)rootView.findViewById(R.id.pager2));

        pager2.setId((int)(Math.random()*10000));
        pager2.setOffscreenPageLimit(5);

        pager2.setAdapter(adapter2);

        //Bind the title indicator to the adapter
        CirclePageIndicator indicator2 = (CirclePageIndicator)rootview.findViewById(R.id.circles2);
        indicator2.setViewPager(pager2);
        indicator2.setSnap(true);

        indicator2.setRadius(5 * density);
        indicator2.setFillColor(0xFFFF0000);
        indicator2.setPageColor(0xFFaaaaaa);
        //indicator.setStrokeColor(0xFF000000);
        //indicator.setStrokeWidth(2 * density);

    }

    public class MyAdapter extends FragmentPagerAdapter {

        int[] mMessages;
        int[] mTitles;

        public MyAdapter(FragmentManager fm, int[] titles, int[] messages) {
            super(fm);
            mTitles = titles;
            mMessages = messages;
        }

        @Override
        public int getCount() {
            return mMessages.length;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString("title",getString(mTitles[position]));
            bundle.putString("msg", getString(mMessages[position]));

            Fragment f = new MyFragment();
            f.setArguments(bundle);

            return f;
        }
    }

    public static final class MyFragment extends Fragment {

        String mMessage;
        String mTitle;

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mTitle = getArguments().getString("title");
            mMessage = getArguments().getString("msg");
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            ViewGroup root = (ViewGroup) inflater.inflate(R.layout.card_pager_textview, null);

            ((TextView)root.findViewById(R.id.title)).setText(mTitle);

            ((TextView)root.findViewById(R.id.description)).setText(mMessage);

            return root;
        }

    }

}