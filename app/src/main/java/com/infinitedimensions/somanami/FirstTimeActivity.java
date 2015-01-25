package com.infinitedimensions.somanami;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import com.infinitedimensions.somanami.network.SyncAlarm;

/**
 * Created by nick on 1/25/15.
 */
public class FirstTimeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_time);

        //set not first time
        setNotFirstTime();
        //getBooks
        //getFriends
        //start alarm service
        startAlarmService();
        //redirect to home page
        

    }

    public void setNotFirstTime(){

        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();
        editor.putString("first_time", "0");
        editor.commit();

    }

    public void startAlarmService(){
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
    }
}
