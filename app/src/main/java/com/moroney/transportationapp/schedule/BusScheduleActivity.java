package com.moroney.transportationapp.schedule;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.moroney.transportationapp.R;

import java.util.Objects;

public class BusScheduleActivity extends AppCompatActivity
{
    private WebView busScheduleWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_schedule);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.bus_schedules));

        busScheduleWebView = (WebView) findViewById(R.id.busScheduleWebView);
        busScheduleWebView.setWebViewClient(new WebViewClient());
        busScheduleWebView.loadUrl(
                "https://www.myrapid.com.my/"+
                "traveling-with-us/how-to-travel-with-us/"+
                "rapid-kl/mrt/mrt-feeder-bus");

        WebSettings webSettings = busScheduleWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.getAllowContentAccess();
        webSettings.getDatabaseEnabled();
        webSettings.setEnableSmoothTransition(true);
        webSettings.setGeolocationEnabled(true);

    }


    @Override
    public void onBackPressed() {
        if (busScheduleWebView.canGoBack())
        {
            busScheduleWebView.goBack();
        }
        else
        {
            super.onBackPressed();
        }

    }

}
