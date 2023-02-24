package com.example.myapplication.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ItemAdapterContacts;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.MapsActivity;
import com.example.myapplication.Model;
import com.example.myapplication.ModelPath;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ContactsFragment extends Fragment {
    private Button logOutButton;
    private Button deleteAccountButton;
    private Button findFriendsButton;
    private Button friendRequestButton;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private List<Model> itemList;
    private View view;
    private FirebaseFirestore db;

    private Map<String, Object> user = new HashMap<>();


    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "UPDATE_CONTACTS"){
                itemList = new ArrayList<Model>();
                setRecyclerView();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_CONTACTS");
        getContext().registerReceiver(refreshReceiver, intentFilter);

        //user.put("username", null);
        //user.put("fullname", null);
        //user.put("email", null);



        mAuth = FirebaseAuth.getInstance();
        String userEmail = mAuth.getCurrentUser().getEmail();
        db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        itemList=new ArrayList<>();

        Query query = users.whereEqualTo("email", userEmail);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots)
                {
                    //user = querySnapshot.toObject((Map<String,Object>).class);
                    Map<String, Object> user = (Map<String, Object>) querySnapshot.getData();
                    Object currentUsername = user.get("username");
                    TextView edited = getActivity().findViewById(R.id.editUsername);
                    edited.setText(currentUsername.toString());
                }
            }
        });

        //edited.setText();

        logOutButton = view.findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //getActivity().finishAffinity();
                startActivity(intent);


            }
        });

        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                builder.setTitle("Delete account");
                builder.setMessage("Are you sure you want to delete your account?\nYou will lose all of your data");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentEmail = mAuth.getCurrentUser().getEmail().toString();
                        FirebaseUser currentUser =mAuth.getCurrentUser();

                        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                        View popupView = getLayoutInflater().inflate(R.layout.popup, null);
                        EditText username = popupView.findViewById(R.id.usernameReAuth);
                        EditText password = popupView.findViewById(R.id.passwordReAuth);
                        Button continueButton = popupView.findViewById(R.id.continueButton);
                        Button backButton = popupView.findViewById(R.id.backButtonPopup);
                        builder.setView(popupView);
                        AlertDialog alertDialog = builder.create();

                        backButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });

                        continueButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        authResult.getCredential();
                                        mAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                db.collection("users").whereEqualTo("email", currentEmail).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {
                                                            db.collection("users").document(queryDocumentSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    mAuth.signOut();
                                                                    startActivity(new Intent(getActivity(), LoginActivity.class));
                                                                    Toast.makeText(getContext(), "Deleted account", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });;
                                    }
                                });
                            }
                        });

                        alertDialog.show();



                        /*


                         */
                        /*
                        mAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                db.collection("users").whereEqualTo("email", currentEmail).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {
                                            db.collection("users").document(queryDocumentSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    mAuth.signOut();
                                                    startActivity(new Intent(getActivity(), LoginActivity.class));
                                                }
                                            });
                                        }
                                    }
                                });
                                //TODO: start login (clear backstack?)
                            }
                        });
                         */
                        Toast.makeText(getContext(), "Deletes account", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        });

        findFriendsButton = view.findViewById(R.id.findFriendsButton);
        findFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                Fragment findFriendsFragment = new NewFragment();
                FrameLayout frameLayout = view.findViewById(R.id.findFriendFrame);
                frameLayout.setClickable(true);
                ft.replace(R.id.findFriendFrame, findFriendsFragment);
                ft.commit();
            }
        });

        friendRequestButton = view.findViewById(R.id.friendRequestButton);
        friendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                Fragment friendRequestsFragment = new friendRequestsFragment();
                FrameLayout frameLayout = view.findViewById(R.id.findFriendFrame);
                frameLayout.setClickable(true);
                ft.replace(R.id.findFriendFrame, friendRequestsFragment);
                ft.commit();
            }
        });


        setRecyclerView();
        //ItemAdapterContacts itemAdapterContacts = new ItemAdapterContacts(initData(), getContext());


        return view;

    }

    public interface FirebaseCallback {
        void onResponse(Map<String, Object> user);
    }

    public void setRecyclerView(){
        recyclerView=view.findViewById(R.id.recyclerViewContacts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db.collection("users").whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots) {
                    String documentId = querySnapshot.getId();
                    db.collection("users").document(documentId).collection("friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<String> stringList = new ArrayList<String>();
                            for (QueryDocumentSnapshot querySnapshot: queryDocumentSnapshots) {
                                QueryDocumentSnapshot querySnapshot2 = querySnapshot;
                                String test = querySnapshot.get("username").toString();
                                itemList.add(new Model(test));
                                stringList.add(test);
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                stringList.sort(String.CASE_INSENSITIVE_ORDER);
                                itemList.clear();
                                for (String string:stringList) {
                                    itemList.add(new Model(string));
                                }
                            }

                            recyclerView.setAdapter(new ItemAdapterContacts(itemList, getContext()));
                        }
                    });
                }
            }
        });
    }




    private List<Model> initData() {

        itemList=new ArrayList<>();

        return itemList;
    }
}