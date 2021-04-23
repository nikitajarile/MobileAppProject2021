package com.example.logintask;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.UserWriteRecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;


public class dashboard extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted {
    //Constants
    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final int REQUEST_SMS_PERMISSION = 1;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";

    //Views
    private Button signOut;
    private Button alert;
    private ProgressBar progressBar;
    private TextView emailText;
    private TextView username;

    //User Information
    private String contact;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    //Location Classes
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference databaseReference;
    private LocationCallback mLocationCallback;
    Boolean mTrackingLocation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        progressBar = (ProgressBar) findViewById(R.id.progresSign);
        signOut = (Button) findViewById(R.id.btnSignout);
        alert = findViewById(R.id.btn_alert);
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        emailText = (TextView) findViewById(R.id.email_conf);
        username = (TextView) findViewById(R.id.username_conf);
        String name = user.getDisplayName();

        username.setText(name);
        emailText.setText(user.getEmail());
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(dashboard.this, login.class));
                    finish();
                }
            }
        };
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                auth.signOut();
                startActivity(new Intent(dashboard.this, login.class));
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        // Restore the state if the activity is recreated.
        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(
                    TRACKING_LOCATION_KEY);
        }

        // Initialize the location callbacks.
        mLocationCallback = new LocationCallback() {
            /**
             * This is the callback that is triggered when the
             * FusedLocationClient updates your location.
             * @param locationResult The result containing the device location.
             */
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // If tracking is turned on, reverse geocode into an address

                if (mTrackingLocation) {
                    new FetchAddressTask(dashboard.this, dashboard.this)
                            .execute(locationResult.getLastLocation());
                }

            }
        };

        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
                databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + firebaseuser.getUid());

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        User user = dataSnapshot.getValue(User.class);
                        contact = user.getEmergencyContact();
                        Log.i("Emergency contact", contact);

                        if (!mTrackingLocation) {
                            startTrackingLocation(contact);

                        } else {
                            stopTrackingLocation();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

            }
        });

    }

    private void startTrackingLocation(final String contact) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mTrackingLocation = true;
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (ActivityCompat.checkSelfPermission(dashboard.this,Manifest.permission.SEND_SMS)
                                    == PackageManager.PERMISSION_DENIED) {
                                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                                ActivityCompat.requestPermissions(dashboard.this,new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);

                            }
                            mFusedLocationClient.requestLocationUpdates
                                    (getLocationRequest(), mLocationCallback,
                                            null /* Looper */);

                            if (location != null) {
                                mLastLocation = location;

                                Log.i("Location",location.toString());
                                Log.i("Location",mLastLocation.toString());

                                if (ActivityCompat.checkSelfPermission(dashboard.this,Manifest.permission.SEND_SMS)
                                        == PackageManager.PERMISSION_DENIED) {
                                    Log.d("permission", "permission denied to SEND_SMS - requesting it");
                                    ActivityCompat.requestPermissions(dashboard.this,new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);

                                }
                                else {
                                    sendSMS(contact);
                                }

                                new FetchAddressTask(dashboard.this,
                                        dashboard.this).execute(location);
                            } else {
                                Log.i("Dashboard", getString(R.string.no_location));
                            }
                        }
                    });
        }

    }

    private void sendSMS(String contact){
        SmsManager smsManager = SmsManager.getDefault();

        smsManager.sendTextMessage(contact, null, "http://maps.google.com/?q="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude(), null, null);
        Log.i("Dashboard", String.valueOf(mLastLocation.getLatitude()));
        Log.i("Dashboard", String.valueOf(mLastLocation.getLongitude()));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS(contact);
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onTaskCompleted(String result) {
        Log.i("Dashboard",getString(R.string.address_text,
                result, System.currentTimeMillis()));
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onPause() {
        if (mTrackingLocation) {
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mTrackingLocation) {
            startTrackingLocation(contact);
        }
        super.onResume();
    }

    /**
     * Saves the last location on configuration change
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }
}

