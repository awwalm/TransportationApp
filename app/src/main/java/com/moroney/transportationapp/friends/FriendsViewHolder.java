package com.moroney.transportationapp.friends;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moroney.transportationapp.R;

class FriendsViewHolder extends RecyclerView.ViewHolder
{
    TextView
    textViewForFriendsName,
    textViewForFriendsGender,
    textViewForFriendsEmail,
    textViewForFriendsGroupCode,
    textViewForFriendsFavoriteCity;

    LinearLayout friendsRVDivider;
    RelativeLayout friendsRelativeLayout;

    View view;

    public FriendsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        textViewForFriendsName = itemView.findViewById(R.id.textViewForFriendsName);
        textViewForFriendsGender = itemView.findViewById(R.id.textViewForFriendsGender);
        textViewForFriendsEmail = itemView.findViewById(R.id.textViewForFriendsEmail);
        textViewForFriendsGroupCode = itemView.findViewById(R.id.textViewForFriendsGroupCode);
        textViewForFriendsFavoriteCity = itemView.findViewById(R.id.textViewForFriendsFavoriteCity);

        friendsRVDivider = itemView.findViewById(R.id.friendsRVDivider);
        friendsRelativeLayout = itemView.findViewById(R.id.friendsRelativeLayout);

        view = itemView;

    }
}
