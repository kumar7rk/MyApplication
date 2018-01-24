package com.example.rohit.myapplication.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rohit.myapplication.R;
import com.example.rohit.myapplication.services.BackgroundService;

import java.text.DateFormat;
import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class MainActivity extends Activity implements SensorEventListener {
    static Activity thisActivity = null;

    SensorManager mSensorManager;
    Sensor mPhotometer,mAccelerometer,mMagnetometer;
    SensorEvent event;

    TextView currentLux, maxLux;
    TextView accelerometerX,accelerometerY,accelerometerZ;
    TextView rotationX,rotationY,rotationZ;
    Switch serviceSwitch;
    AlertDialog dialog;

    SharedPreferences preferences;
    int max = 0;
    boolean isAppRunning = true;
    KeyguardManager keyguardManager;

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity = this;
        findViews();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String lastOpen = "Today";
        lastOpen = preferences.getString("lastOpen",lastOpen);
        Crouton.makeText(thisActivity, "App last open on: "+lastOpen, Style.CONFIRM).show();

        keyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mPhotometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAccelerometer= mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer= mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mPhotometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        isPowerConnected(thisActivity);

        boolean switchOnOff = preferences.getBoolean("switch", true);
        if (switchOnOff){
            serviceSwitch.setChecked(true);
            serviceSwitch.setText("ServiceRunning");
            showSnackBar("ServiceRunning");

        }
        if (!switchOnOff){
            serviceSwitch.setChecked(false);
            serviceSwitch.setText("ServiceStopped");
            showSnackBar("ServiceStopped");
        }
        startService();

        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    serviceSwitch.setText("ServiceRunning");
                    showSnackBar(serviceSwitch.getText().toString());
                    startService();
                } else {
                    serviceSwitch.setText("ServiceStopped");
                    showSnackBar(serviceSwitch.getText().toString());
                    Intent serviceIntent = new Intent(MainActivity.this, BackgroundService.class);
                    stopService( serviceIntent);
                    BackgroundService.shouldContinue = false;
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Using phone in darkness")
                .setTitle("Warning");
        dialog = builder.create();

        max = preferences.getInt("highest",0);
        maxLux.setText(max + "");
    }

    private void startService() {
        Intent serviceIntent = new Intent(MainActivity.this, BackgroundService.class);
        startService(serviceIntent);
    }

    private void findViews() {
        currentLux = (TextView)findViewById(R.id.currentLux);
        maxLux = (TextView) findViewById(R.id.maxLux);
        serviceSwitch = (Switch)findViewById(R.id.switch1);

        accelerometerX= (TextView)findViewById(R.id.accelerometerX);
        accelerometerY= (TextView)findViewById(R.id.accelerometerY);
        accelerometerZ= (TextView)findViewById(R.id.accelerometerZ);

        rotationX = (TextView)findViewById(R.id.rotationX);
        rotationY = (TextView)findViewById(R.id.rotationY);
        rotationZ = (TextView)findViewById(R.id.rotationZ);

    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.RED)
                .show();
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

    public static boolean isPowerConnected(Context context) {
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

        mSensorManager.registerListener(this, mPhotometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        editor.putInt("highest", max);

        if (serviceSwitch.isChecked())
            editor.putBoolean("switch", true);
        if (!serviceSwitch.isChecked())
            editor.putBoolean("switch", false);

        editor.putString("lastOpen", currentDateTimeString);
        editor.apply();
        isAppRunning = false;
        mSensorManager.unregisterListener(this);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor==mPhotometer) {
            int intValue = (int) event.values[0];
            currentLux.setTextSize(40);
            maxLux.setTextSize(40);
            currentLux.setText(intValue + "");
            final Toast toast = Toast.makeText(thisActivity, "Please Turn on BlueLightFilter", Toast.LENGTH_SHORT);
            if (!keyguardManager.inKeyguardRestrictedInputMode())
                if (intValue == 0 /*&& !dialog.isShowing()*/) {// using dialog not showing shows and hides the dialog as long as the first condition is true
                    if (isAppRunning)
                        dialog.show();
                    if (!isServiceRunning()/*&&toast.getView().getWindowVisibility() != View.VISIBLE*//* && toast.getView().isShown() == false*/) {
                        toast.show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toast.cancel();
                            }
                        }, 500);
                    }
                } else dialog.dismiss();

            maxLux.setText(max + "");
            max = max < intValue ? intValue : max;
        }
            /*android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);*/

        if (event.sensor==mAccelerometer){
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
            accelerometerX.setText(String.format( "%.4f", event.values[0]) + "");
            accelerometerY.setText(String.format( "%.4f", event.values[1]) + "");
            accelerometerZ.setText(String.format("%.4f", event.values[2]) + "");
        }
        else if (event.sensor==mMagnetometer) {
            System.arraycopy(event.values, 0, mMagnetometerReading,0,mMagnetometerReading.length);
        }

        boolean success =  SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
        if (success){
            SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
            rotationX.setText((int) Math.toDegrees(mOrientationAngles[0]) + "");
            rotationY.setText((int) Math.toDegrees(mOrientationAngles[1]) + "");
            rotationZ.setText((int) Math.toDegrees(mOrientationAngles[2]) + "");
        }

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