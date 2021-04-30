package com.example.logintask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        final ImageView settings = findViewById(R.id.imageView4);
        Button update=findViewById(R.id.button);
        final EditText previousNum=findViewById(R.id.oldNum);
        final EditText newNum=findViewById(R.id.newNum);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newNo=newNum.getText().toString();
                final String oldNo=previousNum.getText().toString();
                FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + firebaseuser.getUid());

                databaseReference.addValueEventListener(new ValueEventListener() {
                    boolean showToast=false;
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean changed=false;
                        User user = dataSnapshot.getValue(User.class);
                        String contact = user.getEmergencyContact();
                        Log.i("Emergency contact", contact);

                        String oldNo=previousNum.getText().toString();
                        if(showToast)
                        {
                            Toast.makeText(getApplicationContext(),getString(R.string.contactUpdate) + newNo,Toast.LENGTH_SHORT).show();
                            showToast=false;
                        }
                        if(contact.equals(oldNo))
                        {

                            previousNum.setText(getString(R.string.numberUpdated));
                            showToast=true;
                            databaseReference.child("emergencyContact").setValue(newNo);
                        }
                        else
                        {
                            if(!previousNum.getText().toString().equals(getString(R.string.numberUpdated)))
                                previousNum.setText(getString(R.string.enterCorrectNum));
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                });

            }
        });


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(settings.this,com.example.logintask.dashboard.class);
                startActivity(intent);
            }
        });
    }
}