package com.pklabs.preetham;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
    private DatabaseReference UsersRef, PostsRef, LikesRef;

    String currentUserId;
    Boolean LikeChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

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

                     final String PostKey = getRef(position).getKey();

                      holder.username.setText(model.getFullName());
                      holder.PostTime.setText("   " + model.getTime());
                      holder.PostDate.setText("   " +model.getDate());
                      holder.PostDueDate.setText("   "+model.getDueDate());
                      holder.PostDescription.setText(model.description);

                      holder.setLikeButtonStatus(PostKey);

                      Picasso.get().load(model.getProfileImage()).into(holder.image);
                      Picasso.get().load(model.getPostImage()).into(holder.PostImage);

                      holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                                LikeChecker = true;
                              Log.i("checkbox:", "clicked");

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (LikeChecker.equals(true)){

                                                if (dataSnapshot.child(PostKey).hasChild(currentUserId)){
                                                    LikesRef.child(PostKey).child(currentUserId).removeValue();
                                                    LikeChecker = false;

                                                }else{
                                                    LikesRef.child(PostKey).child(currentUserId).setValue(true);
                                                    LikeChecker = false;
                                                }

                                            }
                                        }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                          }
                      });

            }

            @NonNull
            @Override
            public PostsVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                PostsVieHolder viewHolder = new PostsVieHolder(view);
                return viewHolder;
            }
        };

        //new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(fi);
        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

   }

    public static class PostsVieHolder extends RecyclerView.ViewHolder{
        TextView username, PostDescription, PostTime, PostDate, PostDueDate;
        CircleImageView image;
        ImageView PostImage;

        ImageButton LikePostButton;
        TextView DisplayNoOfLikes;

        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostsVieHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.personal_user_name);
            PostDescription = itemView.findViewById(R.id.personal_post_description);
            image  = itemView.findViewById(R.id.personal_profile_image);
            PostTime = itemView.findViewById(R.id.personal_post_time);
            PostDate = itemView.findViewById(R.id.personal_post_date);
            PostDueDate = itemView.findViewById(R.id.personal_post_dueDate);
            PostImage =  itemView.findViewById(R.id.personal_post_image);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);
            LikePostButton = itemView.findViewById(R.id.like_button);



            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        }

        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)){

                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.tick);
                        DisplayNoOfLikes.setText("Total Checks : " + Integer.toString(countLikes));

                    }else{
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.untick);
                        DisplayNoOfLikes.setText("Total Checks : " + Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


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
            case R.id.nav_personalBoardPosts:
                SendUserToPersonalActivity(); break;
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
        Intent selfIntent = new Intent(MainActivity.this, PersonalActivity.class);
        startActivity(selfIntent);
    }

    private void SendUserToMainActivity() {
        Intent selfIntent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(selfIntent);
    }

    private void SendUserToSettingsActivity() {

        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

}
