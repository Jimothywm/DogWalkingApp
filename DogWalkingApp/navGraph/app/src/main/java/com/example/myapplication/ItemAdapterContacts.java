package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapterContacts extends RecyclerView.Adapter<ItemAdapterContacts.ViewHolder> {

    FirebaseAuth mAuth;
    List<Model> itemList1;
    private Context context;
    String globalQuery;

    public ItemAdapterContacts(List<Model> itemList,Context context) {

        this.itemList1=itemList;
        this.context=context;

    }

    @NonNull
    @Override
    public ItemAdapterContacts.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.rowitem,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapterContacts.ViewHolder holder, final int position) {


        holder.itemUsernameText.setText(itemList1.get(position).getName());
        holder.itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Deleting User");
                builder.setMessage("Are you sure you want to delete user" + holder.itemUsernameText.getText() + " from your friends list?\n"
                + "You will be unable to recover any of the paths shared with you by this user and they will not be able to view any paths you have shared with them");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUser(holder.itemUsernameText.getText().toString());
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();



            }
        });

    }

    private void deleteUser(String friendUsername) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots){
                    String currentUsername = queryDocumentSnapshot.get("username").toString();
                    String currentUserID = queryDocumentSnapshot.getId();
                    db.collection("users").document(queryDocumentSnapshot.getId()).collection("friends").whereEqualTo("username", friendUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {
                                String friendID = queryDocumentSnapshot.getId();
                                db.collection("users").document(currentUserID).collection("friends").document(friendID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        db.collection("users").document(queryDocumentSnapshot.getId()).collection("friends").document(friendID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                db.collection("users").whereEqualTo("username", friendUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                                            String friendUserID = queryDocumentSnapshot.getId();
                                                            db.collection("users").document(friendUserID).collection("friends").whereEqualTo("username", currentUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                                                        db.collection("users").document(friendUserID).collection("friends").document(queryDocumentSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                db.collection("users").document(currentUserID).collection("friends paths").whereEqualTo("user", friendUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                                                                            db.collection("users").document(currentUserID).collection("friends paths").document(queryDocumentSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void unused) {
                                                                                                    db.collection("users").document(friendUserID).collection("friends paths").whereEqualTo("user", currentUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                                                                                                db.collection("users").document(friendUserID).collection("friends paths").document(queryDocumentSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Void unused) {

                                                                                                                        Toast.makeText(context, "All deleted", Toast.LENGTH_SHORT).show();

                                                                                                                        Intent broadcastIntentDeletePaths = new Intent();
                                                                                                                        broadcastIntentDeletePaths.setAction("DELETE_PATHS");
                                                                                                                        context.sendBroadcast(broadcastIntentDeletePaths);

                                                                                                                        Intent broadcastIntentDeleteFriends = new Intent();
                                                                                                                        broadcastIntentDeleteFriends.setAction("UPDATE_CONTACTS");
                                                                                                                        context.sendBroadcast(broadcastIntentDeleteFriends);

                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            });
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
                                        });
                                    }
                                });
                            }
                        }
                    });
                };
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList1.size();
    }

    public void add(Model newItem, int position){
        itemList1.add(newItem);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Button itemButton;
        TextView itemUsernameText;
        RelativeLayout relativeLayout;
        int drawable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemButton=itemView.findViewById(R.id.addFriendButton);
            itemUsernameText=itemView.findViewById(R.id.usernameText);
            relativeLayout =itemView.findViewById(R.id.layout_id);
            itemButton.setClickable(true);
            itemButton.setText("delete");
            itemButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24,0,0,0);
            /*
            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO:need to add buttons for contacts like sharing paths, deleting contacts, may need to make new item layout
                    //globalQuery = itemUsernameText.getText().toString();
                    //addFriend(itemButton, itemUsernameText);
                    //itemButton.setClickable(false);
                }
            });
            */

        }
    }

    private void addFriend(Button itemButton, TextView itemUsernameText) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        //gets current users username and adds to friend request collection
        Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
        currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {

                    Map<String, Object> currentUserName = new HashMap<String, Object>();
                    currentUserName.put("username", (querySnapshot.get("username")));

                    Query query = users.whereEqualTo("username", globalQuery);
                    query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            boolean found = false;
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                users.document(querySnapshot.getId()).collection("friend requests").add(currentUserName);
                                found = true;
                                itemButton.setText("Requested");
                                itemButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_done_24,0,0,0);
                            }
                            if(!found){
                                Toast.makeText(context,"error", Toast.LENGTH_SHORT).show();
                            };
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Couldn't access data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
//TODO:need to set to requested if is requested and already friends if friends when button created