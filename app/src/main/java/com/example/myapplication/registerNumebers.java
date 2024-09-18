package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.view.View;

public class registerNumebers extends AppCompatActivity {

    EditText number1,number2,number3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_numebers);

        number1 = findViewById(R.id.phonen1);
        number2 = findViewById(R.id.phonen2);
        number3 = findViewById(R.id.phonen3);
    }
    public void saveNumber(){
        String numString1 = number1.getText().toString();
        String numString2 = number2.getText().toString();
        String numString3 = number3.getText().toString();

        if(numString1.length() == 10 && numString2.length() == 10 && numString3.length() == 10){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        }


    }

}