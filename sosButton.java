package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class sosButton extends AppCompatActivity {
    ImageButton Sos;
    private FusedLocationProviderClient client;
    private final int REQUEST_SEND_SMS = 2;
    private final int REQUEST_SMS_PERMISSION=3;
    private LocationSettingsRequest.Builder builder;
    LocationManager locationManager;
    String x = "", y = "";
    private List<String> emergencyContacts = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    String phoneNumber;

    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sos_button);
        Sos = findViewById(R.id.sos);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Fetch contacts when the activity is created
        fetchEmergencyContacts();
        Sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);//Will take permission to access location from the system
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    onGPS();
                } else {
                    startTrack();
                }
            }
        });

    }

    private void fetchEmergencyContacts() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userReference = databaseReference.child("users").child(userId).child("emergency_contact");

            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    emergencyContacts.clear(); // Clear the list to avoid duplicates
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        phoneNumber = snapshot.child("phone_number").getValue(String.class);
                        if (phoneNumber != null) {
                            emergencyContacts.add(phoneNumber); // Add valid phone numbers to the list
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(sosButton.this, "Error fetching contacts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void startTrack() {
        if (ActivityCompat.checkSelfPermission(sosButton.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(sosButton.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }else{
            client = LocationServices.getFusedLocationProviderClient(this);
            client.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location locationGps) {
                            if(locationGps!=null){
                                double lat = locationGps.getLatitude();
                                double longi = locationGps.getLongitude();
                                x = String.valueOf(lat);
                                y = String.valueOf(longi);
                                String locationUrl = "https://www.google.com/maps?q="+lat+","+longi;
                                String message = "Emergency ! My location is : "+locationUrl;
                                sendSmstoConatacts(message);
                            }
                            else{
                                Toast.makeText(sosButton.this, "Unable to find location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private void sendSmstoConatacts(String message) {


            if (emergencyContacts.isEmpty()) {
                Toast.makeText(this, "No emergency contacts available.", Toast.LENGTH_SHORT).show();
                return;
            }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
            return; // Exit until permission is granted
        }

        SmsManager smsManager = SmsManager.getDefault();
        for (String contact : emergencyContacts) {
            if (contact != null && !contact.isEmpty()) { // Check for null or empty
                try {
                    Log.d("SMS", "Sending SMS to: " + contact);
                    smsManager.sendTextMessage(contact, null, message, null, null);
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to send SMS to " + contact + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid phone number: " + contact, Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(this, "Emergency alert sent!", Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void onGPS() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }
