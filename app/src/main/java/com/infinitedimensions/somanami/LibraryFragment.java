package com.infinitedimensions.somanami;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;

public class LibraryFragment extends Fragment {

    private View rootView;
    private static final String ARG_SECTION_NUMBER = "section_number";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_library, container, false);

        return refresh_view();
    }
    public static LibraryFragment newInstance(int sectionNumber) {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LibraryFragment() {
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    public View refresh_view(){


        ArrayList<Card> cards = new ArrayList<Card>();
        for(int i = 0; i<15; i++) {

            //Create a Card
            Card card = new Card(getActivity().getApplicationContext());



            //Add thumbnail
            CustomThumbCard thumbnail = new CustomThumbCard(getActivity().getApplicationContext(), "http://bookriotcom.c.presscdn.com/wp-content/uploads/2013/06/war-of-the-worlds-cover-by-kjell-roger-ringstad-686x1024.jpg");

            thumbnail.setExternalUsage(true);
            //thumbnail.setUrlResource(content.getThumb_url());

            card.addCardThumbnail(thumbnail);



            //Listeners
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    //View card
                }
            });

            cards.add(card);
        }
        //array adapter
        CardGridArrayAdapter mCardArrayAdapter = new CardGridArrayAdapter(getActivity().getApplicationContext(),cards);

        CardGridView gridView = (CardGridView) rootView.findViewById(R.id.favoritesGrid);
        if (gridView!=null){
            gridView.setAdapter(mCardArrayAdapter);
        }


        return rootView;

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
                Log.d("ctx", "ctx: " + ctx);
                //Picasso.with(ctx).setDebugging(true);
                Picasso.with(ctx)
                        .load(imageSource)
                        .placeholder(R.drawable.default_thumb)
                        .error(R.drawable.cancel)
                        .into((ImageView) viewImage);

                DisplayMetrics metrics=parent.getResources().getDisplayMetrics();
                viewImage.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;//(int)(250*metrics.density);
                viewImage.getLayoutParams().height = (int)(100*metrics.density);
            }
        }
    }

}