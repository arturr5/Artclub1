package com.example.artclub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;

public class SingUpActivity extends AppCompatActivity {
    EditText userName, userEmail, userPassword, userRePassword;
    TextView singIn;
    Button singUp;
    String name, email, password;
    DatabaseReference databaseReference;


    // Inside your class
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);
        userRePassword = findViewById(R.id.passwordTextRe);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        userEmail = findViewById(R.id.emailText);
        userPassword = findViewById(R.id.passwordText);
        userName = findViewById(R.id.usernametext);
        singIn = findViewById(R.id.login);
        singUp = findViewById(R.id.singup);

        singUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingUp();
            }
        });

        // Other onClick listener...
        singIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingUpActivity.this, SingInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void SingUp() {
        name = userName.getText().toString().trim();
        email = userEmail.getText().toString().trim();
        password = userPassword.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SingUpActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        firebaseUser.reload();
                        if (firebaseUser != null) {
                            Toast.makeText(SingUpActivity.this, "Verifacation has been send to your email", Toast.LENGTH_SHORT).show();
                            firebaseUser.sendEmailVerification();

                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            firebaseUser.updateProfile(userProfileChangeRequest);
                            UserModel userModel = new UserModel(firebaseUser.getUid(), email, password, name);

                            // Store user data in Firestore
                            db.collection("users").document(firebaseUser.getUid())
                                    .set(userModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent intent = new Intent(SingUpActivity.this, MainActivity.class);
                                            intent.putExtra("name", name);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SingUpActivity.this, "Failed to store user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SingUpActivity.this, "SignUp Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}