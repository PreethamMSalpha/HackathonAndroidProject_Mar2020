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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;

    private static final int Gallery_Pick = 1;

    private Uri ImageUri;
    private String description;
    private ProgressDialog loadingBar;

    private StorageReference PostsImagesRef;
    private DatabaseReference UsersRef, PostRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, DownloadUrl, currentUserId;

    private Uri resultUri;

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
        description = PostDescription.getText().toString();
        if (ImageUri == null){
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(description)){
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
        Calendar calForTime = Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());
        postRandomName = saveCurrentDate+saveCurrentTime;

        final StorageReference filepath = PostsImagesRef.child(ImageUri.getLastPathSegment()+postRandomName+".jpg");

        filepath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        loadingBar.dismiss();
                        DownloadUrl = uri.toString();
                        Toast.makeText(PostActivity.this,"Image uploaded",Toast.LENGTH_SHORT).show();

                        SavingPostInformationToDatabase();
                    }
                });
            }
        });
    }


//    private void StoringImageToFirebaseStorage() {
//        Calendar calForDate = Calendar.getInstance();
//        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
//        saveCurrentDate = currentDate.format(calForDate.getTime());
//
////        Calendar calForTime = Calendar.getInstance();
////        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
////        saveCurrentTime = currentTime.format(calForTime.getTime());
//          saveCurrentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
//
//        //same images with same names uploaded by the users wont be replaced
//        postRandomName = saveCurrentDate + saveCurrentTime;
//
//        final StorageReference filePath = PostsImagesRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
//        //...................................
////        loadingBar.setTitle("Saving Information...");
////        loadingBar.setMessage("Please wait, while we're saving information...");
////        loadingBar.show();
////        loadingBar.setCanceledOnTouchOutside(true);
////
////        if (resultUri != null) {
////
////
////            //final StorageReference filePath = PostsImagesRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
////            Bitmap bitmap = null;
////
////            try {
////                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), resultUri);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////
////            ByteArrayOutputStream baos = new ByteArrayOutputStream();
////            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
////            byte[] data = baos.toByteArray();
////            UploadTask uploadTask = filePath.putBytes(data);
////            uploadTask.addOnFailureListener(new OnFailureListener() {
////                @Override
////                public void onFailure(@NonNull Exception e) {
////                    finish();
////                }
////            });
////
////
////            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                @Override
////                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                        @Override
////                        public void onSuccess(Uri uri) {
////                            loadingBar.dismiss();
////
////                            Map postMap = new HashMap();
////                            postMap.put("postImage", uri.toString());
////                            PostRef.updateChildren(postMap);
////                            SavingPostInformationToDatabase();
////                            finish();
////                            return;
////                        }
////                    }).addOnFailureListener(new OnFailureListener() {
////                        @Override
////                        public void onFailure(@NonNull Exception e) {
////                            loadingBar.dismiss();
////                            finish();
////                            return;
////                        }
////                    });
////                }
////
////            });
////
////        } else {
////            finish();
////        }
//
//        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()){
//                    DownloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//                    Toast.makeText(PostActivity.this, "Image uploaded successfully to Storage", Toast.LENGTH_SHORT).show();
//
//                    SavingPostInformationToDatabase();
//                }else{
//                    String message = task.getException().getMessage();
//                    Toast.makeText(PostActivity.this, "Error occured: "+message, Toast.LENGTH_SHORT).show();
//                    Log.i("Error", "Error");
//                }
//            }
//        });
//    }

    //-------------------------------------------------------
    private void SavingPostInformationToDatabase() {

        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String userFullName = dataSnapshot.child("fullName").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileImage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", currentUserId);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", description);
                    postMap.put("postImage", DownloadUrl);
                    postMap.put("profileImage", userProfileImage);
                    postMap.put("fullName", userFullName);
                    PostRef.child(currentUserId+postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        SendUserToMainActivity();
                                        Toast.makeText(PostActivity.this,"Uploaded Post!",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else{
                                        Toast.makeText(PostActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();
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
    //------------------------------------------------------
//    private void SavingPostInformationToDatabase() {
//        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    String userFullName = dataSnapshot.child("fullName").getValue().toString();
//                    String userProfileImage = dataSnapshot.child("profileImage").getValue().toString();
//
//                    HashMap postMap = new HashMap();
//                    postMap.put("uid", currentUserId);
//                    postMap.put("date", saveCurrentDate);
//                    postMap.put("time", saveCurrentTime);
//                    postMap.put("description", description);
//                    postMap.put("postImage", DownloadUrl);
//                    postMap.put("profileImage", userProfileImage);
//                    postMap.put("fullName", userFullName);
//                    PostRef.child(currentUserId + postRandomName).updateChildren(postMap)
//                            .addOnCompleteListener(new OnCompleteListener() {
//                                @Override
//                                public void onComplete(@NonNull Task task) {
//                                    if (task.isSuccessful()){
//                                        loadingBar.dismiss();
//                                        SendUserToMainActivity();
//                                        Toast.makeText(PostActivity.this, "New post is updated successfully", Toast.LENGTH_SHORT).show();
//                                    }else{
//                                        Toast.makeText(PostActivity.this, "Error occured while uploading", Toast.LENGTH_SHORT).show();
//                                        loadingBar.dismiss();
//                                    }
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode== Activity.RESULT_OK) {

            Uri ImageUri = data.getData();
            //for cropping image after importing dependencies, manifest files
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(3, 4)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                final Uri resultUri = result.getUri();
                ImageUri = resultUri;
                SelectPostImage.setImageURI(ImageUri);

            }
        }

////        //displaying selected image
//        if (requestCode == Gallery_Pick && resultCode == RESULT_OK){
//            ImageUri = data.getData();
//            SelectPostImage.setImageURI(ImageUri);
//        }
 //dont refer this code
//        if (requestCode == Gallery_Pick && resultCode == RESULT_OK){
//            final Uri ImageUri = data.getData();
//            resultUri = ImageUri;
//            SelectPostImage.setImageURI(resultUri);
//        }
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
