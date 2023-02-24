package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private Button logOnButton;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private EditText usernameField;
    private EditText passwordField;
    private String username;
    private String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        logOnButton = findViewById(R.id.logOnButton);
        logOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameField = findViewById(R.id.username);
                passwordField = findViewById(R.id.password);
                username = usernameField.getText().toString();
                password = passwordField.getText().toString();
                if(username.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Need to enter password and email", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            }else{
                                Toast.makeText(getApplicationContext(),"Email or password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                }
            }
        });
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void updateUI(FirebaseUser account) {
        if (account != null) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
    }




}
