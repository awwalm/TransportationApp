package com.moroney.transportationapp.records;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.moroney.transportationapp.R;
import com.moroney.transportationapp.friends.FriendsActivity;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ShowQRTicketActivity extends AppCompatActivity
{

    String TICKET_ID;
    String GET_DESTINATION, GET_ORIGIN;
    ImageView showTicketQrCodeImageView;
    ExtendedFloatingActionButton shareTripButton;

    // Firebase variables
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    // strip all special characters to create unique username string
    String plainEmail = currentUser.getEmail().replaceAll("[-+.@^:,]","");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qrticket);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.ticket_qrcode));

        // obtain ticket ID from bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("QRCODE_DATA");

        try
        {
            if ( (bundle != null) & !bundle.isEmpty() )
            {
                TICKET_ID = bundle.getString("GET_TICKET_ID", null);
                GET_DESTINATION = bundle.getString("GET_DESTINATION", null);
                GET_ORIGIN = bundle.getString("GET_ORIGIN", null);
                Toast.makeText(this, TICKET_ID, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // attempt to retrieve the QR code image and load/display it
        showTicketQrCodeImageView = findViewById(R.id.showTicketQrCodeImageView);
        try
        {
            if (TICKET_ID != null) {  downloadPicture();  }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Intent intent2 = new Intent(this, FriendsActivity.class);
        shareTripButton = findViewById(R.id.shareTripButton);
        final String PREVIOUS_ACTIVITY = intent.getStringExtra("FROM_ACTIVITY");
        if (!PREVIOUS_ACTIVITY.equals("TRIP_RECORDS"))
        {
            shareTripButton.setEnabled(false);
        }

        shareTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString("SHARE_TICKET_ID", TICKET_ID);
                bundle.putString("GET_DESTINATION", GET_DESTINATION);
                bundle.putString("GET_ORIGIN", GET_ORIGIN);
                intent2.putExtra("SHARE_TRIP_DATA", bundle);
                startActivity(intent2);
            }
        });


    } /** end {@link #onCreate(Bundle)}*/



    // method for retrieving photo
    private void downloadPicture()
    {
        // initialize storage
        storage = FirebaseStorage.getInstance();
        // and get a reference to it
        storageRef = storage.getReference();
        // make the reference extend by or to a child node
        StorageReference imageRef = storageRef.child("QRCODE")
                .child(TICKET_ID)
                .child("ticket.jpg");
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
                                showTicketQrCodeImageView.setImageBitmap(myBitmap);
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



    // refresh this page itself
    public void goToFriends(View view)
    {
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
    }

}
