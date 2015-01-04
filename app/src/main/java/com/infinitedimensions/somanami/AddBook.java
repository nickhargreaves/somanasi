package com.infinitedimensions.somanami;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;

/**
 * Created by nick on 12/23/14.
 */
public class AddBook extends ActionBarActivity {

    String query;
    List<Book> contentList;

    CardGridArrayAdapter mCardArrayAdapter;
    private int stackSize = 0;

    private ImageView loading_gif;
    private GifAnimationDrawable little;

    private RelativeLayout rlloading;

    private GPSTracker gpsT;
    private double latitude;
    private double longitude;
    private String location="(0, 0)";

    private String type = "0";

    private ConnectionDetector cd;
    private Boolean isInternetPresent;

    private TextView notification;

    private String last_item = "0";
    private ArrayList<Card> cards;
    SharedPreferences prefs;

    SharedPreferences.Editor editor;
    CardGridView gridView;

    String user_id = "0";

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_book);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Add Book");

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        user_id = pref.getString("user_id", "0");

        query = getIntent().getStringExtra("query");

        gridView = (CardGridView)findViewById(R.id.resultsGrid);
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
        notification.setText("Looking for matching books...");

        gridView.setVisibility(View.GONE);
        rlloading.setVisibility(View.VISIBLE);
        //check if has network
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        //get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        if(!isInternetPresent){

            Toast.makeText(getApplicationContext(), "Check your internet settings!", Toast.LENGTH_LONG).show();
            //TODO: show cache


            //TODO:timer to check settings
            notification.setText("Tap to retry!");

        }else{
            //check location
            gpsT = new GPSTracker(getApplicationContext());

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
    class AddThisBook extends AsyncTask<String, String, String> {

        private String id;
        private ProgressDialog pDialog;

        private AddThisBook(String id){
            this.id = id;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AddBook.this);
            pDialog.setMessage("Adding book ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected String doInBackground(String... strings) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                String url = Defaults.API_URL + "public/addbook/"+ URLEncoder.encode(id) + "/" + URLEncoder.encode(user_id);// +"/" + location;

                Log.d("url", "url: " + url);

                HttpResponse response = httpClient
                        .execute(new HttpGet(url));

                InputStream is = response.getEntity().getContent();
                JsonFactory factory = new JsonFactory();

                JsonParser jsonParser = factory.createJsonParser(is);

                JsonToken token = jsonParser.nextToken();

                // Expected JSON is an array so if current token is "[" then while
                // we don't get
                // "]" we will keep parsing
                //if (token == JsonToken.START_ARRAY) {
                   // while (token != JsonToken.END_ARRAY) {
                        // Inside array there are many objects, so it has to start
                        // with "{" and end with "}"
                        token = jsonParser.nextToken();
                        //if (token == JsonToken.FIELD_NAME) {


                            while (token != JsonToken.END_OBJECT) {
                                // Each object has a name which we will use to
                                // identify the type.
                                token = jsonParser.nextToken();
                                if (token == JsonToken.FIELD_NAME) {
                                    String objectName = jsonParser.getCurrentName();
                                    // jsonParser.nextToken();
                                    if (0 == objectName.compareToIgnoreCase("status")) {
                                        //fuck it I'll come back to this
                                    }else{

                                    }
                                }
                            }
                       // }
                   // }

                //}
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String file) {
            Toast.makeText(getApplicationContext(), "Added successfully!", Toast.LENGTH_LONG).show();
            pDialog.dismiss();


            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i);

            finish();
        }

    }
    @Override
    public void onBackPressed()
    {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
        finish();
    }
    class getContent extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            cards = new ArrayList<Card>();
            contentList = new ArrayList<Book>();

        }
        protected String doInBackground(String... args) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                String url = Defaults.API_URL + "public/search/"+ URLEncoder.encode(query);// +"/" + location;
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

            return null;

        }
        protected void onPostExecute(String file_url) {

            stackSize = contentList.size();

            if(stackSize==0){

                Toast.makeText(getApplicationContext(), "No items found!", Toast.LENGTH_SHORT).show();

            }else{

                setTitle("Click on a book to add");

            }

            for(int i = 0; i<contentList.size(); i++) {
                //get bookmark in list
                final Book content = contentList.get(i);

                //Create a Card
                Card card = new Card(getApplicationContext());

                //Create a CardHeader
                CustomHeaderInnerCard header = new CustomHeaderInnerCard(getApplicationContext(), content.getTitle(), content.getAuthors());

                //
                String str = content.getTitle();

                if (str.length() > 50)
                    str = str.substring(0, 50) + "...";

                header.setTitle(str);

                //Add Header to card
                card.addCardHeader(header);

                //Add thumbnail
                CustomThumbCard thumbnail = new CustomThumbCard(getApplicationContext(), content.getThumb_url());

                thumbnail.setExternalUsage(true);
                //thumbnail.setUrlResource(content.getThumb_url());

                card.addCardThumbnail(thumbnail);



                //Listeners
                card.setOnClickListener(new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        //Add to DB
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(AddBook.this);
                        builder1.setMessage("Are you sure?");
                        builder1.setCancelable(true);
                        builder1.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        new AddThisBook(content.getId()).execute();
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

                cards.add(card);
            }
            //array adapter
            mCardArrayAdapter = new CardGridArrayAdapter(getApplicationContext(),cards);

            if (gridView!=null){
                gridView.setAdapter(mCardArrayAdapter);
            }

            rlloading.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);

        }
    }


    public class CustomThumbCard extends CardThumbnail {
        private String imageSource;
        private Context ctx;

        public CustomThumbCard(Context context, String _imageSource) {
            super(context);
            this.ctx = context;
            this.imageSource = _imageSource;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {
            if (viewImage!=null){
                //viewImage.getLayoutParams().width=250;
                //viewImage.getLayoutParams().height=250;
                //Picasso.with(ctx).setDebugging(true);
                if (imageSource.trim().length() != 0) {
                    Picasso.with(ctx)
                            .load(imageSource)
                            .placeholder(R.drawable.default_thumb)
                            .error(R.drawable.cancel)
                            .into((ImageView) viewImage);
                }


                DisplayMetrics metrics=parent.getResources().getDisplayMetrics();
                viewImage.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;//(int)(250*metrics.density);
                viewImage.getLayoutParams().height = (int)(100*metrics.density);
            }
        }
    }


    public class CustomHeaderInnerCard extends CardHeader {

        private String title;
        private String authors;

        public CustomHeaderInnerCard(Context context, String _title, String _authors) {
            super(context, R.layout.card_inner_header);
            this.title = _title;
            this.authors = _authors;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            if (view!=null){
                TextView t1 = (TextView) view.findViewById(R.id.title);
                if (t1!=null)
                    t1.setText(title);

                TextView t2 = (TextView) view.findViewById(R.id.subtitle);
                if (t2!=null)
                    t2.setText("By: " + authors);
            }
        }
    }
}
