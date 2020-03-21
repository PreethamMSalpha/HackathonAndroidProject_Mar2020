package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;

    private static final int Gallery_Pick = 1;

    private Uri ImageUri;
    private String Description;
    private ProgressDialog loadingBar;

    private StorageReference PostsImagesRef;
    private DatabaseReference UsersRef, PostRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, DownloadUrl, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        PostsImagesRef = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        SelectPostImage = (ImageButton)findViewById(R.id.select_post_image);
        UpdatePostButton = (Button) findViewById(R.id.update_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {
        Description = PostDescription.getText().toString();
        if (ImageUri == null){
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Description about post is needed", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait, while we're updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

//        Calendar calForTime = Calendar.getInstance();
//        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
//        saveCurrentTime = currentTime.format(calForTime.getTime());
          saveCurrentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //same images with same names uploaded by the users wont be replaced
        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostsImagesRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    DownloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully to Storage", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();
                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured: "+message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationToDatabase() {
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("Fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("ProfileImages").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("UId", currentUserId);
                    postMap.put("Date", saveCurrentDate);
                    postMap.put("Time", saveCurrentTime);
                    postMap.put("Description", Description);
                    postMap.put("PostImage", DownloadUrl);
                    postMap.put("ProfileImage", userProfileImage);
                    postMap.put("Fullname", userFullName);
                    PostRef.child(currentUserId + postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        SendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "New post is updated successfully", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(PostActivity.this, "Error occured while uploading", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //displaying selected image
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data!= null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
