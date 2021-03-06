package hu.ait.android.holla;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import hu.ait.android.holla.adapter.PlacesAdapter;
import hu.ait.android.holla.adapter.TeaAdapter;
import hu.ait.android.holla.data.Place;
import hu.ait.android.holla.data.PlaceResult;
import hu.ait.android.holla.data.Result;
import hu.ait.android.holla.network.GoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TeaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener{

    private ProgressDialog progressDialog;
    private final String URL_BASE = "https://maps.googleapis.com/maps/api/place/";
    private final String API_KEY = "AIzaSyAB1X1dK-fLyGAAuiKD9127SjgVh2K5XrI";

    private GoogleMap mMap;
    private GoogleAPI googleAPI;
    private LocationManager locationManager;
    private LocationListener mLocationListener;
    TextView tvLocation;
    private String currentLocation;

    private TeaAdapter teaAdapter;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tea);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        googleAPI = retrofit.create(GoogleAPI.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        requestNeededPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupAdapter();


        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.getLocation));
        dialog.show();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setUpToolBar(drawer);
    }

    private void setupAdapter() {
        teaAdapter = new TeaAdapter(getApplicationContext(), FirebaseAuth.getInstance().getUid());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.TeaRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(teaAdapter);
    }

    private void setUpToolBar(final DrawerLayout drawerLayout) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tea");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toolbar.setNavigationIcon(R.mipmap.app_icon);
        toolbar.setBackgroundColor(getResources().getColor(R.color.registerbackground));
    }

    private void requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        } else {
            startLocationMonitoring();
        }
    }

    private void startLocationMonitoring() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000, 10, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        dialog.dismiss();
        LatLng currLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 12));
        currentLocation = location.getLatitude() + "," + location.getLongitude();
        getPlaces();

    }

    private void getPlaces() {
        Call<PlaceResult> callPlace = googleAPI.getNearbyCafe(currentLocation, 3000,
                "cafe", "tea", API_KEY);
        callPlace.enqueue(new Callback<PlaceResult>() {
            @Override
            public void onResponse(Call<PlaceResult> call, Response<PlaceResult> response) {
                List<Result> results = response.body().getResults();
                if(response.isSuccessful()){
                    for (int i = 0; i < results.size(); i++) {
                        Result curr = results.get(i);
                        LatLng newPlace = new LatLng(curr.getGeometry().getLocation().getLat(),
                                curr.getGeometry().getLocation().getLng());
                        mMap.addMarker(new MarkerOptions().position(newPlace).title(curr.getName()));

                        final Place place = new Place(curr.getName(), curr.getRating()+"",
                                curr.getOpeningHours().getOpenNow(), "" + curr.getVicinity(),"", false);
                        new Thread(){
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        teaAdapter.addPlace(place);
                                    }
                                });
                            }
                        }.start();
                    }

                }
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {

            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_coffee) {
            stopLocationMonitoring();
            startActivity(new Intent(TeaActivity.this, Home.class));
        }else if(id == R.id.nav_tea){

        }else if(id == R.id.nav_favs){
            stopLocationMonitoring();
            startActivity(new Intent(TeaActivity.this, FavoritesActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getPermission();

    }

    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        } else {
            mMap.setMyLocationEnabled(true);
        }

    }

    private void stopLocationMonitoring() {
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationMonitoring();
    }
}
