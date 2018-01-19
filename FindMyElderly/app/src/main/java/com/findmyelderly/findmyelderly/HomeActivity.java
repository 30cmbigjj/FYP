package com.findmyelderly.findmyelderly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private Button login;
    private Button login_family;
    private Button signUp;
    private FirebaseAuth mAuth;
    public static Boolean isElderly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            setContentView(R.layout.activity_home);
            login = (Button) findViewById(R.id.login);
            //login_family = (Button) findViewById(R.id.login_family);
            signUp = (Button) findViewById(R.id.sign_up);

            /*login_family.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);

                }
            });*/

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);

                }
            });
            signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(HomeActivity.this, SignUpActivity_Elderly.class);
                    startActivity(intent);
                }
            });
        } else {   //keep login as elderly or family
            mDatabase.child("users").child(user.getUid()).child("type").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String type = snapshot.getValue(String.class);
                    try {
                        if (type.equals("elderly")) {
                            Intent intent = new Intent();
                            intent.setClass(HomeActivity.this, MainActivity.class); //jump back to home page
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent();
                            intent.setClass(HomeActivity.this, MainActivity_Family.class); //jump back to home page
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    // ...
                }
            });

        }
    }
}