package com.infinitedimensions.somanami.network;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.infinitedimensions.somanami.Defaults;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class RequestBook extends AsyncTask<Void, Void, String> {

    private Context ctx;
    private String owner;
    private String user_id;
    private String book_id;

    public RequestBook(Context ctx, String owner, String user_id, String book_id){
        this.ctx = ctx;
        this.owner = owner;
        this.user_id = user_id;
        this.book_id = book_id;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(Void... arg0) {
        URI url = null;
        try {
            url = new URI(Defaults.API_URL + "public/request_book/" + owner + "/" + user_id + "/" + book_id);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(url);
        try {
            httpclient.execute(request);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Toast.makeText(ctx, "Request sent!", Toast.LENGTH_SHORT).show();
    }
}