package com.example.myapplication.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.KeyEventDispatcher;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.example.myapplication.AppLocationService;
import com.example.myapplication.ItemAdapterPaths;
import com.example.myapplication.MapsActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.LoggingPermission;

import okio.AsyncTimeout;


public class HomeFragment extends Fragment {

    public GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LinkedList<LatLng> pointsLL;
    private FloatingActionButton record;
    private Boolean flag;
    private FirebaseAuth mAuth;
    private String sameID;
    private Polyline polylineRef;
    private LatLng receivedPoint;
    private boolean initZoom;
    private LocationCallback locationCallback;
    private Context context;

    public void PassPointsLL(LinkedList<LatLng> pointsLL) {
        if (polylineRef != null) {
            polylineRef.remove();
            polylineRef = null;
        }
        polylineRef = mMap.addPolyline(new PolylineOptions().addAll(pointsLL));//////////////////////////////////////////
        //mMap.addPolyline(new PolylineOptions()).setPoints(pointsLL);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("DESTROY");
        getContext().sendBroadcast(broadcastIntent);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }

        context.unregisterReceiver(pointReceiver);

    }

    @Override
    public void onStop(){
        super.onStop();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("APP_CLOSED");
        getContext().sendBroadcast(broadcastIntent);
    }


    private BroadcastReceiver pointReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "RECEIVER_CREATED")
            {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("RECORD_START");
                context.sendBroadcast(broadcastIntent);
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                    }
                });


            }
            if(intent.getAction() == "SENDING_POINT"){
                Bundle bundle = intent.getExtras();
                receivedPoint = ((LatLng)bundle.get("point"));
                if(receivedPoint != null)
                {
                    if(pointsLL == null)
                    {
                        pointsLL = new LinkedList<LatLng>();
                    }
                    pointsLL.add(receivedPoint);
                    if (polylineRef != null) {
                        polylineRef.setPoints(pointsLL);
                    }else{
                        polylineRef = mMap.addPolyline(new PolylineOptions());
                    }
                    if(getContext() != null){
                        Toast.makeText(getContext(), "point has been returned", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            if(intent.getAction() == "SENDING_ALL_POINTS"){
                Bundle bundle = intent.getExtras();
                ArrayList<LatLng> initPointsLL = ((ArrayList<LatLng>)bundle.get("points"));
                pointsLL = new LinkedList<LatLng>();
                if(!initPointsLL.isEmpty())
                {
                    pointsLL.addAll(initPointsLL);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointsLL.getLast(), 16));
                    if(getContext() != null) {
                        Toast.makeText(getContext(), "points have been returned", Toast.LENGTH_SHORT).show();
                    }
                    if(polylineRef != null){
                        polylineRef.remove();
                    }
                    polylineRef = mMap.addPolyline(new PolylineOptions());
                    polylineRef.setPoints(pointsLL);
                }
            }
            if(intent.getAction() == "RECORD_STOP_ACTIVITY"){
                Bundle bundle = intent.getExtras();
                ArrayList<LatLng> initPointsLL = ((ArrayList<LatLng>)bundle.get("points"));
                pointsLL = new LinkedList<LatLng>();
                pointsLL.addAll(initPointsLL);
                if(!pointsLL.isEmpty())
                {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointsLL.getLast(), 16));
                }

                polylineRef.setPoints(pointsLL);
                Context context2 = getContext();
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                //AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                View pathPopupView = getLayoutInflater().inflate(R.layout.pathpopup, null);
                ImageView imagePopup = pathPopupView.findViewById(R.id.pathImageViewPopup);
                EditText editPathName = pathPopupView.findViewById(R.id.editPathNamePopup);


                setImage(imagePopup);

                Button backButtonPathPopup = pathPopupView.findViewById(R.id.backButtonPathPopup);
                builder.setView(pathPopupView);
                AlertDialog alertDialog = builder.create();
                backButtonPathPopup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                Button submitPopupButton = pathPopupView.findViewById(R.id.submitButtonPopup);
                submitPopupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pointsLL.isEmpty()){
                            Toast.makeText(getContext(), "There is no recorded path", Toast.LENGTH_SHORT).show();
                        }else{
                            savePath(pointsLL, editPathName.getText().toString());
                            pointsLL = new LinkedList<LatLng>();
                            polylineRef.remove();
                            alertDialog.dismiss();
                            Toast.makeText(getContext(), "point has been returned and saved", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                alertDialog.show();
            }


        }
    };

    @Override
    public void onStart(){
        super.onStart();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("APP_OPEN");
                getContext().sendBroadcast(broadcastIntent);
            }

    }

    public void updateMapFromCSVs() {
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.clear();
                updateMap();
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);


        context = getContext();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SENDING_POINT");
        intentFilter.addAction("RECEIVER_CREATED");
        intentFilter.addAction("RECORD_STOP_ACTIVITY");
        intentFilter.addAction("SENDING_ALL_POINTS");
        getContext().registerReceiver(pointReceiver, intentFilter);

        flag = false;
        //locationManager=(LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        record = view.findViewById(R.id.record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "start recording here", Toast.LENGTH_SHORT).show();
                flag = !flag;
                if (flag) {
                    record.setImageResource(R.drawable.ic_baseline_stop_24);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && flag) {
                        if(polylineRef != null){
                            polylineRef.remove();
                        }
                        pointsLL = new LinkedList<LatLng>();
                        polylineRef = mMap.addPolyline(new PolylineOptions());

                        Intent intent = new Intent(getActivity(), AppLocationService.class);

                        getActivity().startForegroundService(intent);


                    }
                    else {
                        recordPathOld();
                    }

                } else {
                    record.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (polylineRef != null){
                            polylineRef.remove();
                        }

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("RECORD_STOP");
                        getContext().sendBroadcast(broadcastIntent);

                    }else {
                        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                        View pathPopupView = getLayoutInflater().inflate(R.layout.pathpopup, null);
                        ImageView imagePopup = pathPopupView.findViewById(R.id.pathImageViewPopup);
                        EditText editPathName = pathPopupView.findViewById(R.id.editPathNamePopup);
                        setImage(imagePopup);
                        Button backButtonPathPopup = pathPopupView.findViewById(R.id.backButtonPathPopup);
                        builder.setView(pathPopupView);
                        AlertDialog alertDialog = builder.create();
                        backButtonPathPopup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });
                        Button submitPopupButton = pathPopupView.findViewById(R.id.submitButtonPopup);
                        submitPopupButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (pointsLL.isEmpty()){
                                    Toast.makeText(getContext(), "There is no recorded path", Toast.LENGTH_SHORT).show();
                                }else{
                                    savePath(pointsLL, editPathName.getText().toString());
                                    pointsLL = new LinkedList<LatLng>();
                                    polylineRef.remove();
                                    alertDialog.dismiss();
                                    Toast.makeText(getContext(), "point has been returned and saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        alertDialog.show();
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                }
            }
        });

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(getContext()).checkLocationSettings(builder.build());
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Toast.makeText(getContext(), "settings updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "settings error", Toast.LENGTH_SHORT).show();
            }
        });


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                //if permissions are already granted
                int result = getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                int result2 = getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                if (result == 0 && result2 == 0) {
                    mMap.setMyLocationEnabled(true);
                    mMap.clear();

                    //pointsLL = new LinkedList<LatLng>();
                    //Polyline polyline = mMap.addPolyline(new PolylineOptions());



                    /*
                    locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                        //super.onLocationResult(locationResult);
                        //Toast.makeText(getContext(), locationResult.getLastLocation().getLatitude() + "/" +locationResult.getLastLocation().getLongitude(), Toast.LENGTH_SHORT).show();
                        LatLng point = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                        if (initZoom == false) {
                            LatLng initLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(initLocation, 14));
                            initZoom = true;
                        }
                        if (flag == true) {
                            pointsLL.add(point);
                            polyline.setPoints(pointsLL);
                        }
                        if (flag == false && !pointsLL.isEmpty()) {
                            //TODO: save to paths file and make new points that is empty
                            savePath(pointsLL);
                            pointsLL = new LinkedList<LatLng>();
                            polyline.remove();///////////////////////////////////////////////////////////////////

                        }


                        }
                    };*/
                    //fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }
                updateMap();
            }
        });

        //might put locationmanager here

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == 0 && grantResults[1] == 0) {
                mMap.setMyLocationEnabled(true);

            } else {
                Toast.makeText(getContext(), "app needs permission to location service to work", Toast.LENGTH_SHORT).show();
                //requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    public void updateMap() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");

        users.whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    String currentUsername = querySnapshot.get("username").toString();
                    users.document(querySnapshot.getId()).collection("CSVs").whereEqualTo("checked", "true").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                String filename = querySnapshot.get("filename").toString();
                                StorageReference pathReference = storageRef.child(filename);
                                pathReference.getBytes(1000000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @SuppressLint("MissingPermission")
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        String decoded = null;
                                        try {
                                            decoded = new String(bytes, "UTF-8");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        decoded = decoded.replaceAll("\uFEFF", "");//puts this formatting for bytecode
                                        String[] lines = decoded.split("\n");
                                        for (String line : lines) {
                                            String[] element = line.split(",");
                                            if (element.length == 3 && element[0].matches("[0-9.-]*") && element[1].matches("[0-9.-]*")) {
                                                double lat = Double.parseDouble(element[0]);
                                                double lon = Double.parseDouble(element[1]);
                                                LatLng place = new LatLng(lat, lon);
                                                mMap.addMarker(new MarkerOptions().position(place).title("Marker in " + element[2]));
                                            }
                                        }

                                        try {
                                            TimeUnit.SECONDS.sleep(3);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                if(location != null) {
                                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    public void savePathImage(LinkedList<LatLng> pointsLL){
        String pointString = new String();
        pointString = "&path=color:0x000000ff|weight:5";
        LatLng point = pointsLL.get(0);
        for(LatLng pointStr :pointsLL) {
            pointString = pointString + "|" + pointStr.latitude + "," + pointStr.longitude;
        }
        //.url("https://maps.googleapis.com/maps/api/staticmap?center=" +point.latitude + "," + point.longitude +"&zoom=15&size=400x400&key=AIzaSyCQKGCHUEbPIpuKxU72cFSD4XCh_eRt7GM" + pointString)
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://maps.googleapis.com/maps/api/staticmap?size=400x400&key=AIzaSyCQKGCHUEbPIpuKxU72cFSD4XCh_eRt7GM" + pointString)
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                //String string = response.toString();
                mAuth = FirebaseAuth.getInstance();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference users = db.collection("users");

                users.whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots){
                            String currentUsername = querySnapshot.get("username").toString();
                            Map<String, String> savePoints = new HashMap<String, String>();
                            savePoints.put("path image", imageEncoded);
                            users.document(querySnapshot.getId()).collection("path images").document(sameID).set(savePoints);
                            Toast.makeText(getActivity().getApplicationContext(), "path image success", Toast.LENGTH_SHORT);
                            ((MapsActivity) getActivity()).updateRequestRecycler();
                        }
                    }
                });
            }
        });
    }


    public void savePath(LinkedList<LatLng> pointsLL, String pathName){
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");

        users.whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots){
                    String currentUsername = querySnapshot.get("username").toString();
                    Map<String, Object> savePoints = new HashMap<String, Object>();
                    savePoints.put("path", pointsLL);
                    savePoints.put("path name", pathName);
                    savePoints.put("friend name", "null");
                    users.document(querySnapshot.getId()).collection("paths").add(savePoints).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            sameID = documentReference.getId();
                            savePathImage(pointsLL);
                        }
                    });
                    Toast.makeText(getActivity().getApplicationContext(), "success", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void setImage(ImageView imageViewPopup){
        String pointString = new String();
        pointString = "&path=color:0x000000ff|weight:5";
        if(pointsLL.isEmpty()){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://maps.googleapis.com/maps/api/staticmap?center=" +point.latitude + "," + point.longitude +"&zoom=14&size=400x400&key=AIzaSyCQKGCHUEbPIpuKxU72cFSD4XCh_eRt7GM")
                            .method("GET", null)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            InputStream inputStream = response.body().byteStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageViewPopup.setImageBitmap(bitmap);
                                }
                            });
                        }
                    });
                }
            });

        }else{

            LatLng point = pointsLL.get(0);
            for(LatLng pointStr :pointsLL) {
                pointString = pointString + "|" + pointStr.latitude + "," + pointStr.longitude;
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/staticmap?&size=400x400&key=AIzaSyCQKGCHUEbPIpuKxU72cFSD4XCh_eRt7GM" + pointString)
                    .method("GET", null)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageViewPopup.setImageBitmap(bitmap);
                        }
                    });
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    public void recordPathOld(){
        if(polylineRef != null){
            polylineRef.remove();
        }
        if(pointsLL == null){
            pointsLL = new LinkedList<LatLng>();
        }
        polylineRef = mMap.addPolyline(new PolylineOptions());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                LatLng point = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                pointsLL.add(point);
                polylineRef.setPoints(pointsLL);
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}