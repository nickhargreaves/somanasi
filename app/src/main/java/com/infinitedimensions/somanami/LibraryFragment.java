package com.infinitedimensions.somanami;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.infinitedimensions.somanami.helpers.RoundedImageView;
import com.infinitedimensions.somanami.models.Book;
import com.infinitedimensions.somanami.network.RequestBook;
import com.infinitedimensions.somanami.helpers.SimpleDBHandler;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;

public class LibraryFragment extends Fragment {

    private View rootView;
    private static final String ARG_SECTION_NUMBER = "section_number";

    List<Book> contentList;

    CardGridArrayAdapter mCardArrayAdapter;
    private int stackSize = 0;

    private ArrayList<Card> cards;
    CardGridView gridView;
    SimpleDBHandler dbHandler;
    private SharedPreferences pref;
    private String user_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        user_id = pref.getString("user_id", "0");

        rootView = inflater.inflate(R.layout.fragment_library, container, false);
        gridView = (CardGridView) rootView.findViewById(R.id.favoritesGrid);

        dbHandler = new SimpleDBHandler(getActivity().getApplicationContext(), null, null, 1);
        getBooks();
        return rootView;
    }

    public void getBooks(){

        cards = new ArrayList<Card>();
        contentList = new ArrayList<Book>();

        contentList = dbHandler.getBooks(null);

        displayBooks();

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
                getArguments().getInt(ARG_SECTION_NUMBER), "");
    }


    public void displayBooks(){
        stackSize = contentList.size();

        if(stackSize==0){

            Toast.makeText(getActivity().getApplicationContext(), "No items found!", Toast.LENGTH_SHORT).show();

        }

        for(int i = 0; i<contentList.size(); i++) {
            //get bookmark in list
            final Book content = contentList.get(i);

            //Create a Card
            Card card = new Card(getActivity().getApplicationContext());


            //Add thumbnail
            CustomThumbCard thumbnail = new CustomThumbCard(getActivity().getApplicationContext(), content.getThumb_url());

            thumbnail.setExternalUsage(true);
            //thumbnail.setUrlResource(content.getThumb_url());

            card.addCardThumbnail(thumbnail);

            //Listeners
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    //show book in dialog
                    //show book in dialog

                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    DialogFragment newFragment = singleBookDialogFragment.newInstance(content.getTitle(), content.getDescription(), content.getAuthors(), content.getCategories(), content.getThumb_url(), content.getOwner(), user_id, content.getOwnerName(), content.getId());

                    newFragment.show(ft, "dialog");
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

    public static class singleBookDialogFragment extends DialogFragment {
        String description;
        String authors;
        String categories;
        String thumbURL;
        String title;
        String owner;
        String user_id;
        String owner_name;
        String book_id;

        /**
         * Create a new instance of MyDialogFragment, providing "num"
         * as an argument.
         */
         static singleBookDialogFragment newInstance(String _title, String _description, String _authors, String _categories, String _thumbURL, String _owner, String _user_id, String _name, String _book_id) {
            singleBookDialogFragment f = new singleBookDialogFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putString("title", _title);
            args.putString("description", _description);
            args.putString("authors", _authors);
            args.putString("categories", _categories);
            args.putString("thumbURL", _thumbURL);
            args.putString("owner", _owner);
             args.putString("user_id", _user_id);
             args.putString("owner_name", _name);
             args.putString("book_id", _book_id);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            title = getArguments().getString("title");
            description = getArguments().getString("description");
            authors = getArguments().getString("authors");
            categories = getArguments().getString("categories");
            thumbURL = getArguments().getString("thumbURL");
            owner = getArguments().getString("owner");
            user_id = getArguments().getString("user_id");
            owner_name = getArguments().getString("owner_name");
            book_id = getArguments().getString("book_id");

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.single_book, container, false);

            getDialog().setTitle(title);

            TextView descTV = (TextView)v.findViewById(R.id.description);
            TextView authTV = (TextView)v.findViewById(R.id.authors);
            TextView catTV = (TextView)v.findViewById(R.id.categories);
            ImageView thumbIV = (ImageView)v.findViewById(R.id.thumbnail);
            ToggleButton statusTB = (ToggleButton)v.findViewById(R.id.toggleStatus);
            LinearLayout ownerInfo = (LinearLayout)v.findViewById(R.id.ownerInfoLayout);
            Button requestB = (Button)v.findViewById(R.id.requestB);

            //set toggle status

            if(owner.equals(user_id))
                {
                    statusTB.setEnabled(true);
                    //set status
                    //lend
                    //see who has it, if lent out


                }else{
                    statusTB.setEnabled(false);
                    
                    //show owner info
                    String[] ownerNames = owner_name.split(" ");
                    String ownerName = ownerNames[0];

                    setOwnerInformation(v, owner, getActivity(), ownerName);
                    ownerInfo.setVisibility(View.VISIBLE);
                    //ask to borrow
                    requestB.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){

                            new RequestBook(getActivity().getApplicationContext(),owner, user_id, book_id).execute();

                        }
                    });
            }

            descTV.setText(description);
            authTV.setText("By: " + authors);
            catTV.setText("Categorized under: " + categories);
            if (thumbURL.trim().length() != 0) {
                Picasso.with(getActivity())
                        .load(thumbURL)
                        .placeholder(R.drawable.default_thumb)
                        .error(R.drawable.cancel)
                        .into(thumbIV);
            }

            return v;
        }
    }

    public static void setOwnerInformation(View v, String id, Context ctx, String name){
        String image_value = "http://graph.facebook.com/"+id+"/picture?type=normal";

        RoundedImageView imv1 = (RoundedImageView)v.findViewById(R.id.ownerImage);
        TextView ownerName = (TextView)v.findViewById(R.id.ownerName);
        ownerName.setText(name);

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
        if (final_image_value.trim().length() != 0) {
            Picasso.with(ctx)
                    .load(final_image_value)
                    .placeholder(R.drawable.default_thumb)
                    .error(R.drawable.cancel)
                    .into(imv1);
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
                viewImage.getLayoutParams().height = (int)(150*metrics.density);
            }
        }
    }

}