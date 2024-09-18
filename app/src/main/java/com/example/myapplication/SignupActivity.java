package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText SignUp_username,SignUp_password,Signup_email;
    Button Signup_button;
    TextView loginRedirectText;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        SignUp_username = findViewById(R.id.Signupname);
        SignUp_password = findViewById(R.id.custom_editpassword);
        Signup_email = findViewById(R.id.emailId);
        Signup_button = findViewById(R.id.SigninButton);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        Signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");
                String username = SignUp_username.getText().toString().trim();
                String password = SignUp_password.getText().toString().trim();
                String email = Signup_email.getText().toString();

                HelperClass helperClass = new HelperClass(username,email,password);
                String usernameID = reference.push().getKey();
                reference.child(usernameID).setValue(helperClass);

                Toast.makeText(SignupActivity.this, "You have SignUp successfully.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


    }
}