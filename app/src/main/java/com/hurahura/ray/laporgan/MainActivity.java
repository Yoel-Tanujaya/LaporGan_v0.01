package com.hurahura.ray.laporgan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    //Password Pattern: Minimum 6 Characters, contains at least 1 number, 1 uppercase letter. Special characters is optional
    private static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}";

    public static FirebaseAuth mAuth;

    public static GoogleSignInClient mGoogleSignInClient;

    public static User USER;

    private String imgPath;

    private EditText txtEmail;
    private EditText txtPass;
    private EditText txtName;

    private Button btnLogin;
    private Button btnGoogleLogin;
    private Button btnSignup;

    private Button btnNewUser;
    private Button btnForgotPass;
    private Button btnBackToLogin;

    private TextView tvName;
    private TextView tvSeparator;

    private ImageView imgLogo;

    private DatabaseReference dbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //request permission when launch
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        //set statusbar color to desired color -> #00a8e8 / blue4
        setStatusBar();

        //get user from firebase authentication for choosing the activity
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //get FirebaseDatabase reference
        dbUser = FirebaseDatabase.getInstance().getReference("user");

        if (currentUser!=null) {
            getImageFromDatabase(currentUser.getUid());
            HomeActivity.KEY = currentUser.getUid();
            startActivity(new Intent(getBaseContext(),HomeActivity.class));
            finish();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            setTheme(android.R.style.Theme_Material_NoActionBar);
            setContentView(R.layout.activity_main);

            //---------------------------------------------EditText------------------------------------------
            txtEmail = (EditText) findViewById(R.id.txtEmail);
            txtPass = (EditText) findViewById(R.id.txtPass);
            txtName = (EditText) findViewById(R.id.txtName);

            //---------------------------------------------TextView------------------------------------------
            tvName = (TextView) findViewById(R.id.tvNameHistory);
            tvSeparator = (TextView) findViewById(R.id.tvSeparator);

            //---------------------------------------------Button------------------------------------------
            btnLogin = (Button) findViewById(R.id.btnLogin);
            btnGoogleLogin = (Button) findViewById(R.id.btnGoogle);
            btnSignup = (Button) findViewById(R.id.btnSignup);
            btnNewUser = (Button) findViewById(R.id.btnNewUser);
            btnForgotPass = (Button) findViewById(R.id.btnForgotPass);
            btnBackToLogin = (Button) findViewById(R.id.btnBackToLogin);

            imgLogo = findViewById(R.id.imgLogo);

            //GSO object for Firebase Authentication
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });

            btnNewUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signInTrigger(true);
                }
            });

            btnBackToLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signInTrigger(false);
                }
            });

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signInEmail(txtEmail.getText().toString(),txtPass.getText().toString());
                }
            });

            btnSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = txtEmail.getText().toString();
                    String pass = txtPass.getText().toString();
                    String name = txtName.getText().toString();
                    registerValidation(email,pass,name);
                }
            });

            imgLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getBaseContext(),AdminActivity.class));
                    finish();
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                Log.w(TAG, "Google Sign in Failed",e);
            }
        }
    }

    //Sign in using Google Sign-In
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG,"firebaseAuthWithGoogle: " + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG,"signInWithCredential:success");
                            FirebaseUser users = mAuth.getCurrentUser();
                            USER = new User(users.getUid(),users.getEmail(),users.getDisplayName());
                            addUserToDatabase();
                            Intent intent = new Intent(getBaseContext(),HomeActivity.class);
                            intent.putExtra("IMG_PATH",USER.getImage());
                            HomeActivity.KEY = USER.getId();
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Log.w(TAG,"signInWithCredential:failure", task.getException());
                            Toast.makeText(getBaseContext(),"Authentication Failed",Toast.LENGTH_LONG);
                        }
                    }
                });
    }

    //Register using email address
    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser users = mAuth.getCurrentUser();
                            USER = new User(users.getUid(),users.getEmail(),txtName.getText().toString());
                            addUserToDatabase();
                            Intent intent = new Intent(getBaseContext(),HomeActivity.class);
                            HomeActivity.KEY = USER.getId();
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Sign-in using email address
    public void signInEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser users = mAuth.getCurrentUser();
                            USER = new User(users.getUid(),users.getEmail(),users.getDisplayName());
                            HomeActivity.KEY = USER.getId();
                            Toasty.success(getApplicationContext(),"Login Success",4,true).show();
                            Intent intent = new Intent(getBaseContext(),HomeActivity.class);
                            intent.putExtra("IMG_PATH",USER.getImage());
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toasty.error(getApplicationContext(),"Login Failed!",4,true).show();
                        }
                    }
                });
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public void registerValidation(String email, String pass, String name) {
        if (!email.isEmpty()&&!pass.isEmpty()&&!name.isEmpty()&&email.matches(Patterns.EMAIL_ADDRESS.pattern())&&pass.matches(PASSWORD_PATTERN)) {
            createAccount(email,pass);
        }
        else {
            if (email.isEmpty() || !email.matches(Patterns.EMAIL_ADDRESS.pattern())) {
                txtEmail.setError("Invalid Email Address");
            }
            if (pass.isEmpty() || !pass.matches(PASSWORD_PATTERN)) {
                txtPass.setError("Must be at least 6 characters with 1 uppercase letter and 1 digit number");
            }
            if (name.isEmpty() || name.length()<4) {
                txtName.setError("Must be 4 characters or more");
            }
            Toasty.error(getApplicationContext(),"Registration Failed",3,true).show();
        }
    }

    public void signInTrigger(boolean isSignIn) {
        if (isSignIn) {
            btnSignup.setVisibility(View.VISIBLE);
            btnBackToLogin.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
            txtName.setVisibility(View.VISIBLE);
            //--------------------------------------------
            btnGoogleLogin.setVisibility(View.INVISIBLE);
            btnForgotPass.setVisibility(View.INVISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
            btnNewUser.setVisibility(View.INVISIBLE);
            tvSeparator.setVisibility(View.INVISIBLE);
            txtEmail.setText("");
            txtPass.setText("");

        }
        else {
            btnSignup.setVisibility(View.INVISIBLE);
            btnBackToLogin.setVisibility(View.INVISIBLE);
            tvName.setVisibility(View.INVISIBLE);
            txtName.setVisibility(View.INVISIBLE);
            //--------------------------------------------
            btnGoogleLogin.setVisibility(View.VISIBLE);
            btnForgotPass.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            btnNewUser.setVisibility(View.VISIBLE);
            tvSeparator.setVisibility(View.VISIBLE);
            txtEmail.setText("");
            txtPass.setText("");
        }
    }

    public void setStatusBar() {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.blue4));
    }

    public void addUserToDatabase() {
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(USER.getId())) {
                    Toasty.success(getApplicationContext(),"Welcome back, "+USER.getName(),4).show();
                }
                else {
                    final String name;
                    if (USER.getName()==null) {
                        name = txtName.getText().toString();
                    }
                    else {
                        name = USER.getName();
                    }
                    dbUser.child(USER.getId());
                    dbUser.child(USER.getId()).child("name").setValue(name);
                    dbUser.child(USER.getId()).child("email").setValue(USER.getEmail());
                    dbUser.child(USER.getId()).child("phone").setValue("");
                    dbUser.child(USER.getId()).child("image").setValue("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getImageFromDatabase(final String uid) {
        dbUser.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                imgPath = map.get("image");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
