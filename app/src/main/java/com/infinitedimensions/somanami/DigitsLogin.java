package com.infinitedimensions.somanami;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;
/**
 * Created by nick on 1/1/15.
 */
public class DigitsLogin extends ActionBarActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "Ni10yEOgeLBbAbgPTAEFdb9GM";
    private static final String TWITTER_SECRET = "hUL3at3svmu6NeHTZGL5YWe5iLKYrIBqFe4gFpVbnwkoGhcPR3";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.facebook_login);

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // Do something with the session and phone number
            }

            @Override
            public void failure(DigitsException exception) {
                // Do something on failure
            }
        });

    }
}
