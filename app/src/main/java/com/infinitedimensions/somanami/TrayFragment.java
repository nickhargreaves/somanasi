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

import com.infinitedimensions.somanami.helpers.ConnectionDetector;
import com.infinitedimensions.somanami.helpers.GPSTracker;
import com.infinitedimensions.somanami.helpers.GifAnimationDrawable;
import com.infinitedimensions.somanami.helpers.RoundedImageView;
import com.infinitedimensions.somanami.helpers.SimpleDBHandler;
import com.infinitedimensions.somanami.models.TrayItem;
import com.infinitedimensions.somanami.network.ReturnBook;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    SimpleDBHandler dbHandler;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        user_id = prefs.getString("user_id", "0");

        rootView = inflater.inflate(R.layout.tray, container, false);
        gridView = (LinearLayout) rootView.findViewById(R.id.tray_fragment);
        notification = (TextView)rootView.findViewById(R.id.notification);

        loading_gif = (ImageView)rootView.findViewById(R.id.ivLoading);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        user_id = prefs.getString("user_id", "0");

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
        dbHandler = new SimpleDBHandler(getActivity().getApplicationContext(), null, null, 1);
        getTray(rootView);
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

    public void getTray(View rootview){

        cards = new ArrayList<Card>();
        contentList = new ArrayList<TrayItem>();

        contentList = dbHandler.getTrayItems();

        setUpTrayList(rootview);

    }
    private void setUpTrayList (View rootview)
    {
        gridView.setVisibility(View.VISIBLE);
        rlloading.setVisibility(View.GONE);


        //separate lent from borrowed
        List<TrayItem> lent = new ArrayList<TrayItem>();
        List<TrayItem> borrowed = new ArrayList<TrayItem>();

        for(int i = 0; i<contentList.size(); i++){
            TrayItem trayItem = contentList.get(i);
            if(trayItem.getBorrowed().equals("1")){
                borrowed.add(trayItem);
            }else{
                lent.add(trayItem);
            }
        }

        final float density = getResources().getDisplayMetrics().density;

        if(borrowed.size()==0){
            (rootview.findViewById(R.id.borrow_note)).setVisibility(View.VISIBLE);
            (rootview.findViewById(R.id.pager1)).setVisibility(View.GONE);
        }else{
            MyAdapter adapter = new MyAdapter(getActivity().getSupportFragmentManager(), borrowed);
            ViewPager pager = ((ViewPager)rootview.findViewById(R.id.pager1));

            pager.setId((int)(Math.random()*10000));
            pager.setOffscreenPageLimit(5);

            pager.setAdapter(adapter);

            //Bind the title indicator to the adapter
            CirclePageIndicator indicator = (CirclePageIndicator)rootview.findViewById(R.id.circles1);
            indicator.setViewPager(pager);
            indicator.setSnap(true);

            indicator.setRadius(5 * density);
            indicator.setFillColor(0xFFFF0000);
            indicator.setPageColor(0xFFaaaaaa);
            //indicator.setStrokeColor(0xFF000000);
            //indicator.setStrokeWidth(2 * density);
        }

        if(lent.size()==0){
            (rootview.findViewById(R.id.lent_note)).setVisibility(View.VISIBLE);
            (rootview.findViewById(R.id.pager2)).setVisibility(View.GONE);
        }else {

            MyAdapter adapter2 = new MyAdapter(getActivity().getSupportFragmentManager(), lent);
            ViewPager pager2 = ((ViewPager) rootView.findViewById(R.id.pager2));

            pager2.setId((int) (Math.random() * 10000));
            pager2.setOffscreenPageLimit(5);

            pager2.setAdapter(adapter2);

            //Bind the title indicator to the adapter
            CirclePageIndicator indicator2 = (CirclePageIndicator) rootview.findViewById(R.id.circles2);
            indicator2.setViewPager(pager2);
            indicator2.setSnap(true);

            indicator2.setRadius(5 * density);
            indicator2.setFillColor(0xFFFF0000);
            indicator2.setPageColor(0xFFaaaaaa);
            //indicator.setStrokeColor(0xFF000000);
            //indicator.setStrokeWidth(2 * density);
        }

    }

    public class MyAdapter extends FragmentPagerAdapter {

        List<TrayItem> trayItems;

        public MyAdapter(FragmentManager fm, List<TrayItem> _trayItems) {
            super(fm);
            trayItems = _trayItems;
        }

        @Override
        public int getCount() {
            return trayItems.size();
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("trayItem", new TrayItemsDataHelper(trayItems.get(position)));
            bundle.putString("user_id", user_id);
            Fragment f = new MyFragment();
            f.setArguments(bundle);

            return f;
        }
    }

    public static final class MyFragment extends Fragment {

        TrayItem trayItem;
        String _user_id;

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            TrayItemsDataHelper trd = (TrayItemsDataHelper)(getArguments().getSerializable("trayItem"));
            trayItem = trd.getTrayItem();
            _user_id = getArguments().getString("user_id");


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            ViewGroup root = (ViewGroup) inflater.inflate(R.layout.card_pager_textview, null);
            root.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {

                        //return or cancel
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setMessage("Mark as returned?");
                        builder1.setCancelable(true);
                        builder1.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        new ReturnBook(getActivity(), trayItem.getId(), _user_id, trayItem.getBorrowed()).execute();


                                        dialog.cancel();

                                    }
                                });
                        builder1.setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.cancel();

                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();


                }
            });

            ((TextView)root.findViewById(R.id.title)).setText(trayItem.getBook_title());

            ((TextView)root.findViewById(R.id.description)).setText("Due: " + trayItem.getDate_due());

            //set book thumb
            ImageView bookThumb = (ImageView)root.findViewById(R.id.bookThumb);

            String thumbURL = trayItem.getBook_thumb();

            if (thumbURL.trim().length() != 0) {
                Picasso.with(getActivity())
                        .load(thumbURL)
                        .placeholder(R.drawable.default_thumb)
                        .error(R.drawable.cancel)
                        .into(bookThumb);
            }
            //set user thumb
            RoundedImageView userThumb = (RoundedImageView)root.findViewById(R.id.userThumb);
            userThumb.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //open user's book list
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, FriendBooksFragment.newInstance(8, trayItem.getPerson_id(), trayItem.getPerson_name()))
                            .commit();
                }
            });

            String userThumbUrl = "http://graph.facebook.com/"+trayItem.getPerson_id()+"/picture";
            String final_image_value="";

            try
            {
                URL obj = new URL(userThumbUrl);
                URLConnection conn = obj.openConnection();
                Map<String, List<String>> map = conn.getHeaderFields();

                final_image_value = map.get("Location").toString();

                final_image_value = final_image_value.replace("[", "");

                final_image_value = final_image_value.replace("]", "");


            } catch (Exception e) {
                e.printStackTrace();
            }
            if (final_image_value.trim().length() != 0) {
                Picasso.with(getActivity())
                        .load(final_image_value)
                        .placeholder(R.drawable.default_thumb)
                        .error(R.drawable.cancel)
                        .into(userThumb);
            }

            return root;
        }



    }

    public static class TrayItemsDataHelper implements Serializable {

        private TrayItem trayItem;

        public TrayItemsDataHelper(TrayItem _trayItem) {
            this.trayItem = _trayItem;
        }

        public TrayItem getTrayItem() {
            return this.trayItem;
        }
    }

}