package com.moroney.transportationapp.schedule;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.moroney.transportationapp.R;

import java.util.Objects;

public class TrainScheduleActivity extends AppCompatActivity
{

    private WebView trainScheduleWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_schedule);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.train_schedules));

        trainScheduleWebView = (WebView) findViewById(R.id.trainScheduleWebView);
        trainScheduleWebView.setWebViewClient(new WebViewClient());
        trainScheduleWebView.loadUrl("https://www.myrapid.com.my/plan-my-journey");
       // trainScheduleWebView.loadUrl("https://www.train36.com/ktm-komuter-tampin-batu-caves-route.html");

        WebSettings webSettings = trainScheduleWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.getAllowContentAccess();
        webSettings.getDatabaseEnabled();
        webSettings.setEnableSmoothTransition(true);
        webSettings.setGeolocationEnabled(true);

    }

    @Override
    public void onBackPressed() {
        if (trainScheduleWebView.canGoBack())
        {
            trainScheduleWebView.goBack();
        }
        else
        {
            super.onBackPressed();
        }

    }
}
