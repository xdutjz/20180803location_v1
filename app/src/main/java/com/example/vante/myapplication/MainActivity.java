package com.example.vante.myapplication;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private Location location;
    private Button button;
    private TextView textView;
    public static final int SHOW_LOCATION=0;
    private String lastKnownLoc;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc!= null){
                location = loc;
                showLocation (location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            textView.setText("refresh failed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        textView.setText("Location");
        lastKnownLoc = "null";
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                locationInit();
            }
        });
    }


    public void locationInit(){
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationInitByGPS()||locationInitByNETWORK()){
                showLocation(location);
            }else{
                textView.setText("get location failed, last location is "+lastKnownLoc);
            }
        }catch (Exception e){
            textView.setText("get location failed, last location is "+lastKnownLoc);
        }
    }

    public boolean locationInitByGPS(){
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return false;
        }
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        int check = ContextCompat.checkSelfPermission(this,permissions[0]);
        if (check == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,locationListener);
            location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }else{
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,locationListener);
                location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (location != null){
            return true;
        }else {
            return false;
        }
    }

    public boolean locationInitByNETWORK(){
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            return false;
        }
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
        int check = ContextCompat.checkSelfPermission(this,permissions[0]);
        if (check == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,locationListener);
            location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }else{
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        if (location != null){
            return true;
        }else {
            return false;
        }
    }

    public final void startForeground(int id, Notification notification){}
    public final void stopForeground(boolean removeNotification){
        //stopForeground(removeNotification ?STOP_FOREGROUND_REMOVE : 1);
    }

    private void init(){
        button = (Button)findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.textView);
        startForeground(1, null);

    }

    private void showLocation(final Location loc){
        new Thread(new Runnable(){
            @Override
            public void run() {
                    try{

                        /*Calendar calendar = Calendar.getInstance();
                        String year = String.valueOf(calendar.get(Calendar.YEAR));
                        String month = String.valueOf(calendar.get(Calendar.MONTH));
                        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                        String minute = String.valueOf(calendar.get(Calendar.MINUTE));
                        String second = String.valueOf(calendar.get(Calendar.SECOND));

                        String settime = year + "-" + month + "-" + day + "-" + hour +
                                "-" + minute + "-" + second;*/

                        Message message = new Message();
                        message.what = SHOW_LOCATION;
                        String a = String.valueOf(loc.getLatitude());
                        String b = String.valueOf(loc.getLongitude());
                        message.obj = "Latitude: " + a + "\n" + "Longitude: " + b;
                        lastKnownLoc = String.valueOf(loc.getLatitude()+loc.getLongitude());
                        handler.sendMessage(message);

                        /*String filePath = "/data/Test/";
                        String fileName = settime;

                        FileWriter fw = new FileWriter(filePath + fileName + ".txt");
                        fw.flush();
                        fw.write(a + b);
                        fw.close();*/





                    } catch (Exception e) {
                        Message message = new Message();
                        message.what = SHOW_LOCATION;
                        message.obj = "get location failed, last location is " + lastKnownLoc;
                        handler.sendMessage(message);
                        e.printStackTrace();
                    }
            }
        }).start();
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_LOCATION:
                    String currentPosition = (String) msg.obj;
                    textView.setText(currentPosition);
                    break;
                default:
                    break;
            }
        }
    };

}
