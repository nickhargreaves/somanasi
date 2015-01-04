package com.infinitedimensions.somanami.gcm;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.infinitedimensions.somanami.MainActivity;
import com.infinitedimensions.somanami.R;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;

/**
 * Created by nick on 1/4/15.
 */
public class NotificationsActivity extends ActionBarActivity {
    CardGridView gridView;
    SimpleDBHandler dbHandler;
    private ArrayList<Card> cards;
    CardGridArrayAdapter mCardArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifcations);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Your notifications");

        gridView = (CardGridView) findViewById(R.id.resultsGrid);
        dbHandler = new SimpleDBHandler(getApplicationContext(), null, null, 1);
        cards = new ArrayList<Card>();
        displayNotifications();

    }

    public void displayNotifications() {
        List<NotificationGCM> notificationGCMList = dbHandler.getNotifications();

        Log.d("totalstuff", "total: " + notificationGCMList.size());

        for (int i = 0; i < notificationGCMList.size(); i++) {
            //get bookmark in list
            final NotificationGCM content = notificationGCMList.get(i);

            //Create a Card
            Card card = new Card(getApplicationContext());

            //Create a CardHeader
            CustomHeaderInnerCard header = new CustomHeaderInnerCard(getApplicationContext(), content.getMesage(), content.getType());

            //Add Header to card
            card.addCardHeader(header);

            //Add thumbnail
            String imageSource = "http://graph.facebook.com/"+content.getUser()+"/picture";

            try
            {
                URL obj = new URL(imageSource);
                URLConnection conn = obj.openConnection();
                Map<String, List<String>> map = conn.getHeaderFields();

                imageSource = map.get("Location").toString();

                imageSource = imageSource.replace("[", "");

                imageSource = imageSource.replace("]", "");


            } catch (Exception e) {
                e.printStackTrace();
            }

            CustomThumbCard thumbnail = new CustomThumbCard(getApplicationContext(), imageSource);

            thumbnail.setExternalUsage(true);
            //thumbnail.setUrlResource(content.getThumb_url());

            card.addCardThumbnail(thumbnail);


            //Listeners
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    //Add to DB
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(NotificationsActivity.this);
                    builder1.setMessage("Are you sure?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
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

                if (imageSource.trim().length() != 0) {
                    Picasso.with(ctx)
                            .load(imageSource)
                            .placeholder(R.drawable.default_thumb)
                            .error(R.drawable.cancel)
                            .into((ImageView) viewImage);
                }


                DisplayMetrics metrics=parent.getResources().getDisplayMetrics();
                viewImage.getLayoutParams().width = (int)(50*metrics.density);
                viewImage.getLayoutParams().height = (int)(50*metrics.density);
            }
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

    public class CustomHeaderInnerCard extends CardHeader {

        private String desc;
        private String type;

        public CustomHeaderInnerCard(Context context, String _desc, String _type) {
            super(context, R.layout.card_inner_header);
            this.desc = _desc;
            this.type = _type;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            if (view!=null){
                TextView t1 = (TextView) view.findViewById(R.id.title);

                if (t1!=null)
                    t1.setText(desc);
                    t1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);


                TextView t2 = (TextView) view.findViewById(R.id.subtitle);
                if (t2!=null)
                    t2.setVisibility(View.GONE);
            }
        }
    }

}
