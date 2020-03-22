package com.pklabs.preetham;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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

    }
}
