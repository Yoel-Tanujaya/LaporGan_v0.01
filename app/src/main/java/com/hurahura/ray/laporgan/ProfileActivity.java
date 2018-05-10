package com.hurahura.ray.laporgan;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {
    //Password Pattern: Minimum 6 Characters, contains at least 1 number, 1 uppercase letter. Special characters is optional
    private static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}";

    private TextView tvFullName;
    private TextView tvPhone;
    private TextView tvUserEmail;

    private ImageView imgProfile;

    private Button btnEditProfilePicture;
    private Button btnChangePassword;
    private Button btnDeleteAccount;
    private Button btnSaveProfile;

    public static User USER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
        setContentView(R.layout.activity_profile);
        setStatusBarColor();

        USER = new User(MainActivity.USER.getId(),MainActivity.USER.getEmail(),MainActivity.USER.getName());

        tvFullName = findViewById(R.id.tvFullName);
        tvPhone = findViewById(R.id.tvPhone);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        btnEditProfilePicture = findViewById(R.id.btnEditProfilePicture);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        imgProfile = findViewById(R.id.imgProfile);

        tvFullName.setText(USER.getName());
        tvPhone.setText(USER.getPhone());
        tvUserEmail.setText(USER.getEmail());

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

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    public void setStatusBarColor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void showInputDialog (final TextView t) {
        if (t==tvPhone) {
            new MaterialDialog.Builder(this)
                    .title("Change Phone Number")
                    .content("Min. 12 digits. Max. 13 digits. No Country Code")
                    .inputRangeRes(12,13,R.color.red_notice)
                    .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                    .input("Format: 08xxxxxxxxxxx", "08", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            t.setText(input);
                            //Edit nomor di Firebase Database
                        }
                    }).show();
        }
        else if (t==tvUserEmail) {
            new MaterialDialog.Builder(this)
                    .title("Change Email Address")
                    .input("Current: "+t.getText(), "", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            if (input.toString().matches(Patterns.EMAIL_ADDRESS.pattern())) {
                                t.setText(input);
                                //Edit emailnya di Firebase Auth, dan ganti juga object usernya
                            }
                            else {
                                Toasty.error(getApplicationContext(),"Email address invalid!",4,true).show();
                            }
                        }
                    }).show();
        }
        else {
            new MaterialDialog.Builder(this)
                    .title("Change Name")
                    .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                    .input("Current: "+t.getText(), "", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            t.setText(input);
                            //Edit nama di Firebase Database
                        }
                    }).show();
        }
    }

    public void changePasswordStepOne() { //parameter yang dipake nanti => object user yang login saat itu, supaya bisa dapat credential user tsb
        new MaterialDialog.Builder(this)
                .title("Enter Your Current Password")
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input("Current password","", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().equals("abc123")) {
                            changePasswordStepTwo();
                        }
                        else {
                            Toasty.error(getApplicationContext(),"Password incorrect",2,true).show();
                        }
                    }
                }).show();
    }

    public void changePasswordStepTwo() {
        new MaterialDialog.Builder(this)
                .title("Enter New Password")
                .content("Min. 6 alphanumeric chars, at least 1 Uppercase, 1 Number. Special characters is allowed")
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input("New Password","", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().matches(PASSWORD_PATTERN)) {
                            Toasty.success(getApplicationContext(),"Password successfully changed",4,true).show();
                            //masuk ke Firebase untuk update password
                        }
                        else {
                            Toasty.error(getApplicationContext(),"Password invalid",2,true).show();
                        }
                    }
                }).show();
    }

    public void saveProfile() {
        //connect to Firebase to save profile changes
    }

}
