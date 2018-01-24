package com.example.rohit.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.rohit.myapplication.R;
import com.example.rohit.myapplication.services.BackgroundService;


public class NewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        startService(new Intent(this,BackgroundService.class));
    }

}
