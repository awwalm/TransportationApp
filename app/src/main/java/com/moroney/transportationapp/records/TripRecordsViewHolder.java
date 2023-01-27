package com.moroney.transportationapp.records;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moroney.transportationapp.R;

public class TripRecordsViewHolder extends RecyclerView.ViewHolder
{

    // views that are needed to be populated by the RecyclerView
    TextView textViewForBookedOn;
    TextView textViewForTripDate;
    TextView textViewForTripTime;
    TextView textViewForPlaceOfDestination;
    TextView textViewForPlaceOfOrigin;
    TextView textViewForTicketID;
    TextView textViewForTicketType;
    LinearLayout tripRecordsRVDivider;

    // view declaration for preparing target data fetching
    View view;

    public TripRecordsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        textViewForBookedOn = itemView.findViewById(R.id.textViewForBookedOn);
        textViewForTripDate = itemView.findViewById(R.id.textViewForTripDate);
        textViewForTripTime = itemView.findViewById(R.id.textViewForTripTime);
        textViewForPlaceOfDestination = itemView.findViewById(R.id.textViewForPlaceOfDestination);
        textViewForPlaceOfOrigin = itemView.findViewById(R.id.textViewForPlaceOfOrigin);
        textViewForTicketID = itemView.findViewById(R.id.textViewForTicketID);
        textViewForTicketType = itemView.findViewById(R.id.textViewForTicketType);
        tripRecordsRVDivider = itemView.findViewById(R.id.tripRecordsRVDivider);

        // view initialization
        view = itemView;

    }
}
