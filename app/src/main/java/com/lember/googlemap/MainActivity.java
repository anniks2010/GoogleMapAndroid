package com.lember.googlemap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient client;
    private TextInputEditText etEmail, etPassword;
    private ConstraintLayout constraintLayout;
    private static final String TAG = "FIREBASE";
    private FirebaseAuth mAuth;
    boolean isEmailValid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = LocationServices.getFusedLocationProviderClient(this);
        etEmail = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        constraintLayout = findViewById(R.id.mainView);
        mAuth = FirebaseAuth.getInstance();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("LOCATION: ", "Location permission granted.");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LOCATION: ", "Permission granted");
            }
        }
    }

    public void onMap(View view) {
        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
    }

    ///maps2
    public void onEmail(View view) {
        String txtEmail = etEmail.getText().toString().trim();
        String txtPassword = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)) {
            etEmail.setError("Field needs to be filled");
            etPassword.setError("Field needs to be filled");
        } else login(txtEmail, txtPassword);
    }

    private void login(String txtEmail, String txtPassword) {
        mAuth.signInWithEmailAndPassword(txtEmail, txtPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            } else
                                Snackbar.make(constraintLayout, "Email is not verified", Snackbar.LENGTH_LONG).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.i(TAG, "User already logged in");
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        } else {
            Log.i(TAG, "No users are logged in");
        }
    }

    public void onRegister(View view) {
        String txtEmail = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(txtEmail))
            etEmail.setError("Fields needs  to be filled");
        else if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()) {
            etEmail.setError("You need to enter a valid email");
            isEmailValid = false;
        } else {
            isEmailValid = true;
            Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
            register.putExtra("register", txtEmail);
            startActivity(register);
            finish();
        }
    }

    public void onPasswordForgot(View view) {
        String txtEmail = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(txtEmail))
            etEmail.setError("Fields needs  to be filled");
        else if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()) {
            etEmail.setError("You need to enter a valid email");
            isEmailValid = false;
        } else {
            isEmailValid = true;
            mAuth.useAppLanguage();
            mAuth.sendPasswordResetEmail(txtEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.i(TAG,"Email was successfully sent!");
                        Snackbar.make(constraintLayout,"An email was sent to the provided email address",Snackbar.LENGTH_LONG).show();
                    }else{
                        Log.i(TAG,"An error ocurred while sending email!");
                        Snackbar.make(constraintLayout,"Error sending email",Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

        @Override
        protected void onStart () {
            super.onStart();
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }
    }
