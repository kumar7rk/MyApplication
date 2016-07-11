package com.example.rohit.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {
    static Activity thisActivity = null;
    SensorManager sensorManager;
    Sensor photometer;
    TextView textView,TV1,TV2;
    SensorEvent event;
    AlertDialog dialog;
    SharedPreferences preferences;
    int max = 0;
    boolean isAppRunning = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;

        textView = (TextView)findViewById(R.id.textView1);
        TV1 = (TextView) findViewById(R.id.tv1);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        photometer = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, photometer, SensorManager.SENSOR_DELAY_NORMAL);

        isConnected(thisActivity);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Using phone in darkness")
                .setTitle("Warning");
        dialog = builder.create();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        max = preferences.getInt("highest",0);
        TV1.setText(max + "");


        Intent serviceIntent = new Intent(MainActivity.this,BackgroundService.class);
        startService(serviceIntent);

        /*Intent i4=new Intent(Intent.ACTION_MAIN);

        PackageManager manager = getPackageManager();

        i4 = manager.getLaunchIntentForPackage("jp.ne.hardyinfinity.bluelightfilter.free");

     //   i4.addCategory(Intent.CATEGORY_LAUNCHER);

        startActivity(i4);
*/


/*
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("jp.ne.hardyinfinity.bluelightfilter.free.service", "jp.ne.hardyinfinity.bluelightfilter.free.service.FilterService"));
        ComponentName c = getApplicationContext().startService(intent);
        startService(intent);
*/
    }
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("jp.ne.hardyinfinity.bluelightfilter.free.service.FilterService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC)
        Toast.makeText(thisActivity,"Phone is charging",Toast.LENGTH_SHORT).show();
        if(plugged == BatteryManager.BATTERY_PLUGGED_USB)
            Toast.makeText(thisActivity,"Phone is charging via USB",Toast.LENGTH_SHORT).show();

        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isAppRunning = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppRunning = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("highest", max);
        editor.commit();
        isAppRunning = false;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int intValue = (int) event.values[0];
        textView.setTextSize(60);
        textView.setText(intValue + "");
            final Toast toast = Toast.makeText(thisActivity, "Please Turn on BlueLightFilter", Toast.LENGTH_SHORT);
        if(intValue ==0 /*&& !dialog.isShowing()*/) {// using dialog not showing shows and hides the dialog as long as the first condition is true
            if (isAppRunning) dialog.show();
            if(!isServiceRunning()/*&&toast.getView().getWindowVisibility() != View.VISIBLE*//* && toast.getView().isShown() == false*/){
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 500);
            }
        }
        else
        {
            dialog.dismiss();
        }

        TV1.setText(max + "");
        max = max<intValue ?intValue: max;
        /*if (intValue < 100) {
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, 200);
        }*/

        }
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}