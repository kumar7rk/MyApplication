package com.example.rohit.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Rohit on 8/07/2016.
 */
public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            if(intent.getAction().equals(""/*ACTION_SERVICE*/))
            {
               new BackgroundService().setService(true, 60000*5);
            }
        }
}
