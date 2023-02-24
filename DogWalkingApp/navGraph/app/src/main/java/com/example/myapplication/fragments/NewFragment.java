package com.example.myapplication.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.strictmode.FragmentStrictMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.myapplication.ItemAdapter;
import com.example.myapplication.ItemAdapterRequest;
import com.example.myapplication.Model;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewFragment extends Fragment {


    public NewFragment() {
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
        View view=inflater.inflate(R.layout.fragment_new, container, false);


        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                FrameLayout frameLayout = container.findViewById(R.id.findFriendFrame);
                frameLayout.setClickable(false);
                getFragmentManager().beginTransaction().remove(NewFragment.this).commit();
                //getActivity().onBackPressed(); -works but returns to map becasue restarts activity?
                //getParentFragmentManager().popBackStack();
            }
        });


        recyclerView=view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //initData();


        //recyclerView.setAdapter(new ItemAdapter(initData(),getContext()));


        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ItemAdapter itemAdapter = new ItemAdapter(initData(),getContext());
                recyclerView.setAdapter(itemAdapter);

                globalQuery = query;

                searchDatabase(new FirebaseCallback() {
                    @Override
                    public void onResponse(Map<String, Object> user) {
                        boolean flag = false;
                        if(user.containsKey("empty")){
                            Toast.makeText(getContext(), "User Does Not Exist", Toast.LENGTH_SHORT).show();
                            flag = true;
                        }
                        if(user.containsKey("self")){
                            Toast.makeText(getContext(), "Unfortunately you cannot be your own friend :(", Toast.LENGTH_SHORT).show();
                            flag = true;
                        }
                        if(flag == false){
                            itemAdapter.add(new Model(user.get("username").toString()), itemList.size()-1);
                        }


                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return view;
    }

    private void searchDatabase(FirebaseCallback firebaseCallback) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");


        Query query = users.whereEqualTo("username", globalQuery);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                boolean found = false;
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {

                    Map<String, Object> user = (Map<String, Object>) querySnapshot.getData();
                    String email = mAuth.getCurrentUser().getEmail();
                    String email2 = user.get("email").toString();
                    if (!email2.equals(email)) {
                        firebaseCallback.onResponse(user);
                        found = true;
                    }else{
                        found = true;
                        Map<String,Object> noUser = new HashMap<String, Object>();
                        noUser.put("self", null);
                        firebaseCallback.onResponse(noUser);
                    }
                }
                if(!found){
                    Map<String,Object> noUser = new HashMap<String, Object>();
                    noUser.put("empty", null);
                    firebaseCallback.onResponse(noUser);
                };
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Couldn't access data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public interface FirebaseCallback {
        void onResponse(Map<String, Object> user);
    }




    private List<Model> initData() {

        itemList=new ArrayList<>();


        //itemList.add(new Model("name1"));


        return itemList;
    }
}