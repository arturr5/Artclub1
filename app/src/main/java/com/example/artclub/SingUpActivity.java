package com.example.artclub;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SingUpActivity extends AppCompatActivity {
    EditText userName, userEmail, userPassword, userRePassword;
    TextView singIn;
    Button singUp;
    String name, email, password;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        // Initialize views
        userName = findViewById(R.id.usernametext);
        userEmail = findViewById(R.id.emailText);
        userPassword = findViewById(R.id.passwordText);
        userRePassword = findViewById(R.id.passwordTextRe);
        singIn = findViewById(R.id.login);
        singUp = findViewById(R.id.singup);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Set click listeners
        singUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        singIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to sign in activity
                Intent intent = new Intent(SingUpActivity.this, SingInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signUp() {
        // Get user input
        name = userName.getText().toString().trim();
        email = userEmail.getText().toString().trim();
        password = userPassword.getText().toString().trim();
        String reEnteredPassword = userRePassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name)) {
            userName.setError("Please Enter Your Username");
            userName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            userEmail.setError("Please Enter Your Email");
            userEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            userPassword.setError("Please Enter Your Password");
            userPassword.requestFocus();
            return;
        }
        if (!password.equals(reEnteredPassword)) {
            // Passwords don't match
            Toast.makeText(SingUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign up user
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Update user profile
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        firebaseUser.updateProfile(userProfileChangeRequest);

                        // Send email verification
                        firebaseUser.sendEmailVerification();

                        // Save user data to Firebase Realtime Database
                        UserModel userModel = new UserModel(firebaseUser.getUid(), email, password, name);
                        databaseReference.child(firebaseUser.getUid()).setValue(userModel);

                        // Navigate to main activity
                        Intent intent = new Intent(SingUpActivity.this, MainActivity.class);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle sign-up failure
                        Toast.makeText(SingUpActivity.this, "Sign-up Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("SingUpActivity", "Sign-up Failed", e);
                    }
                });
    }
}