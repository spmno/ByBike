package com.spmno.bybike;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements View.OnClickListener{

    private float maxSpeed = 0;
    private Timer timer = new Timer();
    private boolean isStart = false;
    private Date lastDate;
    private TextView spendTextView;
    private MileRecorder mileRecorder;
    private LocationListener locationListener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            long diff = ((message.arg2) | ((message.arg1) << 32));

            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
            long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
            long seconds = (diff-days*(1000 * 60 * 60 * 24) - hours*(1000* 60 * 60) - minutes*(1000*60))/(1000);

            spendTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike);
        Button startStopButton = (Button)findViewById(R.id.startStopbutton);
        startStopButton.setOnClickListener(this);
        spendTextView = (TextView)findViewById(R.id.timeValueTextView);
        TextView maxSpeedView = (TextView)findViewById(R.id.maxSpeedValueTextView);
        TextView speedView = (TextView)findViewById(R.id.speedValueTextView);
        maxSpeedView.setText(String.valueOf(0.0));
        speedView.setText(String.valueOf(0.0));
        spendTextView.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
        openGPSSettings();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void openGPSSettings() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            String mentionInfo = getResources().getString(R.string.gps_work);
            Toast.makeText(this, mentionInfo, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String warnningInfo = getResources().getString(R.string.please_open_gps);
        Toast.makeText(this, warnningInfo, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(intent,0);
    }

    private void setupLocation() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        //locationManager.setTestProviderEnabled("gps", true);
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void updateLocation(Location location) {
        if (location != null) {
            TextView speedTextView = (TextView)findViewById(R.id.speedValueTextView);
            TextView timeTextView = (TextView)findViewById(R.id.timeValueTextView);
            TextView maxSpeedTextView = (TextView)findViewById(R.id.maxSpeedValueTextView);
            float currentSpeed = location.getSpeed() * 3.6f;
            speedTextView.setText(String.valueOf(currentSpeed));
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
                maxSpeedTextView.setText(String.valueOf(maxSpeed));
            }
            Log.v("latitude", String.valueOf(location.getLatitude()));
            Log.v("longitude", String.valueOf(location.getLongitude()));
            String logText = String.format("%d, %d, %d\n", location.getLongitude(), location.getLatitude(), location.getTime());
            LogSaver logSaver = LogSaver.getInstance();
            logSaver.saveLog(logText);
            mileRecorder.recordMile(location);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startStopbutton: {

                Button button = (Button)findViewById(R.id.startStopbutton);
                if (isStart) {
                    timer.cancel();
                    button.setText(R.string.start);
                    spendTextView.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
                    isStart = false;
                    LogSaver logSaver = LogSaver.getInstance();
                    LocationManager locationManager = (LocationManager) this
                            .getSystemService(Context.LOCATION_SERVICE);
                    locationManager.removeUpdates(locationListener);
                    logSaver.stopLog();
                } else {
                    setupLocation();
                    LogSaver logSaver = LogSaver.getInstance();
                    logSaver.startLog();
                    lastDate = new Date();
                    timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            Date currentDate = new Date();
                            long timeDiff = currentDate.getTime() - lastDate.getTime();
                            Message message = new Message();
                            message.arg1 = (int)(timeDiff >> 32);
                            message.arg2 = (int)(timeDiff);
                            handler.sendMessage(message);
                        }
                    };
                    timer.schedule(timerTask, 1000, 1000);
                    button.setText(R.string.stop);
                    isStart = true;
                }
            }
        }
    }
}
