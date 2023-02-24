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
import androidx.appcompat.view.menu.MenuView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.fragments.PathsFragment;
import com.example.myapplication.fragments.ShareFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapterShare extends RecyclerView.Adapter<ItemAdapterShare.ViewHolder> {

    FirebaseAuth mAuth;
    List<ModelShare> itemList1;
    private Context context;
    String globalQuery;

    public ItemAdapterShare(List<ModelShare> itemList, Context context) {

        this.itemList1=itemList;
        this.context=context;

    }

    @NonNull
    @Override
    public ItemAdapterShare.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.rowitem,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapterShare.ViewHolder holder, final int position) {


        holder.itemUsernameText.setText(itemList1.get(position).getName());


        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots) {
                    String currentUsername = queryDocumentSnapshot.get("username").toString();
                    db.collection("users").whereEqualTo("username", holder.itemUsernameText.getText()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                String friendUserDocID = queryDocumentSnapshot.getId();
                                db.collection("users").document(friendUserDocID).collection("friends paths").whereEqualTo("path", itemList1.get(holder.getAdapterPosition()).getPathID()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                            itemList1.get(holder.getAdapterPosition()).setSent(true);
                                            holder.itemButton.setText("Sent");
                                            holder.itemButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_done_24, 0, 0, 0);
                                            holder.itemButton.setClickable(false);
                                        }
                                        if(!itemList1.get(holder.getAdapterPosition()).sent){
                                            holder.itemButton.setText("Share");
                                            holder.itemButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_send_24, 0);
                                            holder.itemButton.setClickable(true);
                                            holder.itemButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //sharePath(holder.itemUsernameText.getText().toString(), holder.itemView);
                                                    itemList1.get(holder.getAdapterPosition()).setSent(true);
                                                    holder.itemButton.setText("Sent");
                                                    holder.itemButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_done_24, 0, 0, 0);
                                                    holder.itemButton.setClickable(false);
                                                    ((ShareFragment) FragmentManager.findFragment(holder.itemView)).sharePath(holder.itemUsernameText.getText().toString());

                                                }
                                            });
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

    private void sharePath(String friendUsername, View itemView) {


    }

    @Override
    public int getItemCount() {
        return itemList1.size();
    }

    public void add(ModelShare newItem, int position){
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
            itemButton.setClickable(false);
            itemButton.setText("Waiting...");
            itemButton.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_send_24,0);

        }
    }
}
//TODO:need to set to requested if is requested and already friends if friends when button created