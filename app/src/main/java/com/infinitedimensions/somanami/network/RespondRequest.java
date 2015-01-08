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


public class RespondRequest extends AsyncTask<Void, Void, String> {

    private Context ctx;
    private String borrower;
    private String user_id;
    private String book_id;
    private String date;

    public RespondRequest(Context _ctx, String _borrower, String _user_id, String _book_id, String _date){
        this.ctx = _ctx;
        this.borrower = _borrower;
        this.user_id = _user_id;
        this.book_id = _book_id;
        this.date = _date;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(Void... arg0) {
        URI url = null;
        try {
            String urlstring =Defaults.API_URL + "public/respond_request/" + borrower + "/" + user_id + "/" + book_id + "/" + date;
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
        Toast.makeText(ctx, "Response sent!", Toast.LENGTH_SHORT).show();
    }
}