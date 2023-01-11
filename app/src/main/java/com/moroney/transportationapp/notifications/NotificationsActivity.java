package com.moroney.transportationapp.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moroney.transportationapp.R;
import com.moroney.transportationapp.database.Notifications;
import com.moroney.transportationapp.records.ShowQRTicketActivity;

import java.util.Objects;

public class NotificationsActivity extends AppCompatActivity
{

    DatabaseReference currentUserNotificationsRef, notificationsRef;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private RecyclerView notificationsRecyclerView;
    private FirebaseRecyclerOptions<Notifications> notificationsOptions;
    private FirebaseRecyclerAdapter<Notifications, NotificationsViewHolder> notificationsAdapter;
    private LinearLayoutManager notificationsLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.notifications));

        String currentUserID = currentUser.getEmail().replaceAll("[-+.@^:,]","");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        currentUserNotificationsRef = notificationsRef.child(currentUserID);

        Intent intent = new Intent(this, ShowQRTicketActivity.class);

        notificationsLayoutManager = new LinearLayoutManager(this);
        notificationsLayoutManager.setReverseLayout(true);
        notificationsLayoutManager.setStackFromEnd(true);
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setHasFixedSize(true);
        notificationsRecyclerView.setLayoutManager(notificationsLayoutManager);

        notificationsOptions = new FirebaseRecyclerOptions
                .Builder<Notifications>()
                .setQuery(currentUserNotificationsRef, Notifications.class)
                .build();

        notificationsAdapter = new FirebaseRecyclerAdapter<Notifications, NotificationsViewHolder>(notificationsOptions)
        {
            @Override
            protected void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position, @NonNull Notifications model)
            {

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Bundle bundle = new Bundle();
                        bundle.putString("GET_TICKET_ID", model.getTicketID());
                        bundle.putString("GET_DESTINATION", model.getDestination());
                        bundle.putString("GET_ORIGIN", model.getOrigin());
                        intent.putExtra("QRCODE_DATA", bundle);
                        intent.putExtra("FROM_ACTIVITY", "NOTIFICATIONS");
                        startActivity(intent);
                    }
                });

                final String FROM_TICKET_ID = getResources().getString(R.string.ticket_id);
                final String FROM_FROM_WHO = getResources().getString(R.string.shared_by);
                final String FROM_DATE_TIME = getResources().getString(R.string.date_sent);
                final String FROM_DESTINATION = getResources().getString(R.string.destination);
                final String FROM_ORIGIN = getResources().getString(R.string.origin);

                holder.textViewForSharedTicketID.setText(
                        String.format("%s: %s", FROM_TICKET_ID, model.getTicketID()));
                holder.textViewForFrom.setText(
                        String.format("%s: %s", FROM_FROM_WHO, model.getFromExactNameEmail()));
                holder.textViewForDateTime.setText(
                        String.format("%s: %s", FROM_DATE_TIME, model.getDateTime()));
                holder.textViewForSharedDestination.setText(
                        String.format("%s: %s", FROM_DESTINATION, model.getDestination()));
                holder.textViewForSharedOrigin.setText(
                        String.format("%s: %s", FROM_ORIGIN, model.getOrigin()));
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.notifications_single_view_layout, parent, false);

                return new NotificationsViewHolder(view);
            }
        };

        notificationsAdapter.startListening();
        notificationsRecyclerView.setAdapter(notificationsAdapter);


    }


}
