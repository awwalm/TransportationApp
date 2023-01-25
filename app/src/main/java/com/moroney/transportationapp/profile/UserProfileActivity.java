package com.moroney.transportationapp.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.moroney.transportationapp.FeedbackActivity;
import com.moroney.transportationapp.R;
import com.moroney.transportationapp.SignedInActivity;

import java.util.HashMap;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity
{

    // Firebase variables
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseUser currentUser;
    String plainEmail;
    DatabaseReference mainRef;
    DatabaseReference userRef;

    EditText
    editGroupCodeEditText,
    editGenderEditText,
    editFavoriteCityEditText,
    editNameEditText,
    editEmailEditText;

    FloatingActionButton
    fromProfileHomeFab,
    fromProfileNavigateFab,
    fromProfileSaveFab,
    submitNewFeedbackFab;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.user_profile));

        // initialize Firebase variables
        // strip all special characters to create unique username string
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        plainEmail = currentUser.getEmail().replaceAll("[-+.@^:,]","");
        mainRef = FirebaseDatabase.getInstance().getReference();
        userRef = mainRef.child("Users").child(plainEmail);

        // initialize EditTexts and buttons
        editNameEditText = findViewById(R.id.editNameEditText);
        editEmailEditText = findViewById(R.id.editEmailEditText);
        editGroupCodeEditText = findViewById(R.id.editGroupCodeEditText);
        editGenderEditText = findViewById(R.id.editGenderEditText);
        editFavoriteCityEditText = findViewById(R.id.editFavoriteCityEditText);
        fromProfileHomeFab = findViewById(R.id.fromProfileHomeFab);
        fromProfileNavigateFab = findViewById(R.id.fromProfileNavigateFab);
        fromProfileSaveFab = findViewById(R.id.fromProfileSaveFab);
        submitNewFeedbackFab = findViewById(R.id.submitNewFeedbackFab);

        try    // attempt to set the fields by retrieving details from Firebase
        {
            userRef.addValueEventListener(new ValueEventListener() {
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
                       editNameEditText.setText(currentUser.getDisplayName());
                       editEmailEditText.setText(currentUser.getEmail());
                       editGroupCodeEditText.setText(userMap.get("groupCode"));
                       editFavoriteCityEditText.setText(userMap.get("favoriteCity"));
                       editGenderEditText.setText(userMap.get("gender"));

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


        // set onclick listener when user presses save button
        fromProfileSaveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userRef.child("userID").setValue(plainEmail);
                userRef.child("name").setValue(currentUser.getDisplayName());
                userRef.child("email").setValue(currentUser.getEmail());

                userRef.child("groupCode").setValue(String.valueOf(editGroupCodeEditText.getText()));
                userRef.child("gender").setValue(String.valueOf(editGenderEditText.getText()));
                userRef.child("favoriteCity").setValue(String.valueOf(editFavoriteCityEditText.getText()));

                Snackbar.make(v, R.string.profile_updated, Snackbar.LENGTH_LONG).show();

            }
        });


        fromProfileHomeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goHome(v);
            }
        });

        submitNewFeedbackFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFeedback(v);
            }
        });


    }


    // refresh this page itself
    public void goHome(View view)
    {
        Intent intent = new Intent(this, SignedInActivity.class);
        startActivity(intent);
    }

    // go to feedbacks
    public void goToFeedback(View view)
    {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }

}
