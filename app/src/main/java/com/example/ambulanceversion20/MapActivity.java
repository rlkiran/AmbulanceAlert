package com.example.ambulanceversion20;

import android.annotation.SuppressLint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
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
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener
        , MapboxMap.OnMapClickListener{

    private Button startButton;
    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private Point originPosition, destinationPosition;
    private NavigationMapRoute navigationMapRoute;
    public static final String TAG = "MainActivity";
    GeoPoint resultGeo;
    LatLng point2 = new LatLng();
    MarkerOptions markerOptions = new MarkerOptions();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.access_token));
        setContentView(R.layout.activity_map);
        startButton = findViewById(R.id.startNavigation);
        Button requestButton = findViewById(R.id.request);
        startButton.setEnabled(false);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        requestButton.setOnClickListener(v -> {
            map.clear();
            GeoPoint geoPoint = getGeoPoint();
            if(geoPoint != null) {
                destinationPosition = Point.fromLngLat(geoPoint.getLongitude(), geoPoint.getLatitude());
                originPosition = Point.fromLngLat(originLocation.getLongitude(),originLocation.getLatitude());
                Toast.makeText(this, " Request Sent", Toast.LENGTH_SHORT).show();
                startButton.setEnabled(true);
                getRoute(originPosition,destinationPosition);
                setMarker();
                sendCoOrdinates(originLocation);
            } else {
                Toast.makeText(this, " Request Sent", Toast.LENGTH_SHORT).show();
                startButton.setEnabled(true);
            }

        });
        startButton.setOnClickListener(v -> {
            Toast.makeText(this, "Updating Ambulance Location on MAP", Toast.LENGTH_LONG).show();
            GeoPoint geoPoint = getGeoPoint();
            if(geoPoint != null) {
                destinationPosition = Point.fromLngLat(geoPoint.getLongitude(), geoPoint.getLatitude());
                originPosition = Point.fromLngLat(originLocation.getLongitude(),originLocation.getLatitude());
                getRoute(originPosition,destinationPosition);
                setMarker();
                sendCoOrdinates(originLocation);
            } else {
                Toast.makeText(this, " No Routes Found", Toast.LENGTH_SHORT).show();
            }


        });

    }

    private void setMarker() {
        map.clear();
        point2.setLongitude(Objects.requireNonNull(getGeoPoint()).getLongitude());
        point2.setLatitude( getGeoPoint().getLatitude());
        markerOptions.setTitle("Destination");
        markerOptions.position(point2);
        IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.ambulance_icon);
        markerOptions.setIcon(icon);
        map.addMarker(markerOptions);
    }

    private GeoPoint getGeoPoint() {

        DocumentReference docRef = db.collection("Ambulances").document("ambulance one");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    resultGeo =   document.getGeoPoint("geoLocation");

                } else {
                    Log.d("LOGGER", "No such document");
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.getException());
            }
        });
        if(resultGeo != null) {
            return resultGeo;
        }
        else {
            return  null;
            //return new GeoPoint(13.0827,80.2707);
        }

    }

    private void sendCoOrdinates(Location originLocation) {
        GeoPoint userLoc = new GeoPoint(originLocation.getLatitude(),originLocation.getLongitude());

        DocumentReference userRef = db.collection("requests").document("requestone");

        userRef
                .update("geoLocation", userLoc)

                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

    }


    //first Function
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
       // map.addOnMapClickListener(this);
        enableLocation();
    }

    private void enableLocation() {
        if(PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();
            initializeLocationLayer();

        } else  {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }


    @SuppressLint("MissingPermission")
    private void initializeLocationLayer() {
        locationLayerPlugin = new LocationLayerPlugin(mapView,map,locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);

    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),13.0));
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        /*
        if (destinationMarker != null) {
            map.removeMarker(destinationMarker);
        }

        point.setLatitude(13.0827);
        point.setLongitude(80.2707);
        //destinationMarker = map.addMarker(new MarkerOptions().position(point));
        destinationMarker = map.addMarker(new MarkerOptions().position(point));
        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        //lat= 13.0827 lng = 80.2707
        //destinationPosition = Point.fromLngLat(13.0827, 80.2707);
        originPosition = Point.fromLngLat(originLocation.getLongitude(),originLocation.getLatitude());


        getRoute(originPosition,destinationPosition);
       // startButton.setEnabled(true);
        startButton.setBackgroundResource(R.color.colorPrimary);
        */
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            Log.e(TAG,"Check Right User or Access Token");
                            return;
                        } else if (response.body().routes().size() == 0) {
                            Log.e(TAG,"No Routes Found");
                        }

                        DirectionsRoute currentRoute = response.body().routes().get(0);

                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView,map);
                        }
                        navigationMapRoute.addRoute(currentRoute);

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG,"Error"+t.getMessage());
                    }
                });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted) {
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if(locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        mapView.onDestroy();
    }

}
