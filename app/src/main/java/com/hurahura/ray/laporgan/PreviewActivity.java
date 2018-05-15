package com.hurahura.ray.laporgan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class PreviewActivity extends AppCompatActivity {
    private ActionBar actionBar;

    private ImageView imgPreview;
    private TextView tvLocation;
    private TextView tvDescription;
    private TextView tvJenisLaporan;

    private Button btnSubmit;

    private byte[] imgLaporan;

    private StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("imageReport");

    public static String JENIS_LAPORAN;
    public static String LOCATION;
    public static String KEY;

    public static Double LOC_LAT;
    public static Double LOC_LNG;

    public static long count;

    DatabaseReference dbReport = FirebaseDatabase.getInstance().getReference("report");
    GeoFire geoFire = new GeoFire(dbReport.child("location"));


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
        setContentView(R.layout.activity_preview);
        setStatusBarColor();

        KEY = getIntent().getStringExtra("KEY");
        LOC_LAT = getIntent().getDoubleExtra("LOC_LAT",0);
        LOC_LNG = getIntent().getDoubleExtra("LOC_LNG",0);

        Toasty.info(getApplicationContext(),LOC_LAT+" - "+LOC_LNG,4).show();

        imgPreview = findViewById(R.id.imgPreview);

        tvLocation = findViewById(R.id.tvLocationPreview);
        tvDescription = findViewById(R.id.tvDescriptionHistory);
        tvJenisLaporan = findViewById(R.id.tvJenisLaporanPreview);

        btnSubmit = findViewById(R.id.btnSubmit);

        if (tvDescription.getText().toString().equalsIgnoreCase(getString(R.string.default_description))) {
            btnSubmit.setEnabled(false);
        }

        byte[] jpeg = ResultHolder.getImage();

        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
        imgLaporan = ResultHolder.getImage();

        if (bitmap == null) {
            Toasty.error(getApplicationContext(), "Bitmap decode error!", 4, true).show();
            finish();
            return;
        }

        imgPreview.setImageBitmap(bitmap);

        tvLocation.setText(LOCATION);
        tvJenisLaporan.setText(JENIS_LAPORAN);

        tvDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageToFirebase(tvJenisLaporan.getText().toString());
            }
        });

        dbReport.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count=dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setStatusBarColor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void showInputDialog() {
        new MaterialDialog.Builder(this)
                .title("Deskripsi Laporan")
                .titleColor(getResources().getColor(R.color.blue4))
                .buttonRippleColorRes(R.color.blue3)
                .inputRangeRes(0,160,R.color.red_notice)
                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                .input("", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        tvDescription.setText(input);
                        btnSubmit.setEnabled(true);
                    }
                }).show();
    }

    public File byteToFileConversion() {
        File image = null;
        try {
            FileOutputStream fos = new FileOutputStream(image);
            fos.write(imgLaporan);
            fos.flush();
            fos.close();
            image = File.createTempFile("test",".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }


    public void uploadImageToFirebase(String tx){
        final String timeStamp;
        if (tx.equalsIgnoreCase(getString(R.string.lapor))) {
            timeStamp = new SimpleDateFormat("dd_MMMM_yyyy-HH_mm_ss").format(new Date());
            StorageReference fileRef = imgRef.child("LAPOR_"+timeStamp+".jpg");
            fileRef.putBytes(imgLaporan,new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String t = new SimpleDateFormat("dd MMM yyyy").format(new Date());
                    dbReport.child(t);
                    dbReport.child(t).child(KEY+"_"+count);
                    dbReport.child(t).child(KEY+"_"+count).child("userID").setValue(KEY);
                    dbReport.child(t).child(KEY+"_"+count).child("report_number").setValue(count);
                    dbReport.child(t).child(KEY+"_"+count).child("time").setValue(timeStamp);
                    dbReport.child(t).child(KEY+"_"+count).child("photos").setValue(taskSnapshot.getDownloadUrl().toString());
                    dbReport.child(t).child(KEY+"_"+count).child("status").setValue(0);
                    dbReport.child(t).child(KEY+"_"+count).child("description").setValue(tvDescription.getText().toString());
                    dbReport.child(t).child(KEY+"_"+count).child("location").child("latitude").setValue(LOC_LAT);
                    dbReport.child(t).child(KEY+"_"+count).child("location").child("longitude").setValue(LOC_LNG);
                    dbReport.child(t).child(KEY+"_"+count).child("report").setValue(tvJenisLaporan.getText().toString());
                    count++;
                    Toasty.success(getApplicationContext(),"Report sent",4,true).show();
                    HomeActivity.KEY = KEY;
                    startActivity(new Intent(PreviewActivity.this,HomeActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(getApplicationContext(),"Failed to send report. Please try again",4,true).show();
                }
            });
        }
        else if (tx.equalsIgnoreCase(getString(R.string.sampah))) {
            timeStamp = new SimpleDateFormat("dd_MMMM_yyyy-HH_mm_ss").format(new Date());
            StorageReference fileRef = imgRef.child("SAMPAH_"+timeStamp+".jpg");
            fileRef.putBytes(imgLaporan,new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String t = new SimpleDateFormat("dd MMM yyyy").format(new Date());
                    dbReport.child(t);
                    dbReport.child(t).child(KEY+"_"+count);
                    dbReport.child(t).child(KEY+"_"+count).child("userID").setValue(KEY);
                    dbReport.child(t).child(KEY+"_"+count).child("report_number").setValue(count);
                    dbReport.child(t).child(KEY+"_"+count).child("time").setValue(timeStamp);
                    dbReport.child(t).child(KEY+"_"+count).child("photos").setValue(taskSnapshot.getDownloadUrl().toString());
                    dbReport.child(t).child(KEY+"_"+count).child("status").setValue(0);
                    dbReport.child(t).child(KEY+"_"+count).child("description").setValue(tvDescription.getText().toString());
                    dbReport.child(t).child(KEY+"_"+count).child("location").child("latitude").setValue(LOC_LAT);
                    dbReport.child(t).child(KEY+"_"+count).child("location").child("longitude").setValue(LOC_LNG);
                    dbReport.child(t).child(KEY+"_"+count).child("report").setValue(tvJenisLaporan.getText().toString());
                    count++;
                    Toasty.success(getApplicationContext(),"Report sent",4,true).show();
                    HomeActivity.KEY = KEY;
                    startActivity(new Intent(PreviewActivity.this,HomeActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(getApplicationContext(),"Failed to send report. Please try again",4,true).show();
                }
            });
        }

    }
}
