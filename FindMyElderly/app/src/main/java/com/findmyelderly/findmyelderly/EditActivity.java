package com.findmyelderly.findmyelderly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String elderlyId="";
    private String tempName ="";
    private String tempTel ="";
    private String tempAddress="";
    private String tempFName ="";
    private String tempFTel ="";
    private float tempRadius=0;
    private String currentUserId;
    private com.google.firebase.database.Query mQueryMF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button submit = (Button) findViewById(R.id.button1);
        final EditText tempEdit   = (EditText)findViewById(R.id.editName);
        final EditText tempEdit2   = (EditText)findViewById(R.id.editTel);
        final EditText tempEdit3   = (EditText)findViewById(R.id.editAddress);

        final EditText tempEdit4   = (EditText)findViewById(R.id.editFName);
        final EditText tempEdit5   = (EditText)findViewById(R.id.editFTel);
        final EditText tempEdit6   = (EditText)findViewById(R.id.editRadius);




        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempName =  tempEdit.getText().toString();
                tempTel = tempEdit2.getText().toString();
                tempAddress = tempEdit3.getText().toString();
                tempFName=tempEdit4.getText().toString();
                tempFTel=tempEdit5.getText().toString();
                if(!tempEdit6.getText().toString().equals("")){
                tempRadius=Float.parseFloat(tempEdit6.getText().toString());}
                //get the current userid

                mAuth = FirebaseAuth.getInstance();
                user = mAuth.getCurrentUser();
                currentUserId = user.getUid();

                //mQueryMF = mDatabase.child("users").orderByChild("familyId").equalTo(currentUserId);

                FirebaseDatabase.getInstance().getReference().child("edit").orderByChild("email").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            elderlyId =dataSnapshot.child("elderlyId").getValue(String.class);

                //save text in edittext into the firebase
                if(!tempName.equals(""))
                    mDatabase.child("users").child(elderlyId).child("userName").setValue(tempName);
                if(!tempTel.equals(""))
                    mDatabase.child("users").child(elderlyId).child("Tel").setValue(tempTel);
                if(!tempAddress.equals(""))
                    mDatabase.child("users").child(elderlyId).child("address").setValue(tempAddress);
                        if(tempRadius!=0.0f)
                            mDatabase.child("users").child(elderlyId).child("radius").setValue(tempRadius);

                        if(!tempFName.equals(""))
                            mDatabase.child("users").child(currentUserId).child("userName").setValue(tempFName);
                        if(!tempFTel.equals(""))
                            mDatabase.child("users").child(currentUserId).child("Tel").setValue(tempFTel);
            }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                startActivity(new Intent(EditActivity.this, MainActivity_Family.class));

    }
});
    }
}



