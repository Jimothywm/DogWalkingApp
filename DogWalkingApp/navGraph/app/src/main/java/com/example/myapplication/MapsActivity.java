package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.myapplication.fragments.CSVFragment;
import com.example.myapplication.fragments.ContactsFragment;
import com.example.myapplication.fragments.HomeFragment;
import com.example.myapplication.fragments.PathsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.type.LatLng;

import java.util.LinkedList;

public class MapsActivity extends AppCompatActivity{


    public void updateRequestRecycler(){
        PathsFragment pathsFragment2 = ((PathsFragment)getSupportFragmentManager().findFragmentByTag("pathFragment"));
        pathsFragment2.RefreshRecycler();
        bottomNavigationView.getMenu().performIdentifierAction(R.id.ic_paths, 0);
    }

    public void PassPointsLL(LinkedList<com.google.android.gms.maps.model.LatLng> pointsLL) {
        HomeFragment homeFragment2 = ((HomeFragment)getSupportFragmentManager().findFragmentByTag("homeFragment"));
        homeFragment2.PassPointsLL(pointsLL);
    }

    public void clickHome(){
        bottomNavigationView.getMenu().performIdentifierAction(R.id.ic_map, 0);
    }

    enum frag{HOME, CONTACTS, PATHS, CSV}
    frag myFrag;
    private boolean home;
    private BottomNavigationView bottomNavigationView;
    private LinkedList<LatLng> pointsLL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.ic_map);


        HomeFragment homeFragment = new HomeFragment();
        Fragment contactsFragment = new ContactsFragment();
        Fragment pathsFragment = new PathsFragment();
        Fragment CSVFragment = new CSVFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fl_wrapper, CSVFragment).hide(CSVFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_wrapper, contactsFragment).hide(contactsFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_wrapper, pathsFragment, "pathFragment").hide(pathsFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_wrapper, homeFragment, "homeFragment").commit();
        getSupportFragmentManager().beginTransaction().show(homeFragment).commit();


        myFrag = frag.HOME;
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (myFrag){
                    case HOME:
                        getSupportFragmentManager().beginTransaction().hide(homeFragment).commit();
                    case PATHS:
                        getSupportFragmentManager().beginTransaction().hide(pathsFragment).commit();
                    case CONTACTS:
                        getSupportFragmentManager().beginTransaction().hide(contactsFragment).commit();
                    case CSV:
                        getSupportFragmentManager().beginTransaction().hide(CSVFragment).commit();
                }
                Fragment fragment = null;
                switch(item.getItemId()){
                    case R.id.ic_map:
                        fragment = homeFragment;
                        myFrag = frag.HOME;
                        break;
                    case R.id.ic_contacts:
                        fragment = contactsFragment;
                        myFrag = frag.CONTACTS;
                        break;
                    case R.id.ic_paths:
                        fragment = pathsFragment;
                        myFrag = frag.PATHS;
                        break;
                    case R.id.ic_CSVs:
                        fragment = CSVFragment;
                        myFrag = frag.CSV;
                        break;

                }
                getSupportFragmentManager().beginTransaction().show(fragment).commit();
                return true;
            }
        });
    }
}