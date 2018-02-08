package com.findmyelderly.findmyelderly;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity_Elderly extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String account;
    private String password;
    private TextInputLayout accoutLayout;
    private TextInputLayout passwordLayout;
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button signUpBtn;
    private FirebaseUser user;
    public static String temp_elderlyId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up__elderly);
        initView();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();
        accountEdit = (EditText) findViewById(R.id.account_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        accoutLayout = (TextInputLayout) findViewById(R.id.account_layout);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
        passwordLayout.setErrorEnabled(true);
        accoutLayout.setErrorEnabled(true);
        signUpBtn = (Button) findViewById(R.id.signup_button);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if (TextUtils.isEmpty(account)) {
                    accoutLayout.setError("Please enter e-mail");
                    passwordLayout.setError("");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    accoutLayout.setError("");
                    passwordLayout.setError("Please enter password");
                    return;
                }
                accoutLayout.setError("");
                passwordLayout.setError("");
                mAuth.createUserWithEmailAndPassword(account, password)
                        .addOnCompleteListener(SignUpActivity_Elderly.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(SignUpActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();
                                    writeNewUser(account);
                                    temp_elderlyId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent();
                                    intent.setClass(SignUpActivity_Elderly.this, SignUpActivity.class); //jump to family register page
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity_Elderly.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }


    public static class User {

        private String email;
        private String userName;
        private String type;
        private String familyId;
        private boolean help;
        private float radius; 	//default geofencing radius
        private boolean outGeo; //for family geofencing notification
        private double latitude;
        private double longitude;
        private String dateTime;
        private Integer batteryLV;


        public User(String email) {
            this.email = email;
            this.userName = "";
            this.type="elderly";
            this.familyId="";
            this.help=false;
            this.outGeo=false;
            this.radius=1609; //1 miles
            this.latitude=22.316362;
            this.longitude=114.180287;
            this.dateTime="N/A";
            this.batteryLV=100;

        }

        public String getEmail() {
            return email;
        }
        public String getType(){return type;}
        public String getUserName() {
            return userName;
        }
        public String getFamilyId(){return familyId;}
        public float getRadius(){return radius;}
        public boolean getHelp(){return help;}
        public boolean getoutGeo(){return outGeo;}
        public String getDateTime(){return dateTime;}
        public Integer getBatteryLV() {return batteryLV;}
        public double getLatitude() {return latitude;}
        public double getLongitude() {return longitude;}
    }


    private void writeNewUser(String email) {
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User user = new User(email);
        //mDatabase.child("users").push().setValue(user);
        Log.d("id", userId);
        mDatabase.child("users").child(userId).setValue(user);
    }

}