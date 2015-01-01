package com.infinitedimensions.somanami;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.InputStream;

import io.fabric.sdk.android.Fabric;
/**
 * Created by nick on 1/1/15.
 */
public class DigitsLogin extends ActionBarActivity {
    private TwitterLoginButton loginButton;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "Ni10yEOgeLBbAbgPTAEFdb9GM";
    private static final String TWITTER_SECRET = "hUL3at3svmu6NeHTZGL5YWe5iLKYrIBqFe4gFpVbnwkoGhcPR3";
    private String phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.facebook_login);

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String _phoneNumber) {
                // Do something with the session and phone number
                phoneNumber = _phoneNumber;

                SharedPreferences pref;
                pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                SharedPreferences.Editor editor = pref.edit();

                editor.putString("user_id", phoneNumber);
                editor.commit();
                new perfomRegistration().execute();
            }

            @Override
            public void failure(DigitsException exception) {
                // Do something on failure
                Toast.makeText(getApplicationContext(), "Sign in failed!", Toast.LENGTH_LONG).show();
            }
        });

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    class perfomRegistration extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        protected String doInBackground(String... args) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                String url = Defaults.API_URL + "public/register_number/" + phoneNumber;
                Log.d("url", url);
                HttpResponse response = httpClient
                        .execute(new HttpGet(url));

                InputStream is = response.getEntity().getContent();
                JsonFactory factory = new JsonFactory();

                JsonParser jsonParser = factory.createJsonParser(is);

                JsonToken token = jsonParser.nextToken();

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
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;

        }
        protected void onPostExecute(String file_url) {


        }
    }
}
