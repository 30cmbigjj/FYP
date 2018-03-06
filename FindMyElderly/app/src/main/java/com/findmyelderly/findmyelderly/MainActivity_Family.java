package com.findmyelderly.findmyelderly;

//import android.app.FragmentTransaction;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity_Family extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        View.OnClickListener {

    private Button logout;
    private Button edit;
    private ImageButton buttonCurrent;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private double longitude;
    private double latitude;
    private String currentUserId;
    private com.google.firebase.database.Query mQueryMF;
    private TextView tt;
    private String dateTime;
    private String address;
    private int batteryLV;
    private int temp_batteryLV = 0;
    private boolean batteryLVChecked;
    private boolean help = false;
    private boolean outGeo=false;
    private String userName="長者";
    private String elderlyId="";
    private Marker m;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private float radius=0.0f;
    private double templat;
    private double templon;
    private String home="";

    //Our Map
    private GoogleMap mMap;

    //notification
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;
    private static final int uniqueID2 = 456;
    private static final int uniqueID3 = 123;
    private static final String TAG = "MainActivity_Family";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__family);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        edit = (Button) findViewById(R.id.button2);
        logout = (Button) findViewById(R.id.logout);
        buttonCurrent = (ImageButton) findViewById(R.id.buttonCurrent);
        tt = (TextView) findViewById(R.id.tt);
        buttonCurrent.setOnClickListener(this);

        notification = new NotificationCompat.Builder(this);
        //notification.setAutoCancel(true);



        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity_Family.this, HomeActivity.class));
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragment = new FragmentEditList();
                FragmentManager manager = getSupportFragmentManager();
                final FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();
            }
        });


    }


    private void checkElderlyBatteryLV(int batteryLV,String name){
        if(batteryLV<=40 && batteryLV%5 == 0){
            if(batteryLV!=temp_batteryLV) {
                temp_batteryLV = batteryLV;

                if (name== ""){
                    name="長者";
                }
                //local notification
                //notification body
                notification.setSmallIcon(R.drawable.ic_clock);
                notification.setTicker(name+"的手機電量低下!");
                notification.setWhen(System.currentTimeMillis());
                notification.setContentTitle(name+"的手機電量低下!");
                notification.setContentText(name+"的手機電量只餘 "+batteryLV+"% !");
                //Notification ElderlyLowBatteryAlert = new Notification();

                //intent to get to the page
                Intent intent = new Intent(this, MainActivity_Family.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                //sending out notification
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.notify(uniqueID, notification.build());


                //FCM notification
                String token = FirebaseInstanceId.getInstance().getToken();
                Toast.makeText(MainActivity_Family.this, "你有一則新通知", Toast.LENGTH_SHORT).show();
                Log.w("",token);
            }
        }
    }

    private void checkHelp(boolean seekHelp,String name) {
        if (name== ""){
            name="長者";
        }

        if (seekHelp == true) {
            notification.setSmallIcon(R.drawable.ic_clock);
            notification.setTicker(name+"需要幫助");
            notification.setWhen(System.currentTimeMillis());
            notification.setContentTitle(name+"需要你的幫助");
            notification.setContentText(name+"迷路了!");
            //Notification ElderlyLowBatteryAlert = new Notification();

            //intent to get to the page
            Intent intent = new Intent(this, MainActivity_Family.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(pendingIntent);

            //sending out notification
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nm.notify(uniqueID2, notification.build());


            //FCM notification
            String token = FirebaseInstanceId.getInstance().getToken();
            Toast.makeText(MainActivity_Family.this, "你有一則新通知!", Toast.LENGTH_SHORT).show();
            Log.w("",token);
            mDatabase.child("users").child(currentUserId).child("elderlyId").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    elderlyId =dataSnapshot.getValue(String.class);
                    mDatabase.child("users").child(elderlyId).child("help").setValue(false);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }


    private void checkGeo(boolean out,String name) {
        if (name== ""){
            name="長者";
        }

        if (out == true) {
            notification.setSmallIcon(R.drawable.ic_clock);
            notification.setTicker("注意!");
            notification.setWhen(System.currentTimeMillis());
            notification.setContentTitle(name+"需要你的注意");
            notification.setContentText(name+"離開了安全圍欄!");
            //Notification ElderlyLowBatteryAlert = new Notification();

            //intent to get to the page
            Intent intent = new Intent(this, MainActivity_Family.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(pendingIntent);

            //sending out notification
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nm.notify(uniqueID3, notification.build());


            //FCM notification
            String token = FirebaseInstanceId.getInstance().getToken();
            Toast.makeText(MainActivity_Family.this, "你有一則新通知!", Toast.LENGTH_SHORT).show();
            Log.w("",token);
            mDatabase.child("users").child(currentUserId).child("elderlyId").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    elderlyId =dataSnapshot.getValue(String.class);
                    mDatabase.child("users").child(elderlyId).child("outGeo").setValue(false);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }


    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("", strReturnedAddress.toString());
            } else {
                Log.w("", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("", "Canont get Address!");
        }
        return strAdd;
    }

    private void getCurrentLocation() {
        mQueryMF = mDatabase.child("users").orderByChild("familyId").equalTo(currentUserId);

        mQueryMF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    latitude = userSnapshot.child("latitude").getValue(Double.class);
                    longitude = userSnapshot.child("longitude").getValue(Double.class);
                    dateTime = userSnapshot.child("dateTime").getValue(String.class);
                    address = getCompleteAddressString(latitude,longitude);
                    home=userSnapshot.child("address").getValue(String.class);
                    batteryLV = userSnapshot.child("batteryLV").getValue(Integer.class);
                    help = userSnapshot.child("help").getValue(Boolean.class);
                    outGeo=userSnapshot.child("outGeo").getValue(Boolean.class);
                    userName=userSnapshot.child("userName").getValue(String.class);
                    radius=userSnapshot.child("radius").getValue(Float.class);


                }
                //String to display current latitude and longitude
                //DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                //dateTime = df.format(dateTime);
                checkHelp(help,userName);
                checkGeo(outGeo,userName);
                checkElderlyBatteryLV(batteryLV,userName);


                //String msg = latitude + ", " + longitude+ ", last updated: "+dateTime;
                String msg = address+"最近更新: "+dateTime +'\n' +"電池還餘: "+batteryLV+"%";
                String title = "現在位置";
                if (userName==""){
                    title = "老人"+title;
                }else{
                    title = userName+title;
                }

                String snippet = address+'\n'+"最近更新: "+dateTime +'\n' +"電池還餘: "+batteryLV+"%";
                tt.setText(msg);
                //Creating a LatLng Object to store Coordinates
                LatLng latLng = new LatLng(latitude, longitude);
                //Adding marker to map
                /*
                if (m != null) {
                    m.remove();
                }
                */
                mMap.clear();
                //add the custom maker label to the map
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MainActivity_Family.this));

                Marker label = mMap.addMarker(new MarkerOptions()
                        .position(latLng) //setting position
                        //.draggable(true) //Making the marker draggable
                        .title(title)
                        .snippet(snippet)
                        );
                Log.d(TAG,msg);
                label.showInfoWindow();

                //add circle for geofencing
                LatLng result = getLocationFromAddress(home);
                templat = result.latitude;
                templon = result.longitude;

                Circle circle=mMap.addCircle(new CircleOptions()
                        .center(new LatLng(templat, templon))
                        .strokeColor(Color.BLUE)
                        .radius(radius)); // In meters


                //Moving the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                //Animating the camera
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(22.316333,114.180298);
        //mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setOnMarkerDragListener(this);

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


    @Override
    public void onConnected(Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /*@Override
    public void onMapLongClick(LatLng latLng) {
        //Clearing all the markers
        mMap.clear();

        //Adding a new marker to the current pressed position
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
    }*/

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        //Getting the coordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
    }

    @Override
    public void onClick(View v) {
        if (v == buttonCurrent) {
            getCurrentLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
    }

}