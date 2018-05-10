package com.hurahura.ray.laporgan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import es.dmoral.toasty.Toasty;

public class PreviewActivity extends AppCompatActivity {
    private ActionBar actionBar;

    private ImageView imgPreview;
    private TextView tvLocation;
    private TextView tvDescription;
    private TextView tvJenisLaporan;
    private Button btnSubmit;

    public static String JENIS_LAPORAN;
    public static String LOCATION;
    public static User USER;

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

        USER = (User) getIntent().getSerializableExtra(PreviewActivity.class.getName());

        imgPreview = findViewById(R.id.imgPreview);

        tvLocation = findViewById(R.id.tvLocationPreview);
        tvDescription = findViewById(R.id.tvDescription);
        tvJenisLaporan = findViewById(R.id.tvJenisLaporanPreview);

        btnSubmit = findViewById(R.id.btnSubmit);

        byte[] jpeg = ResultHolder.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

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
                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                .input("", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        tvDescription.setText(input);
                    }
                }).show();
    }
}
