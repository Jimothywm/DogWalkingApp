package com.example.myapplication;



import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.fragments.PathsFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ItemAdapterPaths extends RecyclerView.Adapter<ItemAdapterPaths.ViewHolder> {

    FirebaseAuth mAuth;
    List<ModelPath> itemList1;
    private Context context;
    String globalQuery;
    LinkedList<LatLng> pointsLL;


    public ItemAdapterPaths(List<ModelPath> itemList, Context context) {
        this.itemList1 = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemAdapterPaths.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paths_rowitem, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapterPaths.ViewHolder holder, final int position) {

        holder.pathText.setText(itemList1.get(position).getName());
        if(itemList1.get(position).getFriendName() == "null"){
            String pathID = itemList1.get(position).ID;
            changeImage(holder.pathImageView, holder.itemView, pathID);
            holder.renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                    View pathPopupView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.pathpopup, null);
                    ImageView imagePopup = pathPopupView.findViewById(R.id.pathImageViewPopup);
                    EditText editPathName = pathPopupView.findViewById(R.id.editPathNamePopup);

                    imagePopup.setImageBitmap(((BitmapDrawable)holder.pathImageView.getDrawable()).getBitmap());
                    Button backButtonPathPopup = pathPopupView.findViewById(R.id.backButtonPathPopup);
                    builder.setView(pathPopupView);
                    AlertDialog alertDialog = builder.create();
                    backButtonPathPopup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    Button submitPopupButton = pathPopupView.findViewById(R.id.submitButtonPopup);
                    submitPopupButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAuth = FirebaseAuth.getInstance();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            CollectionReference users = db.collection("users");
                            //gets current users username and adds to friend request collection
                            Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
                            currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) { // has gotten user
                                    for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                        String currentUserID = querySnapshot.getId().toString();
                                        HashMap<String, Object> newName = new HashMap<>();
                                        newName.put("path name", editPathName.getText().toString());
                                        db.collection("users").document(currentUserID).collection("paths").document(pathID).update(newName).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                ((PathsFragment) FragmentManager.findFragment(holder.itemView)).RefreshRecycler();
                                                Toast.makeText(context, "new name set", Toast.LENGTH_SHORT);
                                            }
                                        });
                                    }
                                }
                            });
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            });


            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context.getApplicationContext(), "yes", Toast.LENGTH_SHORT).show();

                    deleteButtonFunction(pathID);
                    itemList1.remove(holder.getPosition());
                    notifyDataSetChanged();
                }
            });
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bitmap bitmap = ((BitmapDrawable) holder.pathImageView.getDrawable()).getBitmap();
                    ((PathsFragment) FragmentManager.findFragment(holder.itemView)).addPopup(pathID, bitmap);

                    //((PathsFragment)getSupportFragmentManager().findFragmentByTag("pathFragment"))
                /*friendRequestButton = view.findViewById(R.id.friendRequestButton);
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
                });*/
                }
            });
        }else {
            changeFriendImage(holder.pathImageView, holder.itemView, itemList1.get(position).ID, itemList1.get(position).getFriendName());
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context.getApplicationContext(), "yes", Toast.LENGTH_SHORT).show();

                    deleteFriendButtonFunction(itemList1.get(holder.getAdapterPosition()).getFriendName());
                    itemList1.remove(holder.getPosition());
                    notifyDataSetChanged();
                }
            });
            holder.shareButton.setVisibility(View.GONE);
            holder.renameButton.setVisibility(View.GONE);
            holder.nameOfFriendText.setText(itemList1.get(position).getFriendName());
        }

    }

    private void deleteFriendButtonFunction(String friendName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("email", mAuth.getCurrentUser().getEmail().toString()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots){
                    db.collection("users").document(queryDocumentSnapshot.getId()).collection("friends paths").whereEqualTo("user", friendName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot queryDocumentSnapshot2: queryDocumentSnapshots){
                                db.collection("users").document(queryDocumentSnapshot.getId()).collection("friends paths").document(queryDocumentSnapshot2.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void renameButtonFunction(String pathID, String name) {

    }

    @Override
    public int getItemCount() {
        return itemList1.size();
    }

    public void add(ModelPath newItem, int position) {
        itemList1.add(newItem);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button shareButton;
        Button deleteButton;
        Button renameButton;
        TextView pathText;
        TextView nameOfFriendText;
        RelativeLayout relativeLayout;
        ImageView pathImageView;
        int drawable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shareButton = itemView.findViewById(R.id.shareButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            renameButton = itemView.findViewById(R.id.renameButton);
            pathText = itemView.findViewById(R.id.pathText);
            nameOfFriendText = itemView.findViewById(R.id.nameOfFriend);
            pathImageView = itemView.findViewById(R.id.pathImageView);
            relativeLayout = itemView.findViewById(R.id.paths_layout_id);

        }
    }

    private void deleteButtonFunction(String pathID) {

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        //gets current users username and adds to friend request collection
        Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
        currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) { // has gotten user
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    String currentUserID = querySnapshot.getId().toString();
                    db.collection("users").document(currentUserID).collection("paths").document(pathID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //successfully deleted
                        }
                    });
                    db.collection("users").document(currentUserID).collection("path images").document(pathID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //successfully deleted
                        }
                    });
                }
            }
        });
    }

    private void changeImage(ImageView pathImageView, View itemView, String pathID) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        //gets current users username and adds to friend request collection
        Query currentUserNameQuery = users.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
        currentUserNameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) { // has gotten user
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    String currentUserID = querySnapshot.getId().toString();
                    users.document(currentUserID).collection("path images").document(pathID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) { // has gotten path image
                                String pathID = documentSnapshot.getId();
                                String bitMapString = documentSnapshot.get("path image").toString();
                                byte[] imageBytes = Base64.decode(bitMapString, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                pathImageView.setImageBitmap(bitmap);
                                users.document(currentUserID).collection("paths").document(pathID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        ArrayList<LatLng> pointsLLTemp = new ArrayList<LatLng>();
                                        List<Object> objectList = (List<Object>) documentSnapshot.get("path");
                                        pointsLLTemp = (ArrayList<LatLng>) documentSnapshot.get("path");
                                        LinkedList<LatLng> pointsLL = new LinkedList<LatLng>();

                                        for (Object objectParse: objectList){
                                            HashMap<String, Double> hashPoints = (HashMap<String, Double>)objectParse;
                                            LatLng point = new LatLng((double)hashPoints.get("latitude"), (double)hashPoints.get("longitude"));
                                            pointsLL.add(point);
                                        }
                                        pathImageView.setOnClickListener(new View.OnClickListener() {
                                                                          @Override
                                                                          public void onClick(View v) {
                                                                              ((PathsFragment) FragmentManager.findFragment(itemView)).PassPointsLL(pointsLL);
                                                                              ((MapsActivity)((PathsFragment) FragmentManager.findFragment(itemView)).getActivity()).clickHome();
                                                                              ///globalQuery = pathID.getText().toString();
                                                                              ///shareButtonFunction(itemButton, pathID);
                                                                              ///itemButton.setClickable(false);

                                                                              //Fragment homeFragment = ((FragmentActivity) itemView.getContext()).getSupportFragmentManager().findFragmentByTag("homeFragment");
                                                                              //((FragmentActivity) itemView.getContext()).findViewById(R.id.map)
                                                                              //((FragmentActivity) itemView.getContext()).getSupportFragmentManager().beginTransaction().remove(homeFragment).commit();
                                                                              //Fragment newHomeFragment = new HomeFragment();

                                                                              //((FragmentActivity) itemView.getContext()).getSupportFragmentManager().beginTransaction().add(R.id.fl_wrapper, newHomeFragment, "home fragment").commit();
                                                                              //TODO:this breaks the when Toast tries to get content of original home fragment - might be issue with permissions or looper
                                                                          }
                                                                        });

                                        /*
                                        relativeLayout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //getActivity()

                                            }


                                        });
                                        */
                                    }
                                });
                            }
                    });
                }
            }
        });
    }

    private void changeFriendImage(ImageView pathImageView, View itemView, String pathID, String friendName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("username", friendName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    db.collection("users").document(queryDocumentSnapshot.getId()).collection("path images").document(pathID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String pathID = documentSnapshot.getId();
                            String bitMapString = documentSnapshot.get("path image").toString();
                            byte[] imageBytes = Base64.decode(bitMapString, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            pathImageView.setImageBitmap(bitmap);
                            db.collection("users").document(queryDocumentSnapshot.getId()).collection("paths").document(pathID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    ArrayList<LatLng> pointsLLTemp = new ArrayList<LatLng>();
                                    List<Object> objectList = (List<Object>) documentSnapshot.get("path");
                                    pointsLLTemp = (ArrayList<LatLng>) documentSnapshot.get("path");
                                    LinkedList<LatLng> pointsLL = new LinkedList<LatLng>();

                                    for (Object objectParse : objectList) {
                                        HashMap<String, Double> hashPoints = (HashMap<String, Double>) objectParse;
                                        LatLng point = new LatLng((double) hashPoints.get("latitude"), (double) hashPoints.get("longitude"));
                                        pointsLL.add(point);
                                    }
                                    pathImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ((PathsFragment) FragmentManager.findFragment(itemView)).PassPointsLL(pointsLL);
                                            ((MapsActivity) ((PathsFragment) FragmentManager.findFragment(itemView)).getActivity()).clickHome();
                                            ///globalQuery = pathID.getText().toString();
                                            ///shareButtonFunction(itemButton, pathID);
                                            ///itemButton.setClickable(false);

                                            //Fragment homeFragment = ((FragmentActivity) itemView.getContext()).getSupportFragmentManager().findFragmentByTag("homeFragment");
                                            //((FragmentActivity) itemView.getContext()).findViewById(R.id.map)
                                            //((FragmentActivity) itemView.getContext()).getSupportFragmentManager().beginTransaction().remove(homeFragment).commit();
                                            //Fragment newHomeFragment = new HomeFragment();

                                            //((FragmentActivity) itemView.getContext()).getSupportFragmentManager().beginTransaction().add(R.id.fl_wrapper, newHomeFragment, "home fragment").commit();
                                            //TODO:this breaks the when Toast tries to get content of original home fragment - might be issue with permissions or looper
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

//TODO:need to set to requested if is requested and already friends if friends when button created