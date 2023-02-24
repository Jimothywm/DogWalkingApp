package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

public class AppLocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LinkedList<LatLng> pointsLL;
    private boolean flag;
    private Notification notification;



    public AppLocationService() {
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        pointsLL = new LinkedList<LatLng>();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("NOTIFICATION_CHANNEL_ID", "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Recording Location");
        notificationManager.createNotificationChannel(channel);

        Intent notificationIntent = new Intent(this, AppLocationService.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        notification = new NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_baseline_arrow_back_24).setContentTitle("Title").setContentText("message")
                .setContentIntent(pendingIntent).build();

        startForeground(1234, notification);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(1000);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getBaseContext());

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("APP_OPEN");
        intentFilter.addAction("APP_CLOSED");
        intentFilter.addAction("RECORD_START");
        intentFilter.addAction("RECORD_STOP");
        intentFilter.addAction("DESTROY");
        registerReceiver(broadcastReceiverService, intentFilter);

        flag = true;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RECEIVER_CREATED");
        sendBroadcast(broadcastIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(){

    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            LatLng point = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
            pointsLL.add(point);
            if(flag == true)
            {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("SENDING_POINT");
                broadcastIntent.putExtra("point", point);
                sendBroadcast(broadcastIntent);
                //TODO: send to other Broadcast
            }
        }
    };

        private BroadcastReceiver broadcastReceiverService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == "RECORD_START")
            {
                flag = true;
            }
            if(action == "APP_OPEN")
            {
                //TODO: send all points
                flag = true;
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("SENDING_ALL_POINTS");
                broadcastIntent.putExtra("points", pointsLL);
                sendBroadcast(broadcastIntent);
            }
            if(action == "APP_CLOSED")
            {
                flag = false;
            }
            if(action == "RECORD_STOP"){
                flag = true;
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("RECORD_STOP_ACTIVITY");
                broadcastIntent.putExtra("points", pointsLL);
                sendBroadcast(broadcastIntent);
                onDestroy();

            }            //TODO: record Stop to send all points to PATHS FRAGMENT
            if(action == "DESTROY"){
                onDestroy();
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        pointsLL = new LinkedList<LatLng>();

        stopForeground(true);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}