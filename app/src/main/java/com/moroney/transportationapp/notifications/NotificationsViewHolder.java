package com.moroney.transportationapp.notifications;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moroney.transportationapp.R;

public class NotificationsViewHolder extends RecyclerView.ViewHolder
{
    TextView
    textViewForDateTime,
    textViewForFrom,
    textViewForSharedTicketID,
    textViewForSharedDestination,
    textViewForSharedOrigin;

    LinearLayout notificationsRVDivider;

    View view;

    public NotificationsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        textViewForDateTime = itemView.findViewById(R.id.textViewForDateTime);
        textViewForFrom = itemView.findViewById(R.id.textViewForFrom);
        textViewForSharedTicketID = itemView.findViewById(R.id.textViewForSharedTicketID);
        textViewForSharedDestination = itemView.findViewById(R.id.textViewForSharedDestination);
        textViewForSharedOrigin = itemView.findViewById(R.id.textViewForSharedOrigin);

        notificationsRVDivider = itemView.findViewById(R.id.notificationsRVDivider);

        view = itemView;

    }


}
