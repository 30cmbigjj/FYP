package com.findmyelderly.findmyelderly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
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

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,OnConnectionFailedListener,ResultCallback<Status>{

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;

    private ImageButton helpButton;
    private Button logout;
    private Button add;
    private ImageButton homeButton;
    private Button map;
    private TextView cc;
    private String dateTime;
    //Google ApiClient
    //private GoogleApiClient googleApiClient;

    private double longitude;
    private double latitude;

    private String userId = "";
    private String currentUserId;

    private TextView batteryLV;
    private String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private com.google.firebase.database.Query mQuery;
    public String address = "";
    public float radius = 0.0f;

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 48;

    /**
     * For this sample, geofences expire after 48 hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helpButton = (ImageButton) findViewById(R.id.help);
        homeButton = (ImageButton) findViewById(R.id.home);
        logout = (Button) findViewById(R.id.logout);
        //add = (Button) findViewById(R.id.geofence);
        cc = (TextView) findViewById(R.id.cc);

        batteryLV = (TextView) findViewById(R.id.batteryLV);
        registerReceiver(this.batteryInformationReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        user = mAuth.getCurrentUser();
        currentUserId = user.getUid();
        mQuery = mDatabase.child("users").child(currentUserId);



        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this,Maps.class));
                unregisterReceiver(broadcastReceiver);
                unregisterReceiver(batteryInformationReceiver);
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


        startService(new Intent(MainActivity.this,Maps.class));
       /* if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {*/
            ActivityCompat.requestPermissions(
                    this, // Activity
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);

        mGeofenceList = new ArrayList<Geofence>();

        populateGeofenceList();
        buildGoogleApiClient();




    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnecting()||mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mGoogleApiClient.isConnecting()||!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }
    //2.449484, -76.594959

    /*
    Override methods Google
     */

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onResult(Status status) {
        if(status.isSuccess()){
            Toast.makeText(this, "已設定圍欄", Toast.LENGTH_SHORT).show();

        }else {
            String errorMessage = GeofenceErrorMessages.getErrorStr(this, status.getStatusCode());
            Log.e("", "error");
        }

    }

    public static final HashMap<String,LatLng> POPAYAN_LANDMARKS=new HashMap<String, LatLng>();
    static {

        POPAYAN_LANDMARKS.put("", new LatLng(22.3751429, 114.1115573));}

    /*
    My methods
     */


    private void populateGeofenceList() {

        mQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                address = dataSnapshot.child("address").getValue(String.class);
                radius = dataSnapshot.child("radius").getValue(Float.class);

                LatLng result = getLocationFromAddress(address);
                latitude = result.latitude;
                longitude = result.longitude;
                for (Map.Entry<String, LatLng> entry : POPAYAN_LANDMARKS.entrySet()) {
                    mGeofenceList.add(new Geofence.Builder().setRequestId(entry.getKey())
                            .setCircularRegion(latitude,longitude,radius)
                            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build());
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public LatLng getLocationFromAddress(String Address) {
        Geocoder coder = new Geocoder(this);
        LatLng point = null;
        try {
            List<Address> address = coder.getFromLocationName(Address,1);
            if (address == null)
                return null;
            Address location = address.get(0);
            point = new LatLng(location.getLatitude(),location.getLongitude());
        } catch (IOException e) {
            e.getMessage();
        }
        return point;
    }


    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this).
                addConnectionCallbacks(this).
                addApi(LocationServices.API).
                build();


    }
    private PendingIntent getGeofencePendingIntent(){
        Intent intent=new Intent (this,GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void addGeofence(){//Geofences button Handler
        Log.w(TAG,"geofence called~~~~~~~~");
        if(!mGoogleApiClient.isConnected()){
            //Toast.makeText(this, getString(R.string.not_conected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(this);

        }catch (SecurityException sE){
            Log.i("Error",sE.toString());
        }
    }
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder=new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }



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
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();

            batteryLV.setText("現在電力："+level+"%\n");
            if (!Integer.valueOf(level).equals(null)){
                mDatabase.child("users").child(user.getUid()).child("batteryLV").setValue(level);
            }

            if (level<=100 && level%5 == 0){
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
        registerReceiver(broadcastReceiver, new IntentFilter(Maps.str_receiver));
		addGeofence();
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

