package com.hurahura.ray.laporgan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {


    //Password Pattern: Minimum 6 Characters, contains at least 1 number, 1 uppercase letter. Special characters is optional
    private static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}";
    private static final int RC_IMAGE_PICKER = 8000;

    private TextView tvFullName;
    private TextView tvPhone;
    private TextView tvUserEmail;

    private ImageView imgProfile;

    private Button btnEditProfilePicture;
    private Button btnChangePassword;
    private Button btnDeleteAccount;

    private DatabaseReference dbUser;
    private StorageReference imageProfileRef;

    private com.esafirm.imagepicker.model.Image image;

    private Context context;

    public static String KEY;
    public static String imgPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
        setContentView(R.layout.activity_profile);
        setStatusBarColor();

        context = this;

        KEY = getIntent().getStringExtra("KEY");
        imgPath = getIntent().getStringExtra("IMG_PATH");
        getDataFromDatabase();

        //get FirebaseDatabase reference
        dbUser = FirebaseDatabase.getInstance().getReference("user").child(KEY);

        imageProfileRef = FirebaseStorage.getInstance().getReference().child("imageProfile");

        tvFullName = findViewById(R.id.tvFullName);
        tvPhone = findViewById(R.id.tvPhone);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        btnEditProfilePicture = findViewById(R.id.btnEditProfilePicture);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        imgProfile = findViewById(R.id.imgProfile);

        Toasty.info(this, imgPath, 3).show();

        ;

        if (imgPath == null) {
            imgProfile.setImageDrawable(getDrawable(R.drawable.img_empty_profile));
        } else {
            imgProfile.setImageURI(Uri.parse(imgPath));
        }

        tvFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(tvFullName);
            }
        });

        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(tvPhone);
            }
        });

        tvUserEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(tvUserEmail);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePasswordStepOne();
            }
        });

        btnEditProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
                getDataFromDatabase();
            }
        });
    }

    @Override
    protected void onResume() {
        getDataFromDatabase();
        Toasty.info(this, imgPath, 3).show();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data) || requestCode == RC_IMAGE_PICKER) {
            image = ImagePicker.getFirstImageOrNull(data);
            uploadImageProfile(image);
            dbUser.child("image").setValue(imgPath);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setStatusBarColor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void showInputDialog(final TextView t) {
        if (t == tvPhone) {
            new MaterialDialog.Builder(this)
                    .title("Change Phone Number")
                    .content("Min. 12 digits. Max. 13 digits. No Country Code")
                    .inputRangeRes(12, 13, R.color.red_notice)
                    .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                    .input("Format: 08xxxxxxxxxxx", "08", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            t.setText(input);
                            dbUser.child("phone").setValue(input.toString());
                        }
                    }).show();
        } else if (t == tvUserEmail) {
            new MaterialDialog.Builder(this)
                    .title("Change Email Address")
                    .input("Current: " + t.getText(), "", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            if (input.toString().matches(Patterns.EMAIL_ADDRESS.pattern())) {
                                t.setText(input);
                                //Tampung data id lama dulu, terus akun lama di delete. Setelah delete, buat akun baru dengan data dari tampungan
                            } else {
                                Toasty.error(getApplicationContext(), "Email address invalid!", 4, true).show();
                            }
                        }
                    }).show();
        } else {
            new MaterialDialog.Builder(this)
                    .title("Change Name")
                    .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                    .input("Current: " + t.getText(), "", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            t.setText(input);
                            dbUser.child("name").setValue(input.toString());
                        }
                    }).show();
        }
    }

    public void changePasswordStepOne() { //parameter yang dipake nanti => object user yang login saat itu, supaya bisa dapat credential user tsb
        new MaterialDialog.Builder(this)
                .title("Enter Your Current Password")
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input("Current password", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().equals("abc123")) {
                            changePasswordStepTwo();
                        } else {
                            Toasty.error(getApplicationContext(), "Password incorrect", 2, true).show();
                        }
                    }
                }).show();
    }

    public void changePasswordStepTwo() {
        new MaterialDialog.Builder(this)
                .title("Enter New Password")
                .content("Min. 6 alphanumeric chars, at least 1 Uppercase, 1 Number. Special characters is allowed")
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input("New Password", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().matches(PASSWORD_PATTERN)) {
                            Toasty.success(getApplicationContext(), "Password successfully changed", 4, true).show();
                            //masuk ke Firebase Auth untuk update password
                        } else {
                            Toasty.error(getApplicationContext(), "Password invalid", 2, true).show();
                        }
                    }
                }).show();
    }

    public void getDataFromDatabase() {
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                tvFullName.setText(map.get("name"));
                tvPhone.setText(map.get("phone"));
                tvUserEmail.setText(map.get("email"));
                imgPath = map.get("image");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showImagePicker() {
        ImagePicker.create(this)
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle("Images") // folder selection title
                .toolbarImageTitle("Select 1 photo") // image selection title
                .toolbarArrowColor(getResources().getColor(R.color.white)) // Toolbar 'up' arrow color
                .single() // single mode
                .showCamera(false) // show camera or not (true by default)
                .imageDirectory("DCIM") // directory name for captured image  ("Camera" folder by default)
                .imageFullDirectory(Environment.getExternalStorageDirectory().getPath()) // can be full path
                .enableLog(false) // disabling log
                .start(RC_IMAGE_PICKER); // start image picker activity with request code
    }

    public void uploadImageProfile(com.esafirm.imagepicker.model.Image img) {
        final Uri file = Uri.fromFile(new File(img.getPath()));
        StorageReference profileRef = imageProfileRef.child(file.getLastPathSegment());
        UploadTask uploadTask = profileRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(context, "Upload error", 4, true).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dbUser.child("image").setValue(file.getLastPathSegment());
                imgProfile.setImageURI(file);
                getDataFromDatabase();
                Toasty.success(context, "Upload success", 4, true).show();
            }
        });
    }
}
