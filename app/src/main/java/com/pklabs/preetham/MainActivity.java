package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
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

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.main_app_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = (ImageButton)findViewById(R.id.add_new_post_button);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open,R.string.drawer_close);
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
                        Toast.makeText(MainActivity.this, "Profile name doesn't exsist", Toast.LENGTH_SHORT).show();
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

        DisplayAllUserSPosts();

    }

    private void DisplayAllUserSPosts() {

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                    .setQuery(PostsRef, Posts.class)
                    .build();

        FirebaseRecyclerAdapter<Posts, PostsVieHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, PostsVieHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull PostsVieHolder holder, int position, @NonNull Posts model) {

                      holder.username.setText(model.getFullName());
                      holder.PostTime.setText("   " + model.getTime());
                      holder.PostDate.setText("   " +model.getDate());
                      holder.PostDescription.setText(model.description);
                      Picasso.get().load(model.getProfileImage()).into(holder.image);
                      Picasso.get().load(model.getPostImage()).into(holder.PostImage);

//                    holder.setFullName(model.getFullName());
//                    holder.setTime(model.getTime());
//                    holder.setDate(model.getDate());
//                    holder.setDescription(model.getDescription());
//                    holder.setProfileImage(getApplicationContext(), model.getProfileImage());
//                    holder.setPostImage(getApplicationContext(), model.getPostImage());
            }

            @NonNull
            @Override
            public PostsVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                PostsVieHolder viewHolder = new PostsVieHolder(view);
                return viewHolder;
            }
        };

        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

   }

    public static class PostsVieHolder extends RecyclerView.ViewHolder{
        TextView username, PostDescription, PostTime, PostDate;
        CircleImageView image;
        ImageView PostImage;

        public PostsVieHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.post_user_name);
            PostDescription = itemView.findViewById(R.id.post_description);
            image  = itemView.findViewById(R.id.post_profile_image);
            PostTime = itemView.findViewById(R.id.post_time);
            PostDate = itemView.findViewById(R.id.post_date);
            PostImage =  itemView.findViewById(R.id.post_image);

        }

//        public void setFullName(String fullName){
//            TextView username = itemView.findViewById(R.id.post_user_name);
//            username.setText(fullName);
//        }
//
//        public void setProfileImage(Context ctx, String profileImage){
//            CircleImageView image =  itemView.findViewById(R.id.post_profile_image);
//            Picasso.get().load(profileImage).into(image);
//        }
//
//        public void setTime(String time){
//            TextView PostTime = itemView.findViewById(R.id.post_time);
//            PostTime.setText("   " + time);
//        }
//
//        public void setDate(String date){
//            TextView PostDate = itemView.findViewById(R.id.post_date);
//            PostDate.setText("   " + date);
//        }
//
//        public void setDescription(String description){
//            TextView PostDescription = itemView.findViewById(R.id.post_description);
//            PostDescription.setText("  "+description);
//        }
//
//        public void setPostImage(Context ctx, String postImage){
//            ImageView PostImage =  itemView.findViewById(R.id.post_image);
//            Picasso.get().load(postImage).into(PostImage);
//        }

    }

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            SendUserToLoginActivity();
        }else{
            CheckUserExistence();
        }
        DisplayAllUserSPosts();

    }

    private void CheckUserExistence() {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUserId)){
                    SendUserToSetupActivity();
                }else{
                    DisplayAllUserSPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivityDemo.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
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
            case R.id.nav_profile:
                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_home:
                Toast.makeText(this, "home", Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_friends:
                Toast.makeText(this, "friends", Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_find_friends:
                Toast.makeText(this, "find friends", Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_messages:
                Toast.makeText(this, "messages", Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_settings:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

        }
    }

}
