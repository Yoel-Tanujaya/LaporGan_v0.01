package com.hurahura.ray.laporgan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.ProcessingInstruction;

import java.lang.ref.PhantomReference;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FloatingActionsMenu main_fab_home;
    private View action_sampah;
    private View action_lapor;
    private View home_report_form;
    private Button btnSubmit;
    private Button fab_home;
    private Button fab_close;
    private ActionBar actionBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private LocationListener mLocationListener;
    GPSTracker gpsTracker;
    private LatLng currentLocation;
    private GoogleMap mMap;

    public User user;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
        setContentView(R.layout.activity_home);
        setToolbar();
        setStatusBarColor();

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.setMyLocationEnabled(true);
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    10, mLocationListener);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fab_home = (Button) findViewById(R.id.fab_home);
        home_report_form = findViewById(R.id.home_report_form);
        btnSubmit = findViewById(R.id.btnSubmit);
        fab_close = findViewById(R.id.fab_home_close);
        fab_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                laporOnClick();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "Tombol Submit di klik", Toast.LENGTH_SHORT);
            }
        });
        fab_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeOnClick();
            }
        });
    }

    private void closeOnClick() {
        home_report_form.setVisibility(View.INVISIBLE);
        btnSubmit.setVisibility(View.INVISIBLE);
        findViewById(R.id.fab_text).setVisibility(View.VISIBLE);
        fab_home.setVisibility(View.VISIBLE);
        fab_close.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                break;
            case R.id.action_profile:
                startActivity(new Intent(getBaseContext(), ProfileActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setToolbar() {
        View toolbarView = getLayoutInflater().inflate(R.layout.actionbar_home, null, false);
        TextView titleView = toolbarView.findViewById(R.id.toolbar_title);
        titleView.setText("Lapor Gan");

        getSupportActionBar().setCustomView(toolbarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue4)));
    }

//    public boolean checkLocationPermission() {
//        String permission = "android.permission.ACCESS_FINE_LOCATION";
//        int res = this.checkCallingOrSelfPermission(permission);
//        return (res == PackageManager.PERMISSION_GRANTED);
//    }

    public void laporOnClick() {
        home_report_form.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);
        findViewById(R.id.fab_text).setVisibility(View.INVISIBLE);
        fab_home.setVisibility(View.INVISIBLE);
        fab_close.setVisibility(View.VISIBLE);
    }

    public void setStatusBarColor() {
        Window window = this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(getResources().getColor(R.color.blue4));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
    }

    protected void onResume() {
        super.onResume();
        gpsTracker = new GPSTracker(this);
        currentLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission Granted

                    gpsTracker = new GPSTracker(this);
                    if (gpsTracker.canGetLocation) {
                        currentLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }
                // other 'case' lines to check for other
                // permissions this app might request
            }
        }
    }

    public void logout() {
        MainActivity.mAuth.signOut();
        MainActivity.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(HomeActivity.this,MainActivity.class));
                        finish();
                    }
                });
    }
}
