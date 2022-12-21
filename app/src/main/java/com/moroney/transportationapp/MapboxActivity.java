package com.moroney.transportationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapboxActivity extends AppCompatActivity implements
    OnMapReadyCallback, LocationEngineListener, PermissionsListener, MapboxMap.OnMapClickListener
{

    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;

    private Button startButton;
    private Point originPosition, destinationPosition;
    private Marker destinationMarker;

    private NavigationMapRoute navigationMapRoute;
    private final String TAG = getClass().getSimpleName();

    private ImageView hoveringMarker;
    private Layer droppedMarkerLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, TAG);
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        // This needs to be called either in your application object or
        // in the same activity which contains the mapview.
        // @TODO: obfuscate the access token
        Mapbox.getInstance(this, BuildConfig.MapboxAccessToken);
        setContentView(R.layout.activity_mapbox);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // set the title of the Activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.navigate));

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch navigation UI
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .origin(originPosition)
                        .destination(destinationPosition)
                        .shouldSimulateRoute(true)
                        .build();

                NavigationLauncher.startNavigation(MapboxActivity.this, options);
            }
        });
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.addOnMapClickListener(this);
        enableLocation();
    }

    private void enableLocation()
    {
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {   // do some stuff
            initializeLocationEngine();
            initializeLocationLayer();
        }
        else
        {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    private void initializeLocationEngine()
    {
        locationEngine =
                new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        // get last location
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null)
        {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        }
        else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void initializeLocationLayer()
    {
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location)
    {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13.0
        ));
    }

    // when map is clicked
    @Override
    public void onMapClick(@NonNull LatLng point)
    {
        // @TODO: configure for multiple selection
        if (destinationMarker != null)
        {
            map.removeMarker(destinationMarker);
        }
            destinationMarker = map.addMarker(new MarkerOptions().position(point));
            destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());

            originPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());

            getRoute(originPosition, destinationPosition);

            startButton.setEnabled(true);
            startButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            startButton.setTextColor(getResources().getColor(android.R.color.white));

            // Use the map target's coordinates to make a reverse geocoding search
            final LatLng mapTargetLatLng = map.getCameraPosition().target;

    }

    private void getRoute(Point origin, Point destination)
    {
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null)
                        {
                            Log.e(TAG, "NO ROUTES FOUND!, CHECK USER TOKEN.");
                            return;
                        } else if (response.body().routes().size() == 0)
                        {
                            Log.e(TAG, "NO ROUTES FOUND!");
                            try
                            {
                                Snackbar.make(getCurrentFocus(), "Routes not found.", Snackbar.LENGTH_LONG).show();
                            } catch (NullPointerException e)
                            {
                                e.printStackTrace();
                            }
                            return;
                        }

                        DirectionsRoute currentRoute = response.body().routes().get(0);

                        if (navigationMapRoute != null)
                        {
                            navigationMapRoute.removeRoute();
                        } else
                            {
                                navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                            }

                        navigationMapRoute.addRoute(currentRoute);

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, ""+t.getMessage());
                    }
                });
    }

    // provider begins sending updates
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null)
        {
            originLocation=location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        // present toast or dialog
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted)
        {
            enableLocation();
        }
    }

    // take care of all permissions stuff
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationEngine != null)
        {
            locationEngine.requestLocationUpdates();
            if (locationLayerPlugin != null)
            {
                locationLayerPlugin.onStart();
            }
        }
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null)
        {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null)
        {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null)
        {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }




}
