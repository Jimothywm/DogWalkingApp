package com.example.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ItemAdapterRequest;
import com.example.myapplication.Model;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class friendRequestsFragment extends Fragment {


    public friendRequestsFragment() {
        // Required empty public constructor
    }

    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    List<Model> itemList;
    String globalQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);


        Button backButton = view.findViewById(R.id.backButtonRequest);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                FrameLayout frameLayout = container.findViewById(R.id.findFriendFrame);
                frameLayout.setClickable(false);
                getFragmentManager().beginTransaction().remove(friendRequestsFragment.this).commit();
                //getActivity().onBackPressed(); -works but returns to map becasue restarts activity?
                //getParentFragmentManager().popBackStack();
            }
        });


        recyclerView = view.findViewById(R.id.recyclerViewRequest);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initData(new FirebaseCallback() {
            @Override
            public void onResponse(List<Model> itemList) {
                recyclerView.setAdapter(new ItemAdapterRequest(itemList,getContext()));
            }
        });
        //initData();

        return view;
    }

    private void initData(FirebaseCallback firebaseCallback) {
        itemList=new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        //gets current user
        Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
        currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots)
                {
                    String currentUserId = querySnapshot.getId();
                    users.document(currentUserId).collection("friend requests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots){
                                itemList.add(new Model(querySnapshot.get("username").toString()));
                            }
                            firebaseCallback.onResponse(itemList);
                        }
                    });
                }
            }
        });
    }

    public interface FirebaseCallback {
        void onResponse(List<Model> itemList);
    }


}
