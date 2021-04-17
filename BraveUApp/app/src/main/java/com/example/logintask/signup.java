package com.example.logintask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.logintask.model.user;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

public class signup extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference users;
    EditText phone;
    EditText emails;
    EditText password;
    Button btnRegister;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);



        firebaseAuth = FirebaseAuth.getInstance();

        loginBtn = (Button) findViewById(R.id.btnlogin);
        final EditText phone = (EditText) findViewById(R.id.phone);
        emails = (EditText) findViewById(R.id.editTextemail);
        password = (EditText) findViewById(R.id.editTextpassword);
        btnRegister = (Button) findViewById(R.id.btnsignup);
        progressBar = (ProgressBar) findViewById(R.id.progressLog);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(signup.this,login.class));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final  String email = emails.getText().toString().trim();
                String pwd = password.getText().toString().trim();
                final String name = phone.getText().toString().trim();
                if(email.isEmpty()){
                    emails.setError("Please enter email id");
                    emails.requestFocus();
                }
                else  if(pwd.isEmpty()){
                    password.setError("Please enter your password");
                    password.requestFocus();
                }
                else  if(email.isEmpty() && pwd.isEmpty()){
                    Toast.makeText(signup.this,"Fields Are Empty!",Toast.LENGTH_SHORT).show();
                }
                else  if(!(email.isEmpty() && pwd.isEmpty())){
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(signup.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(!task.isSuccessful()){
                                Toast.makeText(signup.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            else {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(signup.this, "phone stored successful", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });



                                    startActivity(new Intent(signup.this, login.class));
                                Toast.makeText(signup.this, "Registration is successful", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                else{
                    Toast.makeText(signup.this,"Error Occurred!",Toast.LENGTH_SHORT).show();

                }
            }
        });


    }
}