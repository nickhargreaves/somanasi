package com.infinitedimensions.somanami;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitedimensions.somanami.gcm.NotificationGCM;
import com.infinitedimensions.somanami.gcm.SimpleDBHandler;
import com.squareup.picasso.Picasso;

import java.io.IOException;
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

public class NotificationsFragment extends Fragment {

    private View rootView;
    private static final String ARG_SECTION_NUMBER = "section_number";

    CardGridView gridView;
    SimpleDBHandler dbHandler;
    private ArrayList<Card> cards;
    CardGridArrayAdapter mCardArrayAdapter;
    List<NotificationGCM> notificationGCMList;
    private ImageView loading_gif;
    private GifAnimationDrawable little;

    private RelativeLayout rlloading;
    private TextView notification;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_notifcations, container, false);
        gridView = (CardGridView) rootView.findViewById(R.id.resultsGrid);
        notification = (TextView)rootView.findViewById(R.id.notification);

        loading_gif = (ImageView)rootView.findViewById(R.id.ivLoading);

        rlloading = (RelativeLayout)rootView.findViewById(R.id.rlLoading);

        // and add the GifAnimationDrawable
        try{

            little = new GifAnimationDrawable(getResources().openRawResource(R.raw.loading_anim));
            little.setOneShot(false);
            loading_gif.setImageDrawable(little);
        }catch(IOException ioe){

        }
        dbHandler = new SimpleDBHandler(getActivity().getApplicationContext(), null, null, 1);
        cards = new ArrayList<Card>();

        notification.setText("");
        new GetNotifications().execute();
        return rootView;
    }


    public static NotificationsFragment newInstance(int sectionNumber) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public NotificationsFragment() {
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER), "");
    }

    class GetNotifications extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            gridView.setVisibility(View.GONE);
            rlloading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            notificationGCMList = dbHandler.getNotifications();

            return null;
        }
        @Override
        protected void onPostExecute(String file_url){
            displayNotifications();
            rlloading.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    public void displayNotifications() {


        Log.d("totalstuff", "total: " + notificationGCMList.size());

        for (int i = 0; i < notificationGCMList.size(); i++) {
            //get bookmark in list
            final NotificationGCM content = notificationGCMList.get(i);

            //Create a Card
            Card card = new Card(getActivity().getApplicationContext());

            //Create a CardHeader
            CustomHeaderInnerCard header = new CustomHeaderInnerCard(getActivity().getApplicationContext(), content.getMesage(), content.getType());

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

            CustomThumbCard thumbnail = new CustomThumbCard(getActivity().getApplicationContext(), imageSource);

            thumbnail.setExternalUsage(true);
            //thumbnail.setUrlResource(content.getThumb_url());

            card.addCardThumbnail(thumbnail);


            //Listeners
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    //Add to DB
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setMessage("Give user book?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    builder1.setNegativeButton("Ignore",
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
        mCardArrayAdapter = new CardGridArrayAdapter(getActivity().getApplicationContext(),cards);

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
                            .into((RoundedImageView) viewImage);
                }


                DisplayMetrics metrics=parent.getResources().getDisplayMetrics();
                viewImage.getLayoutParams().width = (int)(50*metrics.density);
                viewImage.getLayoutParams().height = (int)(50*metrics.density);
            }
        }
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