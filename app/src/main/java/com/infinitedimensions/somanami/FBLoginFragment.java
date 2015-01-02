package com.infinitedimensions.somanami;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.InputStream;
import java.util.Arrays;

public class FBLoginFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String url;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.facebook_login, container, false);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        editor = pref.edit();

        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("email", "user_friends"));
        //authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");

            Request.newMeRequest(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        editor.putString("user_id", user.getId());
                        editor.putString("firstname", user.getFirstName());
                        editor.putString("lastname", user.getLastName());
                        editor.putString("email", user.asMap().get("email").toString());
                        editor.commit();

                        url = Defaults.API_URL + "public/register/" + user.getId() + "/" + user.getFirstName() + "/" + user.getLastName() + "/" + user.asMap().get("email");

                        //if not registered, register here
                        String reg = pref.getString("reg", "0");
                        if(reg.equals("0")){
                            new perfomRegistration().execute();
                        }


                    }
                }
            }).executeAsync();

            Intent i = new Intent(getActivity(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(i);
            getActivity().finish();
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    class perfomRegistration extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        protected String doInBackground(String... args) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                Log.d("url", "url: " + url);
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