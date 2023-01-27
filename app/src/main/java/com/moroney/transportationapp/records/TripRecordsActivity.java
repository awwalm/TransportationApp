package com.moroney.transportationapp.records;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.moroney.transportationapp.R;
import com.moroney.transportationapp.database.Ticket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class TripRecordsActivity extends AppCompatActivity
{

    ExtendedFloatingActionButton scanQrCodeButton;
    TextInputEditText qrCodeTicketTInputEText;
    private IntentIntegrator qrScan; // qr code scanner object
    DatabaseReference ticketRef; // database Ticket path
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // current user

    // for dynamically generated Firebase RecyclerView
    private RecyclerView tripRecordsRecyclerView;
    private FirebaseRecyclerOptions<Ticket> ticketOptions;
    private FirebaseRecyclerAdapter<Ticket, TripRecordsViewHolder> ticketAdapter;
    private LinearLayoutManager tripLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // inflate layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_records);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.trip_records));

        // initialize QR scanner members
        scanQrCodeButton = findViewById(R.id.scanQrCodeButton);
        qrCodeTicketTInputEText = findViewById(R.id.qrCodeTicketTInputEText);
        qrScan = new IntentIntegrator(this);

        // scan QR code when user presses button
        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                qrScan.initiateScan();
            }
        });



        // initialize database Ticket path reference
        String currentUserID = currentUser.getEmail().replaceAll("[-+.@^:,]","");
        ticketRef = FirebaseDatabase.getInstance().getReference("Tickets").child(currentUserID);

        // initialize intent for activity that displays QR code based on bundle data
        Intent intent = new Intent(this, ShowQRTicketActivity.class);

        // begin RecyclerView processing
        // step 1: initialize RecyclerView
        tripLayoutManager = new LinearLayoutManager(this);
        tripLayoutManager.setReverseLayout(true);
        tripLayoutManager.setStackFromEnd(true);
        tripRecordsRecyclerView = findViewById(R.id.tripRecordsRecyclerView);
        tripRecordsRecyclerView.setHasFixedSize(true);
        //tripRecordsRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        tripRecordsRecyclerView.setLayoutManager(tripLayoutManager);
        //tripRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // step 2: initialize RecyclerView adapter options
        ticketOptions = new FirebaseRecyclerOptions
                .Builder<Ticket>()
                .setQuery(ticketRef, Ticket.class)
                .build();

        // step 3: initialize RecyclerView adapter itself
        ticketAdapter = new FirebaseRecyclerAdapter<Ticket, TripRecordsViewHolder>(ticketOptions)
        {
            @SuppressLint("DefaultLocale")
            @Override
            protected void onBindViewHolder(@NonNull TripRecordsViewHolder holder, int position, @NonNull Ticket model)
            {
                /** step 4: attach the {@linkplain holder} data members from
                 * {@link TripRecordsViewHolder} to set the details in
                 * {@link R.layout.trip_records_single_view_layout} to be dynamically populated */

                /** step 4.1 (optional): set onclick action when an item on the
                 * RecyclerView is clicked. note that we need to send the
                 * ticket id into a bundle/intent so it can be received by {@link ShowQRTicketActivity}*/
                final String GET_TICKET_ID = model.getTicketID();
                final String GET_DESTINATION = model.getPlaceOfDestination();
                final String GET_ORIGIN = model.getPlaceOfOrigin();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Bundle bundle = new Bundle();
                        bundle.putString("GET_TICKET_ID", GET_TICKET_ID);
                        bundle.putString("GET_DESTINATION", GET_DESTINATION);
                        bundle.putString("GET_ORIGIN", GET_ORIGIN);
                        intent.putExtra("QRCODE_DATA", bundle);
                        intent.putExtra("FROM_ACTIVITY", "TRIP_RECORDS");
                        startActivity(intent);
                    }
                });


                /* extract and organize the string resources we need. don't be jarred by this,
                * you can easily do this instead: holder.someTextView.setText(model.getSomeText());*/

                final String TICKET_ID = getResources().getString(R.string.ticket_id);
                final String TRIP_DATE = getResources().getString(R.string.trip_date);
                final String BOOKED_ON = getResources().getString(R.string.date_booked);
                final String DESTINATION = getResources().getString(R.string.destination);
                final String ORIGIN = getResources().getString(R.string.origin);
                final String TRIP_TIME = getResources().getString(R.string.trip_time);
                final String TICKET_TYPE = getResources().getString(R.string.ticket_type);

                holder.textViewForTicketID.setText(
                        String.format("%s: %s", TICKET_ID, model.getTicketID()));
                holder.textViewForTripDate.setText(
                        String.format("%s: %s", TRIP_DATE, model.getDate()));
                holder.textViewForBookedOn.setText(
                        String.format("%s: %s", BOOKED_ON, model.getBookedOn()));
                holder.textViewForPlaceOfDestination.setText(
                        String.format("%s: %s", DESTINATION, model.getPlaceOfDestination()));
                holder.textViewForPlaceOfOrigin.setText(
                        String.format("%s: %s", ORIGIN, model.getPlaceOfOrigin()));
                holder.textViewForTripTime.setText(
                        String.format("%s: %s", TRIP_TIME, model.getTime()));
                holder.textViewForTicketType.setText(
                        String.format("%s: %s", TICKET_TYPE, model.getTicketType()));

                //holder.tripRecordsRVDivider.setVisibility(View.GONE);

            } /**end {@link #onBindViewHolder(TripRecordsViewHolder, int, Ticket)}*/

            @NonNull
            @Override
            public TripRecordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                /** step 5: now, INFLATE the {@link R.layout.trip_records_single_view_layout}
                 * recall that we only POPULATED it in step 4!*/

                View view = LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.trip_records_single_view_layout, parent, false);

                return new TripRecordsViewHolder(view);
            }
        };

        /** step 6: start updating the {@link R.id.tripRecordsRecyclerView}*/
        ticketAdapter.startListening();
        tripRecordsRecyclerView.setAdapter(ticketAdapter);


        /** add {@link RecyclerView#addOnScrollListener(RecyclerView.OnScrollListener)}
         * to cater for Floating Action Button visibility while scrolling*/
        tripRecordsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { scanQrCodeButton.show(); }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0 || dy < 0 && scanQrCodeButton.isShown()) { scanQrCodeButton.hide(); }
            }
        });




    } /** end {@link #onCreate(Bundle)}*/







    // get the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null)
        {
            if (result.getContents() == null)   // if qr code has nothing in it
            {
                Toast.makeText(this, R.string.nothing_to_show, Toast.LENGTH_SHORT).show();
            }
            else    // if qr code contains data
            {
                try     // converting the data to JSON object
                {
                    JSONObject obj = new JSONObject(result.getContents());
                    qrCodeTicketTInputEText.setText(obj.toString());
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                    qrCodeTicketTInputEText.setText(result.getContents());
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }




} /** end {@link com.moroney.transportationapp.records.TripRecordsActivity}*/
