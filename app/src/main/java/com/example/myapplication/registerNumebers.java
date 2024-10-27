package com.example.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class registerNumebers extends AppCompatActivity {
    private static final int REQUEST_CODE_CONTACTS = 2;
    private ListView contactListView;
    private ArrayList<String> emergencycontactList = new ArrayList<>();
    private ArrayList<String> emergencycontactName = new ArrayList<>();
    private Button selectContacts, SubmitContact;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_numebers);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        selectContacts = findViewById(R.id.addButton);
        SubmitContact = findViewById(R.id.SubmitContact);
        contactListView = findViewById(R.id.list_item);


        selectContacts.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(registerNumebers.this, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                selectContacts();
            } else {
                ActivityCompat.requestPermissions(registerNumebers.this, new String[]{
                                Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_CONTACTS);
            }
        });

        SubmitContact.setOnClickListener(view -> {
            if (emergencycontactList.size() == 3) {
                saveContactToFirebase();
                Intent intent = new Intent(registerNumebers.this, sosButton.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select only 3 emergency contact!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to get a list of contacts for the adapter
    private List<HashMap<String, String>> getContactList() {
        List<HashMap<String, String>> listItems = new ArrayList<>();
        for (int i = 0; i < emergencycontactList.size(); i++) {
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("First line", emergencycontactName.get(i));  // Name of the contact
            resultMap.put("Second line", emergencycontactList.get(i));  // Phone number of the contact
            listItems.add(resultMap);
        }
        return listItems;
    }

    private void saveContactToFirebase() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userReference = databaseReference.child("users").child(userId).child("emergency_contact");

            for (int i = 0; i < emergencycontactList.size(); i++) {
                String contactId = "contact" + (i + 1);
                userReference.child(contactId).child("name").setValue(emergencycontactName.get(i));
                userReference.child(contactId).child("phone_number").setValue(emergencycontactList.get(i));
            }
            Toast.makeText(this, "Emergency contact is saved.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        intent.putExtra(intent.EXTRA_ALLOW_MULTIPLE, true);
       startActivityForResult(intent, REQUEST_CODE_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CONTACTS && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri contactUri = data.getClipData().getItemAt(i).getUri();

                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(contactUri, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                        if (nameIndex >= 0 && numberIndex >= 0) {
                            String contactName = cursor.getString(nameIndex);
                            String contactNumber = cursor.getString(numberIndex);

                            emergencycontactList.add(contactNumber);
                            emergencycontactName.add(contactName);
                        }
                        cursor.close();
                    }
                }
            } else if (data.getData() != null) {
                Uri contactUri = data.getData();
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(contactUri, null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    if (nameIndex >= 0 && numberIndex >= 0) {
                        String contactName = cursor.getString(nameIndex);
                        String contactNumber = cursor.getString(numberIndex);
                        emergencycontactList.add(contactNumber);
                        emergencycontactName.add(contactName);
                    }
                    cursor.close();
                }
            }
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    getContactList(),
                    R.layout.contact_details,
                    new String[]{"First line", "Second line"}, // The keys for data
                    new int[]{R.id.contactName, R.id.contactPhoneNum} // The IDs of the TextViews to bind data to
            );
            contactListView.setAdapter(adapter);
            Toast.makeText(this, "Emergency contacts updated.", Toast.LENGTH_SHORT).show();
        }
    }
}
