package com.findmyelderly.findmyelderly;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.os.BatteryManager;


public class MainActivity extends AppCompatActivity /*implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener*/ {

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ImageButton helpButton;
    private Button logout;
    private ImageButton homeButton;
    private Button map;
    private TextView cc;
    private String dateTime;
    //Google ApiClient
    //private GoogleApiClient googleApiClient;

    private double longitude;
    private double latitude;

    private String userId = "";

    //Added by Alan Lee, 15/1/2018, battery level function
    private TextView batteryLV;
    //


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helpButton = (ImageButton) findViewById(R.id.help);
        homeButton = (ImageButton) findViewById(R.id.home);
        logout = (Button) findViewById(R.id.logout);
        cc = (TextView) findViewById(R.id.cc);

        //Added by Alan 15/1/2018
        batteryLV = (TextView) findViewById(R.id.batteryLV);
        registerReceiver(this.batteryInformationReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // ended

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        user = mAuth.getCurrentUser();


        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this,Map.class));
                unregisterReceiver(broadcastReceiver);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });


        startService(new Intent(MainActivity.this,Map.class));

    }


    /*@Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }*/


    //Getting current location
    //private void getCurrentLocation() {
        //Creating a location object
        /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);  */
      /* if (mLastLocation == null) {
            cc.setText("123");
        }
        if (mLastLocation != null) {
            //Getting longitude and latitude
            longitude = mLastLocation.getLongitude();
            latitude = mLastLocation.getLatitude();
            cc.setText("456"+latitude+longitude);
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            //save text in edittext into the firebase
            if (!String.valueOf(latitude).equals(""))
                mDatabase.child("users").child(user.getUid()).child("latitude").setValue(latitude);
            if (!String.valueOf(longitude).equals(""))
                mDatabase.child("users").child(user.getUid()).child("longitude").setValue(longitude);

        }
    }*/


    //Added by Alan 15/1/2018, battery level and noticification
    private BroadcastReceiver batteryInformationReceiver= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            int  health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
            int  level= intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            int  plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
            boolean  present= intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
            int  status= intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
            String  technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            int  temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
            int  voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);

            batteryLV.setText("現在電力："+level+"%\n");
            if (level<=40 && level%5 == 0){

            }
        }
    };



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));

            cc.setText("LOC:   "+latitude+" , "+longitude);
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            //added by alan, 11/12/2017
            DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            //Calendar currentTime = Calendar.getInstance();
            //dateTime = dateTimeFormat.format(Calendar.getInstance().getTime());
            //save text in edittext into the firebase
            if (!String.valueOf(latitude).equals(""))
                mDatabase.child("users").child(user.getUid()).child("latitude").setValue(latitude);
            if (!String.valueOf(longitude).equals("")){
                mDatabase.child("users").child(user.getUid()).child("longitude").setValue(longitude);
                dateTime = dateTimeFormat.format(Calendar.getInstance().getTime());
                mDatabase.child("users").child(user.getUid()).child("dateTime").setValue(dateTime);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Map.str_receiver));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(broadcastReceiver);
    }

    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


}