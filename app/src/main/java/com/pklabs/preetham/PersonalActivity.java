package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PersonalRef;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PersonalRef = FirebaseDatabase.getInstance().getReference().child("Personal").child(currentUserId);

        mToolbar = (Toolbar) findViewById(R.id.main_app_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Personal Board");

        AddNewPostButton = (ImageButton)findViewById(R.id.add_new_post_button);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(PersonalActivity.this, drawerLayout, R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        //actionBarDrawerToggle.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView)findViewById(R.id.navigation_view);

        postList = (RecyclerView)findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        //for navigation bar info update
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("fullName")){
                        String fullname = dataSnapshot.child(("fullName")).getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileImage")){
                        String image = dataSnapshot.child(("profileImage")).getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }else{
                        Toast.makeText(PersonalActivity.this, "Profile name doesn't exsist", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

        DisplayPersonalPosts();

    }

    private void DisplayPersonalPosts() {

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(PersonalRef, Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts, PersonalActivity.PostsVieHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, PersonalActivity.PostsVieHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull PersonalActivity.PostsVieHolder holder, int position, @NonNull Posts model) {

                holder.username.setText(model.getFullName());
                holder.PostTime.setText("   " + model.getTime());
                holder.PostDate.setText("   " +model.getDate());
                holder.PostDueDate.setText("   "+model.getDueDate());
                holder.PostDescription.setText(model.description);
                Picasso.get().load(model.getProfileImage()).into(holder.image);
                Picasso.get().load(model.getPostImage()).into(holder.PostImage);

            }

            @NonNull
            @Override
            public PersonalActivity.PostsVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_personal_layout, parent, false);
                PersonalActivity.PostsVieHolder viewHolder = new PersonalActivity.PostsVieHolder(view);
                return viewHolder;
            }
        };

        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class PostsVieHolder extends RecyclerView.ViewHolder{
        TextView username, PostDescription, PostTime, PostDate, PostDueDate;
        CircleImageView image;
        ImageView PostImage;

        public PostsVieHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.personal_user_name);
            PostDescription = itemView.findViewById(R.id.personal_post_description);
            image  = itemView.findViewById(R.id.personal_profile_image);
            PostTime = itemView.findViewById(R.id.personal_post_time);
            PostDate = itemView.findViewById(R.id.personal_post_date);
            PostDueDate = itemView.findViewById(R.id.personal_post_dueDate);
            PostImage =  itemView.findViewById(R.id.personal_post_image);

        }



    }

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(PersonalActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser == null){
//            SendUserToLoginActivity();
//        }else{
//            CheckUserExistence();
//        }
//        DisplayPersonalPosts();
//
//    }

//    private void CheckUserExistence() {
//        final String currentUserId = mAuth.getCurrentUser().getUid();
//        UsersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.hasChild(currentUserId)){
//                    SendUserToSetupActivity();
//                }else{
//                    DisplayPersonalPosts();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        }) ;
//
//    }

//    private void SendUserToSetupActivity() {
//        Intent setupIntent = new Intent(PersonalActivity.this, SetupActivityDemo.class);
//        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(setupIntent);
//        finish();
//    }
//
    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(PersonalActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuItem) {
        switch(menuItem.getItemId()){
            case R.id.nav_post:
                SendUserToPostActivity(); break;
            case R.id.nav_personalBoardPosts:
                SendUserToPersonalActivity();
                Toast.makeText(this, "Personal Posts", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_teamBoardPosts:
                SendUserToMainActivity(); break;
            case R.id.nav_settings:
                SendUserToSettingsActivity(); break;
            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

        }
    }

    private void SendUserToPersonalActivity() {
        Intent selfIntent = new Intent(PersonalActivity.this, PersonalActivity.class);
        startActivity(selfIntent);
    }

    private void SendUserToMainActivity() {
        Intent selfIntent = new Intent(PersonalActivity.this, MainActivity.class);
        startActivity(selfIntent);
    }

    private void SendUserToSettingsActivity() {

        Intent settingsIntent = new Intent(PersonalActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }



}
