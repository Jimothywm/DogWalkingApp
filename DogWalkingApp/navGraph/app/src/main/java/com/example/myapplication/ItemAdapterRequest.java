package com.example.myapplication;


import android.content.Context;
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

import com.example.myapplication.fragments.friendRequestsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapterRequest extends RecyclerView.Adapter<ItemAdapterRequest.ViewHolder> {

    FirebaseAuth mAuth;
    List<Model> itemList1;
    private Context context;
    String globalQuery;

    public ItemAdapterRequest(List<Model> itemList, Context context) {

        this.itemList1=itemList;
        this.context=context;

    }

    @NonNull
    @Override
    public ItemAdapterRequest.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.rowitem,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapterRequest.ViewHolder holder, final int position) {


        holder.itemUsernameText.setText(itemList1.get(position).getName());


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
            itemButton.setText("confirm friend");
            itemUsernameText=itemView.findViewById(R.id.usernameText);
            relativeLayout =itemView.findViewById(R.id.layout_id);

            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    globalQuery = itemUsernameText.getText().toString();
                    addFriendRequest(new FirebaseCallback() {
                                         @Override
                                         public void onResponse() {
                                             itemButton.setText("accepted");
                                             itemButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_done_24,0,0,0);
                                             itemButton.setClickable(false);

                                             Intent broadcastIntent = new Intent();
                                             broadcastIntent.setAction("UPDATE_CONTACTS");
                                             context.sendBroadcast(broadcastIntent);
                                         }
                                     });
                }
            });
        }
    }

    private void addFriendRequest(FirebaseCallback firebaseCallback) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        //gets current user
        Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
        currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    Map<String, Object> currentUser = querySnapshot.getData();
                    String currentUserId = querySnapshot.getId();
                    //goes into current users friend requests
                    users.document(currentUserId).collection("friend requests").whereEqualTo("username", globalQuery).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Map<String,Object> friendRequest = querySnapshot.getData();
                                String friendRequestId = querySnapshot.getId();
                                //adds user to friends document
                                users.document(currentUserId).collection("friends").add(friendRequest).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        //on success delete from current users friend requests
                                        users.document(currentUserId).collection("friend requests").document(friendRequestId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //on success add to friends friends and delete from their friend requests
                                                //first get user that is new friend
                                                users.whereEqualTo("username", friendRequest.get("username").toString()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                                            //then add current user to friend
                                                            String friendUserId = querySnapshot.getId();
                                                            Map<String, Object> currentUserName = new HashMap<String, Object>();
                                                            currentUserName.put("username", currentUser.get("username"));
                                                            users.document(friendUserId).collection("friends").add(currentUserName);
                                                            Toast.makeText(context, "successfully added friend", Toast.LENGTH_SHORT).show();
                                                            firebaseCallback.onResponse();
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

                }
            }
        });
    }
    public interface FirebaseCallback {
        void onResponse();
    }
}
