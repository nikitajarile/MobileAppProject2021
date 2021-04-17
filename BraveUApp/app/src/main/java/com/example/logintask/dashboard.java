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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;


public class dashboard extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted{
    Button signOut;
    Button alert;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    ProgressBar progressBar;
    TextView emailText;
    TextView username;
    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final int PERMISSION_REQUEST_CODE = 1;
    Location mLastLocation;
    FusedLocationProviderClient mFusedLocationClient;

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

        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mLastLocation = location;

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                                    if (checkSelfPermission(Manifest.permission.SEND_SMS)
                                            == PackageManager.PERMISSION_DENIED) {

                                        Log.d("permission", "permission denied to SEND_SMS - requesting it");
                                        String[] permissions = {Manifest.permission.SEND_SMS};

                                        ActivityCompat.requestPermissions(dashboard.this,new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);

                                    }
                                }
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage("+17866085254", null, "http://maps.google.com/?q="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude(), null, null);
                                Log.i("Dashboard", String.valueOf(mLastLocation.getLatitude()));
                                Log.i("Dashboard", String.valueOf(mLastLocation.getLongitude()));
                                new FetchAddressTask(dashboard.this,
                                        dashboard.this).execute(location);
                            } else {
                                Log.i("Dashboard", String.valueOf(R.string.no_location));
                            }
                        }
                    });
        }

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
                    getLocation();
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
}
