package com.moroney.transportationapp.schedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.moroney.transportationapp.R;

import java.util.Objects;

public class TripScheduleActivity extends AppCompatActivity
{

    Button busScheduleButton;
    Button trainScheduleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_schedule);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.trip_schedules));

        busScheduleButton = findViewById(R.id.busScheduleButton);
        trainScheduleButton = findViewById(R.id.trainScheduleButton);

        busScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goToBusSchedule(v);
            }
        });

        trainScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goToTrainSchedule(v);
            }
        });


    } /**end {@link #onCreate(Bundle)}*/


    /**go to {@link TrainScheduleActivity}*/
    public void goToTrainSchedule(View view)
    {
        Intent intent = new Intent(this, TrainScheduleActivity.class);
        startActivity(intent);
    }

    /**go to {@link BusScheduleActivity}*/
    public void goToBusSchedule(View view)
    {
        Intent intent = new Intent(this, BusScheduleActivity.class);
        startActivity(intent);
    }

}
