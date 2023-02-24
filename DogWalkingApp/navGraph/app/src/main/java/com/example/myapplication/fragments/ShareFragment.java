package com.example.myapplication.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.myapplication.ItemAdapterPaths;
import com.example.myapplication.ItemAdapterShare;
import com.example.myapplication.Model;
import com.example.myapplication.ModelShare;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareFragment extends Fragment {

    private String pathID;
    private FirebaseAuth mAuth;
    private List<ModelShare> itemList;

    public ShareFragment(String pathID) {
        this.pathID = pathID;
    }

    public void sharePath(String friendUsername){
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        Query friendUserNameQuery = users.whereEqualTo("username", friendUsername);
        Query currentUsername = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail().toString());
        currentUsername.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshotCurrent : queryDocumentSnapshots) {
                    String currentUsername = querySnapshotCurrent.get("username").toString();
                    friendUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                HashMap<String, String> path= new HashMap<String, String>();
                                path.put("path", pathID);
                                path.put("user", currentUsername);

                                users.document(querySnapshot.getId()).collection("friends paths").add(path);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =  inflater.inflate(R.layout.fragment_share, container, false);


        Button backButton = view.findViewById(R.id.backButtonShare);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                FrameLayout frameLayout = container.findViewById(R.id.shareFrame);
                frameLayout.setClickable(false);
                getFragmentManager().beginTransaction().remove(ShareFragment.this).commit();
                //getActivity().onBackPressed(); -works but returns to map becasue restarts activity?
                //getParentFragmentManager().popBackStack();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewShare);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        String userEmail = mAuth.getCurrentUser().getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        itemList=new ArrayList<>();

        Query query = users.whereEqualTo("email", userEmail);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots)
                {
                    users.document(querySnapshot.getId()).collection("friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots){
                                Map<String, Object> user = (Map<String, Object>) querySnapshot.getData();
                                Object friendUsername = user.get("username");

                                itemList.add(new ModelShare(friendUsername.toString(), pathID, false));
                            }
                            recyclerView.setAdapter(new ItemAdapterShare(itemList, getContext()));
                        }
                    });
                }
            }
        });


        return view;
    }

}