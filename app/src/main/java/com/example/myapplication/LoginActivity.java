package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button loginButton;
    TextView SignupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.LoginUsername);
        loginPassword = findViewById(R.id.Loginpassword);
        loginButton = findViewById(R.id.LoginButton);
        SignupRedirectText = findViewById(R.id.signinRedirectText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(!validateUsername() | !validatePassword()){

            }
            else {
                checkUser();
            }
            }
        });

        SignupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });
    }

        public Boolean validateUsername () {
            String val = loginUsername.getText().toString();

            if (val.isEmpty()) {
                loginUsername.setError("Username cannot be empty");
                return false;
            } else {
                loginUsername.setError(null);
                return true;
            }
        }

        public Boolean validatePassword () {
            String val = loginPassword.getText().toString();

            if (val.isEmpty()) {
                loginPassword.setError("Password cannot be empty");
                return false;
            } else {
                loginPassword.setError(null);
                return true;
            }
        }


        public void checkUser() {
            String userUsername = loginUsername.getText().toString().trim();
            String userPassword = loginPassword.getText().toString().trim();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("LoginActivity", "Query path: username");
                    Log.d("LoginActivity", "Query username: " + userUsername);
                    Log.d("LoginActivity", "snapshot exists: " + snapshot.exists());



                    if (snapshot.exists()) {
                        loginUsername.setError(null);
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String passwordfromDB = userSnapshot.child("password").getValue(String.class);

                            if (passwordfromDB != null && passwordfromDB.equals(userPassword)) {
                                loginUsername.setError(null);
                                Intent intent = new Intent(LoginActivity.this, registerNumebers.class);
                                startActivity(intent);
                            } else {
                                loginPassword.setError("Invalid Credentials");
                                loginPassword.requestFocus();
                            }
                        }

                        } else{
                            loginUsername.setError("User does not exist");
                            loginUsername.requestFocus();
                        }

                }



                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            {

            }


        }
    }
