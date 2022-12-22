package com.moroney.transportationapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.moroney.transportationapp.database.Ticket;
import com.moroney.transportationapp.datetime.DatePickerFragment;
import com.moroney.transportationapp.datetime.TimePickerFragment;
import com.moroney.transportationapp.records.TripRecordsActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{

    private GoogleMap mMap;
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    ArrayList<LatLng> mMarkerPoints;
    String TAG = "placeautocomplete";

    Button bookTicket;
    Button bookTrainTicket;
    Button dateCalendar;
    Button timeCalendar;
    ImageView qrCodeImageView;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference tickets;

    // create database variable within try-catch vault
    FirebaseAuth auth;
    TextView userEmail;
    TextView userName;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage;
    FirebaseDatabase database;
    DatabaseReference mainRef;

    private String bookingID;

    public static final String TRAIN_TICKET_TYPE = "train";
    public static final String BUS_TICKET_TYPE = "bus";

    String currentUserID;
    String placeNameOrigin;
    String placeNameDestination;
    String placeDate;
    String placeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mMarkerPoints = new ArrayList<>();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Initialize Places. @TODO: Add Google Maps key to BuildConfig.java
        Places.initialize(getApplicationContext(), "" + R.string.google_maps_key);
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment toOrigin = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.origin);
        toOrigin.setHint("Current Location");
        toOrigin.getView().findViewById(R.id.places_autocomplete_search_button).setBackgroundResource(R.drawable.bluemarkerseachbar);

        // Specify the types of place data to return.
        toOrigin.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        // Set up a PlaceSelectionListener to handle the response.
        toOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mMap.addMarker(markerOptions.position(place.getLatLng()));
                placeNameOrigin = place.getName();
                mMarkerPoints.add(place.getLatLng());
                mOrigin = mMarkerPoints.get(0);
                drawRoute();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        AutocompleteSupportFragment toDestination = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination);
        toDestination.setHint("Destination");
        toOrigin.getView().findViewById(R.id.places_autocomplete_search_button)
                .setBackgroundResource(R.drawable.redmarkersearchbar);

        // Specify the types of place data to return.
        toDestination.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        // Set up a PlaceSelectionListener to handle the response.
        toDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                placeNameDestination = place.getName();
                mMarkerPoints.add(place.getLatLng());
                mDestination = mMarkerPoints.get(1);
                drawRoute();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mapFragment.getMapAsync(this);

        // set ticket date
        Button button = (Button) findViewById(R.id.datecalendar);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        // set ticket time
        Button button2 = (Button) findViewById(R.id.timecalendar);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        bookTicket = findViewById(R.id.bookticket);
        bookTrainTicket = findViewById(R.id.booktrainticket);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        timeCalendar = findViewById(R.id.timecalendar);
        dateCalendar = findViewById(R.id.datecalendar);
        db = FirebaseDatabase.getInstance();
        tickets = db.getReference("Tickets");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // strip all special characters to create unique username string
        final String currentUserId = currentUser.getEmail().replaceAll("[-+.@^:,]","");


        // booking bus tickets
        bookTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ticketsRef = db.getReference("Tickets");
                final String key = ticketsRef.child(currentUserId).push().getKey();
                Ticket ticket = new Ticket();

                ticket.setUserID(currentUserId);
                ticket.setPlaceOfOrigin(placeNameOrigin);
                ticket.setPlaceOfDestination(placeNameDestination);
                ticket.setDate(placeDate);
                ticket.setTime(placeTime);
                ticket.setTicketID(key);
                ticket.setTicketType(BUS_TICKET_TYPE);
                ticket.setBookedOn(String.valueOf(System.currentTimeMillis()));


                // quick null check validation
                if( (placeNameDestination != null) && (placeNameOrigin != null)
                    && (placeDate != null) && (placeTime != null)  )
                {
                    ticketsRef.child(currentUserId).child(key).child("ticketID")
                            .setValue(key);
                    ticketsRef.child(currentUserId).child(key).child("userID")
                            .setValue(currentUserId);
                    ticketsRef.child(currentUserId).child(key).child("placeOfOrigin")
                            .setValue(placeNameOrigin);
                    ticketsRef.child(currentUserId).child(key).child("placeOfDestination")
                            .setValue(placeNameDestination);
                    ticketsRef.child(currentUserId).child(key).child("date")
                            .setValue(placeDate);
                    ticketsRef.child(currentUserId).child(key).child("time")
                            .setValue(placeTime);
                    ticketsRef.child(currentUserId).child(key).child("ticketType")
                            .setValue(BUS_TICKET_TYPE);
                    ticketsRef.child(currentUserId).child(key).child("bookedOn")
                            .setValue(String.valueOf(new Date(System.currentTimeMillis())));

                    // generate the QR code
                    String text =
                            "BOOKED BY: " + currentUser.getDisplayName() + "\n" +
                            "TICKET ID: " + key + "\n" +
                            "TICKET TYPE: " + BUS_TICKET_TYPE.toUpperCase() + "\n" +
                            "BOOKING DATE: " + placeDate + "\t" + placeTime + "\n" +
                            "BOOKED ON: " + String.valueOf(new Date(System.currentTimeMillis())) + "\n" +
                            "ORIGIN: " + placeNameOrigin + "\n" +
                            "DESTINATION: " + placeNameDestination;

                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try
                    {
                        BitMatrix bitMatrix = multiFormatWriter.encode(
                                                text, BarcodeFormat.QR_CODE, 200, 200);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        Bitmap newbitmap= BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()));
                        qrCodeImageView.setImageBitmap(newbitmap);
                    }
                    catch (WriterException e)
                    {
                        e.printStackTrace();
                    }


                    try // send the QR code to Firebase Storage
                    {
                        // Firebase storage reference where QR codes are stored
                        StorageReference imageRef = storage.getReference()
                                .child("QRCODE")
                                .child(key)
                                .child("ticket.jpg");
                        // call a putBytes method that handles the file upload asynchronously
                        // ...the upload is done byte by byte
                        UploadTask uploadTask = imageRef.putBytes(getByteArray(qrCodeImageView));
                        // add onFailure listener to catch any errors if upload fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                String ex = e.getLocalizedMessage();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                            {
                                try
                                {
                                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!urlTask.isSuccessful());
                                    Uri downloadUrl = urlTask.getResult();
                                    ticketsRef.child(currentUserId).child(key).child("qrCodeUrl")
                                            .setValue(urlTask.getResult());
                                }
                                catch (RuntimeException | StackOverflowError e)
                                {
                                    e.printStackTrace();
                                }

                                Toast.makeText(
                                    getBaseContext(), R.string.ticket_qrcode_uploaded, Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (RuntimeException e) {  // in case it crashes, some devices can't do it
                        e.printStackTrace();
                        Toast.makeText(
                            getBaseContext(), R.string.qrcode_not_supported, Toast.LENGTH_SHORT).show();
                    }


                    // display brief notification
                    Snackbar ticketBooked = Snackbar.make(
                            getCurrentFocus(), R.string.ticket_booked_successfully, Snackbar.LENGTH_LONG);
                    ticketBooked.setAction(getResources().getString(R.string.check_now),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    // go to trip records
                                    goToTripRecords(getCurrentFocus());
                                }
                            });
                    ticketBooked.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
                    ticketBooked.show();

                    // reset the selections
                    placeNameOrigin = null;
                    placeNameDestination = null;
                    toOrigin.setText(null);
                    toDestination.setText(null);
                    dateCalendar.setText(R.string.set_date);
                    timeCalendar.setText(R.string.set_time);
                    qrCodeImageView.setImageBitmap(null);

                    //finish(); // go back to previous activity
                }
                else  // nothing selected
                {
                    Snackbar.make(
                        getCurrentFocus(), R.string.missing_values, Snackbar.LENGTH_SHORT).show();
                }



            }
        });

        // book train tickets
        bookTrainTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ticketsRef = db.getReference("Tickets");
                final String key = ticketsRef.child(currentUserId).push().getKey();
                Ticket ticket = new Ticket();

                ticket.setUserID(currentUserId);
                ticket.setPlaceOfOrigin(placeNameOrigin);
                ticket.setPlaceOfDestination(placeNameDestination);
                ticket.setDate(placeDate);
                ticket.setTime(placeTime);
                ticket.setTicketID(key);
                ticket.setTicketType(TRAIN_TICKET_TYPE);

                // quick null check validation
                if ((placeNameDestination != null) && (placeNameOrigin != null)
                        && (placeDate != null) && (placeTime != null))
                {

                    ticketsRef.child(currentUserId).child(key).child("ticketID")
                            .setValue(key);
                    ticketsRef.child(currentUserId).child(key).child("userID")
                            .setValue(currentUserId);
                    ticketsRef.child(currentUserId).child(key).child("placeOfOrigin")
                            .setValue(placeNameOrigin);
                    ticketsRef.child(currentUserId).child(key).child("placeOfDestination")
                            .setValue(placeNameDestination);
                    ticketsRef.child(currentUserId).child(key).child("date")
                            .setValue(placeDate);
                    ticketsRef.child(currentUserId).child(key).child("time")
                            .setValue(placeTime);
                    ticketsRef.child(currentUserId).child(key).child("ticketType")
                            .setValue(TRAIN_TICKET_TYPE);
                    ticketsRef.child(currentUserId).child(key).child("bookedOn")
                            .setValue(String.valueOf(new Date(System.currentTimeMillis())));

                    // generate the QR code
                    String text =
                            "BOOKED BY: " + currentUser.getDisplayName() + "\n" +
                            "TICKET ID: " + key + "\n" +
                            "TICKET TYPE: " + TRAIN_TICKET_TYPE.toUpperCase() + "\n" +
                            "BOOKING DATE: " + placeDate + "\t" + placeTime + "\n" +
                            "BOOKED ON: " + String.valueOf(new Date(System.currentTimeMillis())) + "\n" +
                            "ORIGIN: " + placeNameOrigin + "\n" +
                            "DESTINATION: " + placeNameDestination;

                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try
                    {
                        BitMatrix bitMatrix = multiFormatWriter.encode(
                                text, BarcodeFormat.QR_CODE, 200, 200);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        Bitmap newbitmap= BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()));
                        qrCodeImageView.setImageBitmap(newbitmap);
                    }
                    catch (WriterException e)
                    {
                        e.printStackTrace();
                    }


                    try // send the QR code to Firebase Storage
                    {
                        // Firebase storage reference where QR codes are stored
                        StorageReference imageRef = storage.getReference()
                                .child("QRCODE")
                                .child(key)
                                .child("ticket.jpg");
                        // call a putBytes method that handles the file upload asynchronously
                        // ...the upload is done byte by byte
                        UploadTask uploadTask = imageRef.putBytes(getByteArray(qrCodeImageView));
                        // add onFailure listener to catch any errors if upload fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                String ex = e.getLocalizedMessage();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                            {
                                try
                                {
                                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!urlTask.isSuccessful());
                                    Uri downloadUrl = urlTask.getResult();
                                    ticketsRef.child(currentUserId).child(key).child("qrCodeUrl")
                                            .setValue(urlTask.getResult());
                                }
                                catch (RuntimeException | StackOverflowError e)
                                {
                                    e.printStackTrace();
                                }
                                Toast.makeText(
                                    getBaseContext(), R.string.ticket_qrcode_uploaded, Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (RuntimeException e) {  // in case it crashes, some devices can't do it
                        e.printStackTrace();
                        Toast.makeText(
                            getBaseContext(), R.string.qrcode_not_supported, Toast.LENGTH_SHORT).show();
                    }



                    // display brief notification
                    Snackbar ticketBooked = Snackbar.make(
                            getCurrentFocus(), R.string.ticket_booked_successfully, Snackbar.LENGTH_LONG);
                    ticketBooked.setAction(getResources().getString(R.string.check_now),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // go to trip records
                                    goToTripRecords(getCurrentFocus());
                                }
                            });
                    ticketBooked.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
                    ticketBooked.show();

                    // reset the selections
                    placeNameOrigin = null;
                    placeNameDestination = null;
                    toOrigin.setText(null);
                    toDestination.setText(null);
                    placeDate = null;
                    placeTime = null;
                    dateCalendar.setText(R.string.set_date);
                    timeCalendar.setText(R.string.set_time);
                    qrCodeImageView.setImageBitmap(null);

                    // finish(); // go back to previous activity
                }
                else  // nothing filled up or selected
                {
                    Snackbar.make(
                        getCurrentFocus(), R.string.missing_values, Snackbar.LENGTH_SHORT).show();
                }

            }
        });



    } /**end {@link #onCreate(Bundle)}*/





    // method to retrieve image from ImageView
    public byte[] getByteArray(ImageView imageView)
    {
        // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        return data;
    }







    // go to TripRecords
    public void goToTripRecords(View view)
    {
        Intent intent = new Intent(this, TripRecordsActivity.class);
        startActivity(intent);
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        Button button = (Button) findViewById(R.id.datecalendar);
        button.setText(currentDateString);
        placeDate = currentDateString;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        Button button2 = (Button) findViewById(R.id.timecalendar);
        button2.setText(hourOfDay + " : " + minute);
        placeTime = hourOfDay + " : " + minute;
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.getMyLocation();
        mMap.setMyLocationEnabled(true);

    } /** end {@link #onMapReady(GoogleMap)}*/




    /**function for drawing routes*/
    private void drawRoute()
    {
        if (mMarkerPoints.size() == 2)
        {
            //Create the URL to get request from first marker to second marker
            mOrigin = mMarkerPoints.get(0);
            mDestination = mMarkerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(mOrigin, mDestination);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }

    } /**end {@link #drawRoute()}*/



    /**function for getting directions*/
    private String getDirectionsUrl(LatLng origin,LatLng dest)
    {

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Key @TODO: Key should be in BuildConfig.java
        String key = "key=" + R.string.google_maps_key;

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;

    } /** end {@link #getDirectionsUrl(LatLng, LatLng)}*/





    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception on download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;

    } /** end {@link #downloadUrl(String)}*/





    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String>
    {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url)
        {
            // For storing data from web service
            String data = "";

            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask","DownloadTask : " + data);
            }catch(Exception e)
            {
                Log.d("Background Task",e.toString());
            }
            return data;

        } /**end {@link #doInBackground(String...)}*/

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        } /** end {@link #onPostExecute(String)} */

    } /* end DownloadTask */




    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >
    {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                ParseDirections parser = new ParseDirections();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLACK);
            }


            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if(mPolyline != null){
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

            }else
                Toast.makeText(getApplicationContext(),"No route is found", Toast.LENGTH_LONG).show();
        }


    } /* end ParserTask class */



}

