package com.findmyelderly.findmyelderly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String account;
    private String password;
    private TextInputLayout accoutLayout;
    private TextInputLayout passwordLayout;
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private CheckBox checkBoxRememberMe;
    private FirebaseUser user;
    private String userId;
    private boolean mlogin = false;

    private static String PREFS = "PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        accountEdit = (EditText) findViewById(R.id.account_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        accoutLayout = (TextInputLayout) findViewById(R.id.account_layout);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
        passwordLayout.setErrorEnabled(true);
        accoutLayout.setErrorEnabled(true);
        loginBtn = (Button) findViewById(R.id.login_button);
        checkBoxRememberMe = (CheckBox) findViewById(R.id.checkBox);

        receiveData();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if(TextUtils.isEmpty(account)){
                    accoutLayout.setError("Please enter e-mail");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    passwordLayout.setError("Please enter password");
                    return;
                }
                if(checkBoxRememberMe.isChecked()){
                    saveData();
                }
                accoutLayout.setError("");
                passwordLayout.setError("");
                mAuth.signInWithEmailAndPassword(account, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                                    user = mAuth.getCurrentUser();
                                    userId = user.getUid();

                                    /*                                   mlogin = true;
                                    if(mlogin) {
                                        databaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                for (DataSnapshot child : snapshot.getChildren()) {
                                                    SignupActivity.User users = child.getValue(SignupActivity.User.class);
                                                    if (users.getEmail().equalsIgnoreCase(email)) {
                                                        if (users.getType().equalsIgnoreCase("elderly")) {
                                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                            mlogin = false;
                                                            finish();
                                                        } else {
                                                            startActivity(new Intent(LoginActivity.this, MainActivity_Family.class));
                                                            mlogin = false;
                                                            finish();
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }*/

                                    mDatabase.child("users").child(user.getUid()).child("type").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            String type = snapshot.getValue(String.class);
                                            try {
                                                if (type.equals("elderly")) {
                                                    Intent intent = new Intent();
                                                    intent.setClass(LoginActivity.this, MainActivity.class); //jump back to home page
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else{
                                                    Intent intent = new Intent();
                                                    intent.setClass(LoginActivity.this, MainActivity_Family.class); //jump back to home page
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }catch(Exception e){
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            // Getting Post failed, log a message
                                            // ...
                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });
    }

    public void saveData(){
        String account = accountEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("USERNAME", account);
        editor.putString("PASSWORD", password);
        editor.commit();
    }

    public void receiveData(){
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        String account = preferences.getString("USERNAME", null);
        String password = preferences.getString("PASSWORD", null);

        accountEdit.setText(account);
        passwordEdit.setText(password);

    }
}