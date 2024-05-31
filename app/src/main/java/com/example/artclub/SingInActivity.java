package com.example.artclub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

public class SingInActivity extends AppCompatActivity {
    EditText userEmail, userPassword;
    Button singIn;
    TextView singUp, rePassword;
    String email, password;
    DatabaseReference databaseReference;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);
        fAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        rePassword = (TextView) findViewById(R.id.RePassword);
        userEmail = findViewById(R.id.emailText);
        userPassword = findViewById(R.id.passwordText);
        singIn = findViewById(R.id.login);
        singUp = findViewById(R.id.singup);

        singIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = userEmail.getText().toString().trim();
                password = userPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    userEmail.setError("Please Enter Your Email");
                    userEmail.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    userEmail.setError("Please Enter Your Password");
                    userEmail.requestFocus();
                    return;
                }
                SingIn();
            }
        });
        singUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingInActivity.this, SingUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
        rePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter Your email to Received Reset Link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //stex link em uxarkelu mailin
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SingInActivity.this, "Reset Link sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SingInActivity.this, "Error ! Reset Link is not sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //durs em galu dialogic
                    }
                });
                passwordResetDialog.create().show();
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent = new Intent(SingInActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void SingIn() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(),password.trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser.isEmailVerified() && firebaseUser!=null) {
                    String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    //Intent intent = new Intent(SingInActivity.this, MainActivity.class);
                    Intent intent = new Intent(SingInActivity.this, MainActivity.class);
                    intent.putExtra("name", username);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(SingInActivity.this, "Pls Verify your email first", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof FirebaseAuthInvalidUserException){
                    Toast.makeText(SingInActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SingInActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}