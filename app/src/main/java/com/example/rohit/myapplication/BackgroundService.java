package com.example.rohit.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Rohit on 4/05/2016.
 */
public class BackgroundService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //setService(true,0);
        startService(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    void setService(boolean set, int timeout)
    {
        Intent intent;
        PendingIntent alarmIntent;
        intent = new Intent(); // forms and creates appropriate Intent and pass it to AlarmManager
//        intent.setAction(ACTION_SERVICE);
        intent.setClass(this, BackgroundService.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if(set)
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout, alarmIntent);
        else
            am.cancel(alarmIntent);
    }
}
