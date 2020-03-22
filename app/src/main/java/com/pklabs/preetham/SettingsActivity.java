package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userProfName, userStatus,  userCountry, userGender;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;
    private ProgressDialog loadingBar;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;

    private String currentUserId;

    final static int Gallery_Pick = 1;

    private Uri resultUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImage");
        loadingBar = new ProgressDialog(this);

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

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode== Activity.RESULT_OK) {

            Uri ImageUri = data.getData();
            //for cropping image after importing dependencies, manifest files
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {


                final Uri imageUri = result.getUri();
                resultUri = imageUri;
                userProfImage.setImageURI(resultUri);
            }
        }

    }

    private void ValidateAccountInfo() {

        loadingBar.setTitle("Saving Information...");
        loadingBar.setMessage("Please wait, while we're saving information...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

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
                    loadingBar.dismiss();
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Updated successfully!!!", Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.dismiss();
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (resultUri != null) {


            final StorageReference filepath = UserProfileImageRef.child(currentUserId + ".jpg");
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            loadingBar.dismiss();

                            Map userInfo = new HashMap();
                            userInfo.put("profileImage", uri.toString());
                            settingsUserRef.updateChildren(userInfo);
                            SendUserToMainActivity();
                            finish();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingBar.dismiss();
                            finish();
                            return;
                        }
                    });
                }

            });

        } else {
            finish();
        }



    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }



}
