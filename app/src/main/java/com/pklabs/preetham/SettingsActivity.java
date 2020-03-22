package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userProfName, userStatus,  userCountry, userGender;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText)findViewById(R.id.settings_username);
        userProfName = (EditText)findViewById(R.id.settings_profile_fullname);
        userStatus = (EditText)findViewById(R.id.settings_status);
        userCountry = (EditText)findViewById(R.id.settings_country);
        userGender = (EditText)findViewById(R.id.settings_gender);

        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);

        updateAccountSettingsButton = (Button) findViewById(R.id.update_account_settings_button);

        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUserName = dataSnapshot.child("userName").getValue().toString();
                    String myProfName = dataSnapshot.child("fullName").getValue().toString();
                    String myProfStatus = dataSnapshot.child("status").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();

                    Picasso.get().load(myProfImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfName);
                    userStatus.setText(myProfStatus);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

    }

    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();

        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(profilename) && TextUtils.isEmpty(status) && TextUtils.isEmpty(country) && TextUtils.isEmpty(gender)){
            Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
        }else{
            UpdateAccountInformation(username, profilename, status, country, gender);

        }
    }

    private void UpdateAccountInformation(String username, String profilename, String status, String country, String gender) {

        HashMap userMap = new HashMap();
        userMap.put("userName", username);
        userMap.put("fullName", profilename);
        userMap.put("status", status);
        userMap.put("country", country);
        userMap.put("gender", gender);
        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Updated successfully!!!", Toast.LENGTH_SHORT).show();
                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
