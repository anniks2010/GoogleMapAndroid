package com.lember.googlemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword, etFirstname, etLastName, etConfirmPassword;
    private ConstraintLayout constraintLayout;
    private static final String TAG = "FIREBASE";
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etEmail = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        etFirstname=findViewById(R.id.etFirsName);
        etLastName=findViewById(R.id.etLastName);
        etConfirmPassword=findViewById(R.id.etConfirmPassword);
        constraintLayout = findViewById(R.id.mainView);
        mAuth = FirebaseAuth.getInstance();

        String registerEmail=getIntent().getExtras().getString("register");
        etEmail.setText(registerEmail);
    }

    public void onRegister2(View view) {
        String first = etFirstname.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String conf_password = etConfirmPassword.getText().toString().trim();

        if(password.length() < 6){
            etPassword.requestFocus();
            Snackbar.make(constraintLayout,"Password must be longer than 6 characters",Snackbar.LENGTH_LONG).show();
        }else{
            if(!password.matches(conf_password)){
                etPassword.setError("Password doesn't match");
                etConfirmPassword.setError("Password doesn't match");
            }else{
                //create new user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "user created");
                                    FirebaseUser user=mAuth.getCurrentUser();
                                    if(user!=null){
                                        String userId=user.getUid();
                                        databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("userId",userId);
                                        hashMap.put("firstname",first);
                                        hashMap.put("lastname",last);
                                        hashMap.put("email",email);
                                        hashMap.put("imageUrl","default");

                                        databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                         if(task.isSuccessful()){
                                                             Log.i(TAG,"Verification email was sent");
                                                             Snackbar.make(constraintLayout,"Verification email was sent to you",Snackbar.LENGTH_LONG).show();
                                                             new CountDownTimer(3000,1000){

                                                                 @Override
                                                                 public void onTick(long l) {

                                                                 }

                                                                 @Override
                                                                 public void onFinish() {
                                                                     mAuth.signOut();
                                                                     startActivity(new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

                                                                 }
                                                             }.start();
                                                         }else{
                                                             Log.i(TAG,"Failure sending verification email");
                                                             Snackbar.make(constraintLayout,"Couldn't send verification email to you.",Snackbar.LENGTH_LONG).show();
                                                         }
                                                        }
                                                    });
                                                }else{
                                                    Log.i(TAG,"Error saving data to firebase database");
                                                    Snackbar.make(constraintLayout,"Couldn't save data.",Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                }else{
                                    Log.i(TAG,"Failure to create user");
                                    Snackbar.make(constraintLayout,"Couldn't create user.",Snackbar.LENGTH_LONG).show();
                                }

                            }
                        });
            }
        }
    }
}