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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {
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
    private Button familyBtn;
    private FirebaseUser user;
    private String userId;
    private String editid;
    private String temp_familyId;
    public String Editid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initView();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //user = mAuth.getCurrentUser();
        //userId = user.getUid();
        accountEdit = (EditText) findViewById(R.id.account_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        accoutLayout = (TextInputLayout) findViewById(R.id.account_layout);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
        passwordLayout.setErrorEnabled(true);
        accoutLayout.setErrorEnabled(true);
        signUpBtn = (Button) findViewById(R.id.signup_button);
        familyBtn = (Button) findViewById(R.id.family);
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
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(SignUpActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();
                                    writeNewUser(account);
                                    user = mAuth.getCurrentUser();
                                    userId = user.getUid();
                                    temp_familyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    mDatabase.child("users").child(userId).child("elderlyId").setValue(SignUpActivity_Elderly.temp_elderlyId);
                                    mDatabase.child("users").child(SignUpActivity_Elderly.temp_elderlyId).child("familyId").setValue(userId);

                                    Intent intent = new Intent();
                                    intent.setClass(SignUpActivity.this, HomeActivity.class); //jump back to home page
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        familyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, FamilyActivity.class));
            }
        });
    }


    public static class User {

        private String email;
        private String userName;
        private String type;
        private String elderlyId;


        public User(String email) {
            this.email = email;
            this.userName = "";
            this.type = "family";
            this.elderlyId = "";


        }

        public String getEmail() {
            return email;
        }

        public String getType() {
            return type;
        }

        public String getUserName() {
            return userName;
        }

        public String getElderlyId() {
            return elderlyId;
        }

    }


    private void writeNewUser(String email) {
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User user = new User(email);
        //mDatabase.child("users").push().setValue(user);
        Log.d("id", userId);
        mDatabase.child("users").child(userId).setValue(user);
    }






}