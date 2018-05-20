package com.hurahura.ray.laporgan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    Geocoder geocoder;
    List<Address> addresses;

    private Button btnSubmit;
    private Button fab_home;
    private Button btnCloseCamera;
    private Button btnCapture;
    private Button btnFlash;
    private Button btnJenisLaporan;

    private TextView tvJenisLaporan;
    private TextView tvFab;
    private TextView tvLocation;

    private LocationListener mLocationListener;
    private GPSTracker gpsTracker;
    private LatLng currentLocation;
    private GoogleMap mMap;
    private CameraView cameraView;

    private View cameraViewHolder;

    //0 = lapor, 1 = sampah
    public static int JENIS_LAPORAN = 0;

    private long captureStartTime;

    public static String KEY;
    public static String IMG_PATH;

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

        IMG_PATH = getIntent().getStringExtra("IMG_PATH");

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.setMyLocationEnabled(true);
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
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

        geocoder = new Geocoder(this, Locale.getDefault());


        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,
                    10, mLocationListener);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fab_home = findViewById(R.id.fab_home);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCloseCamera = findViewById(R.id.btnCloseCamera);
        cameraView = findViewById(R.id.cameraView);
        btnCapture = findViewById(R.id.btnCapture);
        btnFlash = findViewById(R.id.btnFlash);
        btnJenisLaporan = findViewById(R.id.btnJenisLaporan);
        tvJenisLaporan = findViewById(R.id.tvJenisLaporanHistory);
        tvFab = findViewById(R.id.fab_text);
        cameraViewHolder = findViewById(R.id.cameraViewHolder);
        tvLocation = findViewById(R.id.tvLocation);

        fab_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLapor();

            }
        });
        btnCloseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeOnClick();
            }
        });
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraView.getFlash()==CameraKit.Constants.FLASH_TORCH) {
                    cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                    btnFlash.setBackground(getDrawable(R.drawable.ic_flashoff));
                }
                else {
                    cameraView.setFlash(CameraKit.Constants.FLASH_TORCH);
                    btnFlash.setBackground(getDrawable(R.drawable.ic_flashon));
                }
            }
        });
        btnJenisLaporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (JENIS_LAPORAN==0) {
                    JENIS_LAPORAN=1;
                    btnJenisLaporan.setBackground(getDrawable(R.drawable.ic_sampah));
                    tvJenisLaporan.setText("JEMPUT SAMPAH");
                }
                else {
                    JENIS_LAPORAN=0;
                    btnJenisLaporan.setBackground(getDrawable(R.drawable.ic_lapor));
                    tvJenisLaporan.setText("LAPOR KELUHAN");
                }
            }
        });
        btnCloseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraViewHolder.setVisibility(View.GONE);
                tvFab.setVisibility(View.VISIBLE);
                fab_home.setVisibility(View.VISIBLE);
                cameraView.stop();
            }
        });
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureStartTime = System.currentTimeMillis();
                captureImage();
            }
        });
    }

    private void closeOnClick() {
        btnSubmit.setVisibility(View.INVISIBLE);
        findViewById(R.id.fab_text).setVisibility(View.VISIBLE);
        fab_home.setVisibility(View.VISIBLE);
        btnCloseCamera.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                cameraView.stop();
                logout();
                break;
            case R.id.action_profile:
                cameraView.stop();
                ProfileActivity.KEY = KEY;
                startActivity(new Intent(getBaseContext(),ProfileActivity.class));
                break;
//            case R.id.action_history:
//                HistoryActivity.KEY = KEY;
//                startActivity(new Intent(getBaseContext(),HistoryActivity.class));
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

    public void startLapor() {
        fab_home.setVisibility(View.INVISIBLE);
        tvFab.setVisibility(View.INVISIBLE);
        cameraViewHolder.setVisibility(View.VISIBLE);
        cameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
        cameraView.setFlash(0);
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
        try {
            addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tvLocation.setText(addresses.get(0).getAddressLine(0));
    }

    protected void onResume() {
        super.onResume();
        cameraView.start();
        gpsTracker = new GPSTracker(this);
        currentLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        try {
            addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //tvLocation.setText(addresses.get(0).getAddressLine(0));
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraViewHolder.setVisibility(View.GONE);
        cameraView.stop();
        tvFab.setVisibility(View.VISIBLE);
        fab_home.setVisibility(View.VISIBLE);
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

                    }
                    return;
                }
                // other 'case' lines to check for other
                // permissions this app might request
            }
        }
    }

    public void logout() {
        MainActivity.signOut();
        MainActivity.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(HomeActivity.this,MainActivity.class));
                        finish();
                    }
                });
    }

    public void captureImage() {
        cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(CameraKitImage event) {
                byte[] jpeg = event.getJpeg();
                String[] loc = currentLocation.toString().split(",");
                long callbackTime = System.currentTimeMillis();
                ResultHolder.dispose();
                ResultHolder.setImage(jpeg);
                ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
                ResultHolder.setTimeToCallback(callbackTime - captureStartTime);
                Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
                PreviewActivity.LOCATION = tvLocation.getText().toString();
                PreviewActivity.JENIS_LAPORAN = tvJenisLaporan.getText().toString();
                intent.putExtra("KEY",KEY);
                intent.putExtra("LOC_LAT",currentLocation.latitude);
                intent.putExtra("LOC_LNG",currentLocation.longitude);
                getApplicationContext().startActivity(intent);
            }
        });
    }

}
