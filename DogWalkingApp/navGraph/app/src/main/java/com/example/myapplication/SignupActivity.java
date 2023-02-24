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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class SignupActivity extends AppCompatActivity {
//TODO: big fixes needed, day 12 in log
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    int bothwork;
    private Button button;

    private EditText usernameField;
    private EditText passwordField;
    private EditText fullNameField;
    private EditText emailField;
    private String username;
    private String password;
    private String fullName;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        button = findViewById(R.id.signupButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameField = findViewById(R.id.signupUsername);
                passwordField = findViewById(R.id.signupPassword);
                emailField = findViewById(R.id.signupEmail);
                fullNameField = findViewById(R.id.signupFullName);

                username = usernameField.getText().toString();
                password = passwordField.getText().toString();
                email = emailField.getText().toString();
                fullName = fullNameField.getText().toString();

                //do not add password to firestore
                Map<String, Object> user = new HashMap<>();
                user.put("username", username);
                user.put("fullname", fullName);
                user.put("email", email);

                if (password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$") ){
                    checkUserOrEmailExist(user);
                }else{
                    Toast.makeText(getApplicationContext(),"Password Must Contain a Uppercase Letter, a Lowercase Letter and a Number and be at Least 8 characters long", Toast.LENGTH_SHORT).show();
                }



            }
        });
    }


    public void checkUserOrEmailExist(Map user){
        db.collection("users").whereEqualTo("username", user.get("username")).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                boolean usernameAlreadyExists = false;
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    usernameAlreadyExists = true;
                    Toast.makeText(getApplicationContext(), "Username Already Exists", Toast.LENGTH_SHORT).show();
                }
                if (usernameAlreadyExists == false) {
                    db.collection("users").whereEqualTo("email", user.get("email")).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            boolean emailAlreadyExists = false;
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                Toast.makeText(getApplicationContext(), "Email Already Exists", Toast.LENGTH_SHORT).show();
                                emailAlreadyExists = true;
                            }
                            if (emailAlreadyExists == false) {
                                addFirestore(user);
                            }
                        }
                    });
                }

            }
        });
    }

    public void addFirestore(Map user) {
        db.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getApplicationContext(), "Successfully added", Toast.LENGTH_SHORT).show();
                addAuth(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Sign up failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addAuth(Map user){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    mAuth.signOut();
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(SignupActivity.this, "Authentication failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
