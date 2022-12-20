package com.moroney.transportationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moroney.transportationapp.friends.FriendsActivity;
import com.moroney.transportationapp.notifications.NotificationsActivity;
import com.moroney.transportationapp.profile.UserProfileActivity;
import com.moroney.transportationapp.records.TripRecordsActivity;
import com.moroney.transportationapp.schedule.TripScheduleActivity;
import com.moroney.transportationapp.startnew.NewTripActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SignedInActivity extends AppCompatActivity
{
    TextView userEmail;
    TextView userName;
    private Button signOutButton;
    //private Toast toast = Toast.makeText(this, "SignOutFailed", Toast.LENGTH_LONG);

    private Button tripScheduleButton;
    private Button tripRecordsButton;
    private Button userProfileButton;
    private Button newTripButton;
    private Button mapsButton;
    private Button mapsButton2;

    private FloatingActionButton friendsFab, profileFab, notificationsFab;

    private final int CAMERA_REQUEST = 1; // for camera action
    private ImageView profilePicture;

    // Firebase variables
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference usersRef;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    // strip all special characters to create unique username string
    String plainEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // check to see if the user is valid, if not, then go back to where you came from
        if (currentUser == null)
        {
            startActivity(MainActivity.createIntent(this));
            finish();
            return;
        }

        plainEmail = currentUser.getEmail().replaceAll("[-+.@^:,]","");

        userEmail = (TextView) findViewById(R.id.user_email);
        userName = (TextView) findViewById(R.id.user_display_name);

        // reads from the database, but only some credentials
        userEmail.setText(currentUser.getEmail()); // uses predefined methods to get user email
        userName.setText(currentUser.getDisplayName()); // as above, this gets username

        signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(signOutListener);

        // go to TripSchedule activity
        tripScheduleButton = findViewById(R.id.tripScheduleButton);
        tripScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goToTripSchedule(v);
            }
        });

        // go to TripRecords activity
        tripRecordsButton = findViewById(R.id.tripRecordsButton);
        tripRecordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goToTripRecords(v);
            }
        });

        // go to UserProfile activity
        userProfileButton = findViewById(R.id.userProfileButton);
        profileFab = findViewById(R.id.profileFab);
        profileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                gotoUserProfile(v);
            }
        });

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                gotoUserProfile(v);
            }
        });

        // go to NewTrip
        newTripButton = findViewById(R.id.newTripButton);
        newTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                gotoNewTrip(v);
            }
        });


        mapsButton = findViewById(R.id.mapsButton);
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goToMaps(v);
            }
        });

        // go to Mapbox activity
        mapsButton2 = findViewById(R.id.mapsButton2);
        mapsButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMapbox(v);
            }
        });

        // open friends page
        friendsFab = findViewById(R.id.friendsFab);
        friendsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFriends(v);
            }
        });


        notificationsFab = findViewById(R.id.notificationsFab);
        notificationsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNotifications(v);
            }
        });



        // attempt to load profile picture
        profilePicture = findViewById(R.id.profilePicture);
        try // load the profile image
        {
            FirebaseAuth auth;
            FirebaseDatabase database;
            DatabaseReference mainRef;
            FirebaseStorage storage;
            StorageReference storageRef;

            profilePicture = (ImageView) findViewById(R.id.profilePicture);
            // immediately attempt to load the profile picture on start
            downloadPicture();
            // set onclick listener upon tapping the photo
            profilePicture.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // inform the user the function might not work on old devices
                    Toast.makeText(SignedInActivity.this, R.string.old_device_warning, Toast.LENGTH_SHORT).show();
                    // take the picture with camera and upload
                    takePictureAndUpload();
                    // download the picture and display
                    downloadPicture();
                }
            });

        }
        catch (Exception e)
        {
            Log.d("PROFILE_PIC_LOAD", e.toString());
        } /*finish profile picture loading*/

        profilePicture.setRotation(90);


    } /**end {@link #onCreate(Bundle)}*/






    // method for taking photo
    private void takePictureAndUpload()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // startActivityForResult(cameraIntent, Integer.parseInt(CAMERA_SERVICE));
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }


    // method for retrieving photo
    private void downloadPicture()
    {
        // initialize storage
        storage = FirebaseStorage.getInstance();
        // and get a reference to it
        storageRef = storage.getReference();
        // make the reference extend by or to a child node
        StorageReference imageRef = storageRef.child("dp")
                .child(plainEmail)
                .child("image.jpg");

        try
        {
            final File localFile = File.createTempFile("image", "jpg");
            // getFile is an asynchronous operation so it needs a callback monitor
            imageRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                        {   // Local file has been created, create a bitmap image
                            Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            // load the bitmap image onto the ImageView
                            try
                            {
                                // profilePicture = (ImageView) view.findViewById(R.id.profilePicture);
                                profilePicture.setImageBitmap(myBitmap);
                            }
                            catch (NullPointerException npe)
                            {
                                //
                            }

                        }
                        // in case of failure, also add a callback monitor
                    }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    String foo = e.getLocalizedMessage();
                    e.printStackTrace();
                    Log.w("FETCH_IMAGE_fail", "TROUBLE FETCHING IMAGE", e);
                }
            });
        }
        catch (IOException ioe)
        {
            ioe.getLocalizedMessage();
            ioe.printStackTrace();
            Log.w("FETCH_IMAGE_io", "TROUBLE FETCHING IMAGE", ioe);
        }
        catch (Exception ex)
        {
            String foo = ex.getLocalizedMessage();
            ex.printStackTrace();
            Log.w("FETCH_IMAGE_ex", "TROUBLE FETCHING IMAGE", ex);
        }

    }/**end {@link #downloadPicture()} method*/


    /** method for getting photo operation done ... the {@param data} field contains the picture*/
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try
        {
            if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK))
            {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageData = stream.toByteArray();

                storage = FirebaseStorage.getInstance();
                // this becomes the child node for the location of the image
                storageRef = storage.getReference();
                // send the image to the database by unprofessionally hard-coding the title
                StorageReference imageRef = storageRef.child("dp")
                        .child(plainEmail)
                        .child("image.jpg");
                usersRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users");

                // call a putBytes method that handles the file upload asynchronously
                // ...the upload is done byte by byte
                UploadTask uploadTask = imageRef.putBytes(imageData);
                // add onFailure listener to catch any errors if upload fails
                uploadTask.addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String ex = e.getLocalizedMessage();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        Uri downloadUrl = urlTask.getResult();

                        // send the photo URL to Firebase
                        usersRef.child(plainEmail).child("dpUrl").setValue(downloadUrl.toString());

                        Toast.makeText(
                            SignedInActivity.this, R.string.dp_uploaded, Toast.LENGTH_SHORT).show();
                        refresh(getCurrentFocus());
                        // Toast.makeText(
                        //      getContext(), downloadUrl.toString(), Toast.LENGTH_LONG).show();
                        // deprecated alternate procedure
                        /*StorageMetadata urlMetadata = taskSnapshot.getMetadata();
                        StorageReference urlReference = urlMetadata.getReference();
                        Task urlTask = urlReference.getDownloadUrl();
                        */
                    }
                });
            }

        } catch (RuntimeException re)   // if RuntimeException occurs, device can't perform this
        {
            re.printStackTrace();
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
        }
    }


    // go to NotificationsActivity
    public void goToNotifications(View view)
    {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }


    // go to FriendsActivity
    public void goToFriends(View view)
    {
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
    }

    // refresh this page itself
    public void refresh(View view)
    {
        Intent intent = new Intent(this, SignedInActivity.class);
        startActivity(intent);
    }

    // go to MapboxActivity
    public void goToMapbox(View view)
    {
        Intent intent = new Intent(this, MapboxActivity.class);
        startActivity(intent);
    }

    // go to MapsActivity
    public void goToMaps(View view)
    {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    // go to NewTrip
    public void gotoNewTrip(View view)
    {
        Intent intent = new Intent(this, NewTripActivity.class);
        startActivity(intent);
    }

    // go to UserProfile
    public  void gotoUserProfile(View view)
    {
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }

    // go to TripRecords
    public void goToTripRecords(View view)
    {
        Intent intent = new Intent(this, TripRecordsActivity.class);
        startActivity(intent);
    }

    // go to TripSchedule
    public void goToTripSchedule(View view)
    {
        Intent intent = new Intent(this, TripScheduleActivity.class);
        startActivity(intent);

    }

    // intent to make this activity to be started by an Intent
    public static Intent createIntent(Context context, IdpResponse idpResponse)
    {
        //Intent in = IdpResponse.getIntent(idpResponse);
        return new Intent().setClass(context, SignedInActivity.class).putExtra
                                (ExtraConstants.IDP_RESPONSE, idpResponse);
    }

    // event listener for sign out
    private View.OnClickListener signOutListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {   // @TODO implement @fn signOut() method
            signOut();
        }
    };

    // signOut method to start a new MainActivity to bring you back to the sign-in screen.
    public void signOut()
    {
        // reset the device token so no notifications get sent after logging out
        FirebaseAuth auth;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database;
        DatabaseReference usersRef;
        DatabaseReference notificationRef;

        // get current user's userID
        String currentUserId =
                currentUser.getEmail().replaceAll("[-+.@^:,]", "");
        // initiate the user path reference
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.child(currentUserId)
                .child("deviceToken")
                .setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            // do nothing
                        }
                    }
                });

        AuthUI.getInstance().signOut(this).addOnCompleteListener
        (new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    startActivity(MainActivity.createIntent(SignedInActivity.this));
                    finish();
                }
                else
                {   // Sign out failed
                    try {
                        // now we attempt to get the user's device token once login is successful
                        FirebaseAuth auth;
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseDatabase database;
                        DatabaseReference usersRef;
                        DatabaseReference notificationRef;

                        // get current user's userID
                        String currentUserId =
                                currentUser.getEmail().replaceAll("[-+.@^:,]", "");
                        // get device token
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        // initiate the user path reference
                        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        usersRef.child(currentUserId)
                                .child("deviceToken")
                                .setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            // do nothing
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
