package com.hurahura.ray.laporgan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
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
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import org.w3c.dom.ProcessingInstruction;

import java.lang.ref.PhantomReference;

import es.dmoral.toasty.Toasty;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnSubmit;
    private Button fab_home;
    private Button btnCloseCamera;
    private Button btnCapture;
    private Button btnFlash;
    private Button btnJenisLaporan;
    private Button btnImageDiscard;

    private TextView tvJenisLaporan;
    private TextView tvFab;

    private LocationListener mLocationListener;
    private GPSTracker gpsTracker;
    private LatLng currentLocation;
    private GoogleMap mMap;
    private CameraView cameraView;

    private View cameraViewHolder;
    private View imagePreview;

    private ImageView imgCaptured;

    //0 = lapor, 1 = sampah
    private int jenisLaporan = 0;
    private int mShortAnimationDuration = 5;

    public User user;

    private long captureStartTime;

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

        fab_home = findViewById(R.id.fab_home);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCloseCamera = findViewById(R.id.btnCloseCamera);
        cameraView = findViewById(R.id.cameraView);
        btnCapture = findViewById(R.id.btnCapture);
        btnFlash = findViewById(R.id.btnFlash);
        btnJenisLaporan = findViewById(R.id.btnJenisLaporan);
        tvJenisLaporan = findViewById(R.id.tvJenisLaporan);
        tvFab = findViewById(R.id.fab_text);
        cameraViewHolder = findViewById(R.id.cameraViewHolder);
        imagePreview = findViewById(R.id.imagePreview);
        imgCaptured = findViewById(R.id.imgCaptured);
        btnImageDiscard = findViewById(R.id.btnImageDiscard);

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
                if (jenisLaporan==0) {
                    jenisLaporan=1;
                    btnJenisLaporan.setBackground(getDrawable(R.drawable.ic_sampah));
                    tvJenisLaporan.setText("JEMPUT SAMPAH");
                }
                else {
                    jenisLaporan=0;
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
            }
        });
        btnCapture.setOnClickListener(captureClick);
    }

    private View.OnClickListener captureClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            captureStartTime = System.currentTimeMillis();
            cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                @Override
                public void callback(CameraKitImage image) {
                    byte[] jpeg = image.getJpeg();
                    long callbackTime = System.currentTimeMillis();
                    cameraViewHolder.setVisibility(View.GONE);
                    imagePreview.setVisibility(View.VISIBLE);
                    //imgCaptured.setImageBitmap(setImage(jpeg));

                }
            });
        }
    };

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

    public void startLapor() {
        fab_home.setVisibility(View.INVISIBLE);
        tvFab.setVisibility(View.INVISIBLE);
        cameraViewHolder.setAlpha(0f);
        cameraViewHolder.setVisibility(View.VISIBLE);
        cameraViewHolder.animate()
                .alpha(1f)
                .setDuration(10)
                .setListener(null);
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
    }

    protected void onResume() {
        super.onResume();
        cameraView.start();
        gpsTracker = new GPSTracker(this);
        currentLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
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

    private void crossfade(final View load, View content) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        content.setAlpha(0f);
        content.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        content.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        load.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        load.setVisibility(View.GONE);
                    }
                });
    }

    public Bitmap setImage(byte[] jpeg) {
        if (jpeg==null) {
            Toasty.error(getApplicationContext(),"Image Error",4,true).show();
            finish();
            return null;
        }
        else {
            Bitmap bm = BitmapFactory.decodeByteArray(jpeg,0,jpeg.length);
            return bm;
        }
    }
}
