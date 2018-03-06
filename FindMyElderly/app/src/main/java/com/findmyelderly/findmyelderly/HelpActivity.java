package com.findmyelderly.findmyelderly;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import com.bumptech.glide.Glide;

public class HelpActivity extends AppCompatActivity {
    private ImageButton addButton;
    private ImageView family;
    private ImageView police;
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;
    private Uri uri;
    boolean upload;
    private StorageReference riversRef;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String familyId = "";
    private String userId = "";
    private Boolean mAccept=false;
    private URL domain;
    private String currentUserId;
    private com.google.firebase.database.Query mQueryMF;
    private ProgressDialog progressDialog;
    public String phoneno;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_page);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();
        currentUserId = user.getUid();


        addButton = (ImageButton) findViewById(R.id.add);
        family = (ImageView) findViewById(R.id.familyicon);
        police = (ImageView) findViewById(R.id.police);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("users").child(userId).child("help").setValue(true);
                Toast.makeText(HelpActivity.this, "已發送求救訊息", Toast.LENGTH_LONG).show();
                calling();
            }
        });

        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //callPolice();
            }
        });




        mDatabase.child("users").child(user.getUid()).child("familyicon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String userprofile = snapshot.getValue(String.class);
                try{
                    if (!userprofile.equals("")) {
                        ImageView family = (ImageView) findViewById(R.id.familyicon);
                        try {
                            domain = new URL(userprofile);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        Glide.with(HelpActivity.this)
                                .load(domain)
                                .override(450, 450)
                                .into(family);
                    }}catch(NullPointerException killer){

                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        });
    }

    public void callPolice(){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + 999));
        startActivity(intent);
    }

    public void calling(){

        mDatabase.child("users").orderByChild("elderlyId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    phoneno =userSnapshot.child("Tel").getValue(String.class);

                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneno));
                startActivity(intent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            upload = true;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 450, 450, true);
                family.setImageBitmap(resized);
                uploadFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadFile() {
        //if there is a file to upload
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            riversRef = mStorageRef.child("images/" + userId + ".jpg");
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("link", uri.toString());
                                    //put link to database

                                            mDatabase.child("users").child(userId).child("familyicon").setValue(uri.toString());


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }




}
