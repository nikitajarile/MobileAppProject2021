package com.example.logintask;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference users;
    EditText name;
    EditText emergencyContact;
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
        name = (EditText) findViewById(R.id.editTextName);
        emergencyContact = (EditText) findViewById(R.id.phone);
        emails = (EditText) findViewById(R.id.editTextemail);
        password = (EditText) findViewById(R.id.editTextpassword);
        btnRegister = (Button) findViewById(R.id.btnsignup);
        progressBar = (ProgressBar) findViewById(R.id.progressLog);



        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(signup.this, login.class));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = name.getText().toString().trim();
                final  String email = emails.getText().toString().trim();
                String pwd = password.getText().toString().trim();
                final String contact = emergencyContact.getText().toString().trim();

                if(userName.isEmpty()){
                    name.setError(getString(R.string.enterName));
                    name.requestFocus();
                }
                else if(email.isEmpty()){
                    emails.setError(getString(R.string.enterEmail));
                    emails.requestFocus();
                }
                else if(pwd.isEmpty()){
                    password.setError(getString(R.string.enterPassword));
                    password.requestFocus();
                }
                else if(contact.isEmpty()){
                    emergencyContact.setError(getString(R.string.enterContact));
                    emergencyContact.requestFocus();
                }
                else  if(userName.isEmpty() && contact.isEmpty() && email.isEmpty() && pwd.isEmpty()){
                    Toast.makeText(signup.this, getString(R.string.emptyFields),Toast.LENGTH_SHORT).show();
                }
                else  if(!(userName.isEmpty() && contact.isEmpty() && email.isEmpty() && pwd.isEmpty())){
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(signup.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(!task.isSuccessful()){
                                Toast.makeText(signup.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                            else {
                                //FirebaseUser user = firebaseAuth.getCurrentUser();
                                /*UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(signup.this, "phone stored successful", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });*/

                                User user = new User(email,userName,contact);

                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            startActivity(new Intent(signup.this, login.class));
                                            Toast.makeText(signup.this, getString(R.string.registrationSuccess), Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        else{
                                            Toast.makeText(signup.this, getString(R.string.registerFailed), Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });




                            }
                        }
                    });
                }
                else{
                    Toast.makeText(signup.this, getString(R.string.errorOccured),Toast.LENGTH_SHORT).show();

                }
            }
        });


    }
}