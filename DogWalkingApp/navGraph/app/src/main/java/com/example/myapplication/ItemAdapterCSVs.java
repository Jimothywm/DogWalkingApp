package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

public class ItemAdapterCSVs extends RecyclerView.Adapter<ItemAdapterCSVs.ViewHolder> {

    FirebaseAuth mAuth;
    List<CSVModel> itemList1;
    private Context context;
    String globalQuery;

    public ItemAdapterCSVs(List<CSVModel> itemList, Context context) {

        this.itemList1=itemList;
        this.context=context;

    }

    @NonNull
    @Override
    public ItemAdapterCSVs.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.rowitemcsv,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapterCSVs.ViewHolder holder, final int position) {

        holder.filenameText.setText(itemList1.get(position).getName());
        holder.checkBox.setChecked(itemList1.get(position).isChecked());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                itemList1.get(holder.getAdapterPosition()).setChecked(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList1.size();
    }

    public List<CSVModel> getItemList1() {
        return itemList1;
    }

    public void add(CSVModel newItem, int position){
        itemList1.add(newItem);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView filenameText;
        RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            filenameText = itemView.findViewById(R.id.filenameText);
            checkBox = itemView.findViewById(R.id.CSVCheckbox);
            //if checkbox is checked in users csv collection check it here
            //get filename from same check
        }
    }

}
//TODO:need to set to requested if is requested and already friends if friends when button created