package com.example.myapplication.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.CSVModel;
import com.example.myapplication.ItemAdapterCSVs;
import com.example.myapplication.MapsActivity;
import com.example.myapplication.Model;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVFragment extends Fragment {

    private FirebaseAuth mAuth;
    private List<CSVModel> itemList;
    private RecyclerView recyclerView;
    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == Activity.RESULT_OK){
                    Intent data = o.getData();
                    Uri uri = data.getData();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference fileReference = storageReference.child(uri.getLastPathSegment());
                    fileReference.putFile(uri);

                    mAuth = FirebaseAuth.getInstance();
                    String userEmail = mAuth.getCurrentUser().getEmail();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference users = db.collection("users");
                    users.whereEqualTo("email", userEmail).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots) {
                                HashMap<String,String> csvInput = new HashMap<String, String>();
                                csvInput.put("filename", uri.getLastPathSegment());
                                csvInput.put("checked", "false");
                                users.document(querySnapshot.getId()).collection("CSVs").add(csvInput).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        itemList.add(new CSVModel(uri.getLastPathSegment(), false));
                                        recyclerView.setAdapter(new ItemAdapterCSVs(itemList, getContext()));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        this.resultLauncher = resultLauncher;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_c_s_v, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCSVs);
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
                for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots) {
                    users.document(querySnapshot.getId()).collection("CSVs").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            boolean checkedBoolean;
                            String filename;
                            String checked;
                            for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots) {
                                filename = querySnapshot.get("filename").toString();
                                checked = querySnapshot.get("checked").toString();
                                checkedBoolean = Boolean.parseBoolean(checked);
                                itemList.add(new CSVModel(filename, checkedBoolean));
                            }
                            recyclerView.setAdapter(new ItemAdapterCSVs(itemList, getContext()));
                        }
                    });
                }
            }
        });

        Button confirmChoiceButton = view.findViewById(R.id.confirmCSVChoice);
        confirmChoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CSVModel> itemList = ((ItemAdapterCSVs)recyclerView.getAdapter()).getItemList1();
                //get data from itemlist into map
                String filename;
                Boolean checked;
                String checkedString;
                HashMap<String, String> itemListMap = new HashMap<String, String>();
                for(int i = 0; i<itemList.size(); i++){
                    filename = itemList.get(i).getName();
                    checked = itemList.get(i).isChecked();
                    checkedString = checked.toString();
                    itemListMap.put(filename, checkedString);
                }



                mAuth = FirebaseAuth.getInstance();
                String userEmail = mAuth.getCurrentUser().getEmail();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference users = db.collection("users");
                db.collection("users").whereEqualTo("email", userEmail).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot querySnapshotUser: queryDocumentSnapshots) {
                            db.collection("users").document(querySnapshotUser.getId()).collection("CSVs").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot querySnapshotFile: queryDocumentSnapshots) {
                                        db.collection("users").document(querySnapshotUser.getId()).collection("CSVs").document(querySnapshotFile.getId()).update("checked", itemListMap.get(querySnapshotFile.get("filename"))).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                ((HomeFragment) getActivity().getSupportFragmentManager().findFragmentByTag("homeFragment")).updateMapFromCSVs();
                                                ((MapsActivity) getActivity()).clickHome();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });

                ((HomeFragment) getActivity().getSupportFragmentManager().findFragmentByTag("homeFragment")).updateMapFromCSVs();
                ((MapsActivity) getActivity()).clickHome();
                //tells maps to do it
                //changes users csv checks
            }
        });
        Button uploadFile = view.findViewById(R.id.uploadCSVFile);
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("CSV Upload");
                builder.setMessage("All files uploaded must be .csv files with the following format:\n\n" +
                        "latitude, longitude, name of marker\n" +
                        "latitude, longitude, name of marker\n" +
                        "latitude, longitude, name of marker");
                builder.setPositiveButton("I Understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent data2 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        data2.setType("*/*");
                        data2 =Intent.createChooser(data2, "choose a file");
                        resultLauncher.launch(data2);
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


                //upload file
                //update users csv
            }
        });
        return view;
    }
}