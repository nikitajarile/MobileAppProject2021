package com.example.logintask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

public class login extends AppCompatActivity {

   FirebaseAuth firebaseAuth;
    EditText etmail;
    EditText etPassword;
    Button btnLogin;
    Button btnRegister;
    private FirebaseAuth.AuthStateListener authStateListener;
    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        etmail = (EditText) findViewById(R.id.etemail);
        etPassword = (EditText) findViewById(R.id.etpassword);
        btnLogin = (Button) findViewById(R.id.btnlogin);
        btnRegister = (Button) findViewById(R.id.btnsignup);
        progressBar = (ProgressBar) findViewById(R.id.progressLog);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    Log.i("Phone number",firebaseUser.getPhoneNumber());
                    Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent home = new Intent(login.this, dashboard.class);
                    startActivity(home);

                }
                else{
                    Toast.makeText(login.this, "Please Login", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

            String email = etmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.isEmpty()){
               etmail.setError("PLEASE ENTER EMAIL ADDRESS");
               etmail.requestFocus();
            }
            else if (password.isEmpty()){
                etPassword.setError("PLEASE INPUT PASSWORD");
                etPassword.requestFocus();
            }
            else if (email.isEmpty() && password.isEmpty()){
                Toast.makeText(login.this, " BOTH FIELDS ARE EMPTY!!", Toast.LENGTH_SHORT).show();
            }
            else if (!(email.isEmpty() && !password.isEmpty())){
                progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(login.this,
                        new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()){
                            Toast.makeText(login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent dashboard = new Intent(login.this, dashboard.class);
                            startActivity(dashboard);
                        }
                    }
                });

            }
            else {
                Toast.makeText(login.this, "Error Occured",Toast.LENGTH_SHORT).show();
            }



            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(login.this, signup.class);
                startActivity(signup);
            }
        });


    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);

        }
    }
}
