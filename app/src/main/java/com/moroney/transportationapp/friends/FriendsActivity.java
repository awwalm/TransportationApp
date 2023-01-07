package com.moroney.transportationapp.friends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moroney.transportationapp.R;
import com.moroney.transportationapp.database.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class FriendsActivity extends AppCompatActivity
{
    String TICKET_ID;
    String GET_DESTINATION, GET_ORIGIN;
    String GROUP_CODE;
    DatabaseReference userRef, currentUserRef;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private RecyclerView friendsRecyclerView;
    private FirebaseRecyclerOptions<User> friendsOptions;
    private FirebaseRecyclerAdapter<User, FriendsViewHolder> friendsAdapter;
    private LinearLayoutManager friendsLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.friends));


        try
        {
            // obtain ticket ID from bundle
            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("SHARE_TRIP_DATA");
            if ( (bundle != null) & !bundle.isEmpty() )
            {
                TICKET_ID = bundle.getString("SHARE_TICKET_ID", null);
                GET_DESTINATION = bundle.getString("GET_DESTINATION", null);
                GET_ORIGIN = bundle.getString("GET_ORIGIN", null);
                Toast.makeText(this, TICKET_ID, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        String currentUserID = currentUser.getEmail().replaceAll("[-+.@^:,]","");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        currentUserRef = userRef.child(currentUserID);

        // collect current user's group code
        try     // attempt to set the fields by retrieving details from Firebase
        {
            currentUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    HashMap<String, String> userMap = new HashMap<>();

                    for (DataSnapshot dataValues : dataSnapshot.getChildren())
                    {
                        userMap.put(dataValues.getKey(), String.valueOf(dataValues.getValue()));
                    }

                    try
                    {
                        GROUP_CODE = userMap.get("groupCode");
                        Log.d("GROUP_CODE", GROUP_CODE);
                    }
                    catch (NullPointerException npe)
                    {
                        npe.getMessage();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {   }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



        friendsLayoutManager = new LinearLayoutManager(this);
        friendsLayoutManager.setReverseLayout(true);
        friendsLayoutManager.setStackFromEnd(true);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setLayoutManager(friendsLayoutManager);

        friendsOptions = new FirebaseRecyclerOptions
                .Builder<User>()
                .setQuery(userRef, User.class)
                .build();

        friendsAdapter = new FirebaseRecyclerAdapter<User, FriendsViewHolder>(friendsOptions)
        {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull User model)
            {


                try {
                    // if users belong to the same group code, they can see each other
                    if (model.getGroupCode().equals(GROUP_CODE) && !model.getEmail().equals(currentUser.getEmail()))
                    {


                        // on click action
                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                if ( (TICKET_ID != null) && !(TICKET_ID.isEmpty()) )
                                {
                                    Toast.makeText(FriendsActivity.this, R.string.trip_sharing_detected, Toast.LENGTH_SHORT).show();

                                    // share the ticket with the correspondent
                                    // @TODO: add confirmation alert dialog
                                    // generate reference to the notifications node
                                    DatabaseReference notificationRef =
                                            FirebaseDatabase.getInstance().getReference().child("Notifications");
                                    String senderUserId =
                                            currentUser.getEmail().replaceAll("[-+.@^:,]", "");
                                    String receiverUserId =
                                            model.getEmail().replaceAll("[-+.@^:,]", "");
                                    String senderExactName =
                                            currentUser.getDisplayName();
                                    String senderExactEmail =
                                            currentUser.getEmail();

                                    // create HashMap object for notification database insertions
                                    HashMap<String, String> tripNotificationMap = new HashMap<>();
                                    // declare the use case of the HashMap
                                    tripNotificationMap.put("from", senderUserId);
                                    tripNotificationMap.put("fromExactNameEmail", senderExactName + " (" + senderExactEmail + ")");
                                    tripNotificationMap.put("type", "newtrip");
                                    tripNotificationMap.put("ticketID", TICKET_ID);
                                    tripNotificationMap.put("origin", GET_ORIGIN);
                                    tripNotificationMap.put("destination", GET_DESTINATION);
                                    tripNotificationMap.put("dateTime", String.valueOf(new Date(System.currentTimeMillis())));
                                    // structure the notification insertion with genuine push IDs
                                    notificationRef.child(receiverUserId)
                                            .push()
                                            .setValue(tripNotificationMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        Toast.makeText(FriendsActivity.this,
                                                                String.format("%s %s", model.getName(), getResources().getString(R.string.notify_new_shared_ticket)),
                                                                Toast.LENGTH_SHORT).show();
                                                        finish(); // close the activity
                                                    }
                                                }
                                            });
                                }
                                else
                                {
                                    Toast.makeText(FriendsActivity.this, R.string.trip_sharing_not_detected, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });




                        holder.textViewForFriendsName.setText(model.getName());
                        holder.textViewForFriendsEmail.setText(model.getEmail());
                        holder.textViewForFriendsGender.setText(model.getGender());
                        holder.textViewForFriendsGroupCode.setText(model.getGroupCode());
                        holder.textViewForFriendsFavoriteCity.setText(model.getFavoriteCity());
                    }
                    else
                    {
                        holder.textViewForFriendsName.setVisibility(View.GONE);
                        holder.textViewForFriendsEmail.setVisibility(View.GONE);
                        holder.textViewForFriendsGender.setVisibility(View.GONE);
                        holder.textViewForFriendsGroupCode.setVisibility(View.GONE);
                        holder.textViewForFriendsFavoriteCity.setVisibility(View.GONE);
                        holder.friendsRVDivider.setVisibility(View.GONE);

                        holder.friendsRelativeLayout.setVisibility(View.GONE);

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.friends_single_view_layout, parent, false);

                return new FriendsViewHolder(view);
            }
        };

        friendsAdapter.startListening();
        friendsRecyclerView.setAdapter(friendsAdapter);


    }




}
