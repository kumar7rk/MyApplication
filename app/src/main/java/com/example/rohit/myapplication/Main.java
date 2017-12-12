package com.example.rohit.myapplication;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Rohit on 9/08/2016.
 */
public class Main {

    Context mContext;

    public Main(Context mContext) {
        this.mContext = mContext;
    }

    public static void showToast(String text) {
        Toast.makeText(MyApplication.getAppContext(), text, Toast.LENGTH_LONG).show();
    }

}