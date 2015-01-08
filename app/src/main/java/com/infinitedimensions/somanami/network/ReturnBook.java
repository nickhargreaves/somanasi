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


public class ReturnBook extends AsyncTask<Void, Void, String> {

    private Context ctx;
    private String tray_item;
    private String user_id;
    private String borrowed;

    public ReturnBook(Context _ctx, String _tray_item, String _user_id, String _borrowed){
        this.ctx = _ctx;
        this.tray_item = _tray_item;
        this.user_id = _user_id;
        this.borrowed = _borrowed;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(Void... arg0) {
        URI url = null;
        try {
            String urlstring =Defaults.API_URL + "public/return_book/" + tray_item + "/" + user_id + "/" + borrowed;
            url = new URI(urlstring);
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
        Toast.makeText(ctx, "Sent!", Toast.LENGTH_SHORT).show();
    }
}