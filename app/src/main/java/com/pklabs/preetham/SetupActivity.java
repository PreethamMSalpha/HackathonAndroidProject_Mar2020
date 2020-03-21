package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName,FullName,CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserId;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImage");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_fullname);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInfo();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("ProfileImages")) {
                        String image = dataSnapshot.child("ProfileImages").getValue().toString();
                        //using picaso library
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                        //Glide.with(getApplication()).load(R.drawable.profile).into(ProfileImage);
                     }
                else{
                        Toast.makeText(SetupActivity.this, "please select profile image first.", Toast.LENGTH_SHORT).show();
                        Log.i("profile image error", "please select profile image first.");
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data!= null){
            Uri ImageUri = data.getData();
            //for cropping image after importing dependencies, manifest files
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we're updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                final StorageReference filepath = UserProfileImageRef.child(currentUserId +".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if (task.isSuccessful()){
                           Toast.makeText(SetupActivity.this, "Profile Image stored to Database successfully", Toast.LENGTH_SHORT).show();
                           Log.i("profile image error 1","Profile Image stored to Database successfully");
                            //final String downloadUrl = task.getResult().getTask().toString(); //error might occur here 18.56_14
                           final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                           UsersRef.child("ProfileImages").setValue(downloadUrl)
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                               startActivity(selfIntent);

                                               Toast.makeText(SetupActivity.this, "Profile image stored to Database successfully!!!", Toast.LENGTH_SHORT).show();
                                               Log.i("profile image error 2","Profile Image stored to Database successfully");

                                               loadingBar.dismiss();
                                           }else{
                                               String message = task.getException().getMessage();
                                               Toast.makeText(SetupActivity.this, "Error occured "+message, Toast.LENGTH_SHORT).show();
                                               Log.i("profile image error3","Profile Image stored to Database successfully");

                                               loadingBar.dismiss();
                                           }
                                       }
                                   });
                       }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


//                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                       if (task.isSuccessful()){
//                           Toast.makeText(SetupActivity.this, "Profile Image stored to Database successfully", Toast.LENGTH_SHORT).show();
//                            //final String downloadUrl = task.getResult().getTask().toString(); //error might occur here 18.56_14
//                           final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//
//                           UsersRef.child("ProfileImages").setValue(downloadUrl)
//                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                       @Override
//                                       public void onComplete(@NonNull Task<Void> task) {
//                                           if (task.isSuccessful()){
//                                               Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
//                                               startActivity(selfIntent);
//
//                                               Toast.makeText(SetupActivity.this, "Profile image stored to Database successfully!!!", Toast.LENGTH_SHORT).show();
//                                               loadingBar.dismiss();
//                                           }else{
//                                               String message = task.getException().getMessage();
//                                               Toast.makeText(SetupActivity.this, "Error occured "+message, Toast.LENGTH_SHORT).show();
//                                               loadingBar.dismiss();
//                                           }
//                                       }
//                                   });
//                       }
//                    }
//                });
//

    private void SaveAccountSetupInfo() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String countryname = CountryName.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Username required...", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Fullname required...", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(countryname)){
            Toast.makeText(this, "Countryname required...", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Saving Information...");
            loadingBar.setMessage("Please wait, while we're saving information...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("Username", username);
            userMap.put("Fullname", fullname);
            userMap.put("Country", countryname);
            userMap.put("Status", "hey there...");
            userMap.put("Gender", "none");
            userMap.put("DOB", "none");
            userMap.put("RelationshipStatus", "none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Information updated!!!", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
