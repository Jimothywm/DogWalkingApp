package com.example.myapplication.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ItemAdapterPaths;
import com.example.myapplication.MapsActivity;
import com.example.myapplication.ModelPath;
import com.example.myapplication.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PathsFragment extends Fragment {


    private BroadcastReceiver broadcastReceiverService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == "DELETE_PATHS") {
                RefreshRecycler();
            }
        }
    };

    public void RefreshRecycler(){
        itemList = new ArrayList<ModelPath>();
        initData(new FirebaseCallback() {
            @Override
            public void onResponse(List<ModelPath> itemList) {
                /*
                    List<String> stringList = new ArrayList<String>();
                    HashMap<String, String> itemListTemp = new HashMap<String, String>();
                    for (ModelPath model: itemList) {
                        itemListTemp.put(model.getName(), model.getID());
                        stringList.add(model.getName());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stringList.sort(String.CASE_INSENSITIVE_ORDER);
                        itemList.clear();
                        for (String string: stringList) {
                            itemList.add(new ModelPath(string, itemListTemp.get(string)));
                        }
                    }

                 */
                recyclerView.setAdapter(new ItemAdapterPaths(itemList,getContext()));
            }
        });
    }

    public void PassPointsLL(LinkedList<LatLng> pointsLL){
        ((MapsActivity) getActivity()).PassPointsLL(pointsLL);
    }

    public void addPopup(String pathID, Bitmap image){
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment shareFragment = new ShareFragment(pathID);
        FrameLayout frameLayout = view.findViewById(R.id.shareFrame);
        frameLayout.setClickable(true);
        ft.replace(R.id.shareFrame, shareFragment);
        ft.commit();
    }

    public PathsFragment() {
        // Required empty public constructor
    }

    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    List<ModelPath> itemList;
    String globalQuery;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_paths, container, false);
        this.view=view;// idk if this works
        ////////////////////////////////////////////////////////////////////////////////////////

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_CONTACTS");
        getContext().registerReceiver(broadcastReceiverService, intentFilter);

        recyclerView = view.findViewById(R.id.recyclerViewPaths);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initData(new FirebaseCallback() {
            @Override
            public void onResponse(List<ModelPath> itemList) {
                recyclerView.setAdapter(new ItemAdapterPaths(itemList,getContext()));
            }
        });
        //initData();

        return view;
    }

    private void initData(FirebaseCallback firebaseCallback) {
        itemList = new ArrayList<ModelPath>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        //gets current users username and adds to friend request collection
        Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
        currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    String currentUserID = querySnapshot.getId().toString();
                    users.document(currentUserID).collection("paths").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            HashMap<String, Object> itemListTemp = new HashMap<String, Object>();
                            List<String> stringList = new ArrayList<String>();
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                HashMap<String, String> itemTemp = new HashMap<String, String>();
                                itemList.add(new ModelPath(querySnapshot.get("path name").toString(), querySnapshot.getId().toString(), "null"));
                                itemTemp.put("path name", querySnapshot.get("path name").toString());
                                itemTemp.put("path ID", querySnapshot.getId().toString());
                                itemTemp.put("friend username", "null");
                                stringList.add(querySnapshot.get("path name").toString());

                                itemListTemp.put(querySnapshot.get("path name").toString(), ((Object)itemTemp));
                            }
                            users.document(currentUserID).collection("friends paths").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots){
                                        String friendPathID = queryDocumentSnapshot.get("path").toString();
                                        String friendUsername = queryDocumentSnapshot.get("user").toString();
                                        //TODO: Friend ID instead of username
                                        users.whereEqualTo("username", friendUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (QueryDocumentSnapshot queryDocumentSnapshot1: queryDocumentSnapshots) {
                                                    String friendUserID = queryDocumentSnapshot1.getId();
                                                    //TODO: idk if it gets the paths sent
                                                    users.document(friendUserID).collection("paths").document(friendPathID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            if(documentSnapshot.exists()){
                                                                HashMap<String, String> itemTemp = new HashMap<String, String>();
                                                                itemList.add(new ModelPath(documentSnapshot.get("path name").toString(), documentSnapshot.getId().toString(), friendUsername.toString()));
                                                                itemTemp.put("path name", documentSnapshot.get("path name").toString());
                                                                itemTemp.put("path ID", documentSnapshot.getId().toString());
                                                                itemTemp.put("friend username", friendUsername);
                                                                stringList.add(documentSnapshot.get("path name").toString());
                                                                itemListTemp.put(documentSnapshot.get("path name").toString(), itemTemp);

                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                    stringList.sort(String.CASE_INSENSITIVE_ORDER);
                                                                    itemList.clear();
                                                                    for (String string : stringList) {
                                                                        HashMap<String, String> map = (HashMap<String, String>) itemListTemp.get(string);
                                                                        itemList.add(new ModelPath(map.get("path name"), map.get("path ID"), map.get("friend username")));
                                                                    }
                                                                    firebaseCallback.onResponse(itemList);
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                stringList.sort(String.CASE_INSENSITIVE_ORDER);
                                itemList.clear();
                                for (String string: stringList) {
                                    HashMap<String, String> map = (HashMap<String, String>) itemListTemp.get(string);
                                    itemList.add(new ModelPath(map.get("path name"), map.get("path ID"), map.get("friend username")));
                                }
                                firebaseCallback.onResponse(itemList);
                            }

                        }
                    });
                }
            }
        });
    }




    public interface FirebaseCallback {
        void onResponse(List<ModelPath> itemList);
    }

}
