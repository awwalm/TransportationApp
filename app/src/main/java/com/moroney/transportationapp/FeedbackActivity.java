package com.moroney.transportationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class FeedbackActivity extends AppCompatActivity
{
    // Firebase variables
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseUser currentUser;
    String plainEmail;
    DatabaseReference mainRef;
    DatabaseReference userRef;
    DatabaseReference feedbackRef;

    TextInputEditText submitFeedbackEditText;
    Button submitFeedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.lets_hear_from_you));

        // initialize Firebase variables
        // strip all special characters to create unique username string
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        plainEmail = currentUser.getEmail().replaceAll("[-+.@^:,]","");
        mainRef = FirebaseDatabase.getInstance().getReference();
        userRef = mainRef.child("Users").child(plainEmail);
        feedbackRef = mainRef.child("Feedbacks").push();


        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);
        submitFeedbackEditText = findViewById(R.id.submitFeedbackEditText);

        // check if user has already submitted feedback
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
                        if (userMap.get("hasSubmittedFeedback").equals(String.valueOf(true)))
                        {
                            submitFeedbackButton.setEnabled(false);
                            submitFeedbackEditText.setEnabled(false);
                            submitFeedbackEditText.setText(R.string.reviewing_feedback);
                            submitFeedbackEditText.setTypeface(Typeface.DEFAULT_BOLD);
                        }
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


        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String feedback = submitFeedbackEditText.getText().toString();
                if (!feedback.isEmpty() && (feedback.length() >= 10))
                {
                    String senderExactName = currentUser.getDisplayName();
                    String senderExactEmail = currentUser.getEmail();
                    HashMap<String, String> feedbackMap = new HashMap<>();
                    feedbackMap.put("dateSubmitted", String.valueOf(new Date(System.currentTimeMillis())));
                    feedbackMap.put("submittedBy", senderExactName + " (" + senderExactEmail + ")");
                    feedbackMap.put("message", feedback);

                    feedbackRef
                        .setValue(feedbackMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                userRef.child("hasSubmittedFeedback").setValue(String.valueOf(true));
                                Toast.makeText(FeedbackActivity.this, R.string.feedback_submitted, Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(FeedbackActivity.this, android.R.string.httpErrorBadUrl, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    finish(); // end the activity
                }
                else
                {
                    Toast.makeText(FeedbackActivity.this, R.string.feedback_too_short, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
