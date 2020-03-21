package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName,FullName,CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


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
    }

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
