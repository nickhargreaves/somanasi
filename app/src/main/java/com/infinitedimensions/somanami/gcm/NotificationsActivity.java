package com.infinitedimensions.somanami.gcm;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.infinitedimensions.somanami.R;

/**
 * Created by nick on 1/4/15.
 */
public class NotificationsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_book);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Your notifications");
    }
}
