package com.example.myapplication;

import android.content.Context;
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

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    FirebaseAuth mAuth;
    List<Model> itemList1;
    private Context context;
    String globalQuery;

    public ItemAdapter(List<Model> itemList,Context context) {

        this.itemList1=itemList;
        this.context=context;

    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.rowitem,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, final int position) {


        holder.itemUsernameText.setText(itemList1.get(position).getName());
        holder.itemButton.setClickable(false);
        holder.itemButton.setText("Waiting...");
        checkRequestedAlready(holder.itemButton, holder.itemUsernameText.getText().toString());

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
            itemButton.setText("Add Friend");

            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    globalQuery = itemUsernameText.getText().toString();
                    addFriend(itemButton, itemUsernameText);
                    itemButton.setClickable(false);
                }
            });
        }
    }

    private void checkRequestedAlready(Button itemButton, String requestUsername){
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        //gets current users username and adds to friend request collection
        Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
        currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    String currentUsername = querySnapshot.get("username").toString();
                    users.whereEqualTo("username", requestUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                users.document(querySnapshot.getId()).collection("friend requests").whereEqualTo("username", currentUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        boolean flag = false;
                                        for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                            itemButton.setText("Requested");
                                            itemButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_done_24, 0, 0, 0);
                                            itemButton.setClickable(false);
                                            flag = true;
                                        }
                                        if(flag == false){
                                            itemButton.setText("Add Friend");
                                            itemButton.setClickable(true);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
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