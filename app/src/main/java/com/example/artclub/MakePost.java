package com.example.artclub;

import static android.content.ContentValues.TAG;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionBarPolicy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MakePost extends AppCompatActivity {
    TextView FullName, Email, Phone, mess;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    String userid;
    Button buttonV, rePassword, ChangeProfile;
    ImageView ProfileImage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_post);

        ProfileImage = findViewById(R.id.ProfileImage);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profilrRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "profile.jpg");
        profilrRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(ProfileImage);
            }
        });

//        buttonV = findViewById(R.id.Verify);
//        mess = findViewById(R.id.verifytext);

        userid = fAuth.getCurrentUser().getUid();
        final FirebaseUser user = fAuth.getCurrentUser();

//        if (!user.isEmailVerified()){
//            buttonV.setVisibility(View.VISIBLE);
//            mess.setVisibility(View.VISIBLE);
//
//            buttonV.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//                            Toast.makeText(MakePost.this, "Verification Email Has been sent to your email", Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d(TAG,"onFailure: Email not sent"+ e.getMessage());
//                        }
//                    });
//
//                }
//            });
//        }

//        DocumentReference documentReference = fstore.collection("users").document(userid);
//        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                Phone.setText(value.getString("phone"));
//                FullName.setText(value.getString("fName"));
//                Email.setText(value.getString("email"));
//            }
//        });
//        rePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final EditText resetpasssword = new EditText(v.getContext());
//
//                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
//                passwordResetDialog.setTitle("Reset Password ?");
//                passwordResetDialog.setMessage("Enter Your New password nedds to bee more than 6");
//                passwordResetDialog.setView(resetpasssword);
//
//                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //stex link em uxarkelu mailin
//                        String newPassword = resetpasssword.getText().toString();
//                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//                                Toast.makeText(
//                                        MakePost.this, "Password Reset Successfully", Toast.LENGTH_SHORT).show();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(MakePost.this, "Password Reset Failed", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                });
//                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //durs em galu dialogic
//                    }
//                });
//                passwordResetDialog.create().show();
//            }
//        });
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent OpengalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(OpengalleryIntent, 1000);
                finish();
            }
        });
//        ChangeProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(),EditProfile.class);
//                intent.putExtra("Name",FullName.getText().toString());
//                intent.putExtra("Phone",Phone.getText().toString());
//                intent.putExtra("Email",Email.getText().toString());
//                startActivity(intent);
////                Intent OpenGalleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////                startActivityForResult(OpenGalleryIntent,1000);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri ImageUri = data.getData();
                //ProfileImage.setImageURI(ImageUri);


                uploadImageToFirebase(ImageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        final StorageReference fileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(ProfileImage);
                        Toast.makeText(MakePost.this, "Image uploading", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MakePost.this, "Image uploading is failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

//    public void logout(View view){
//        FirebaseAuth.getInstance().signOut();
//        startActivity(new Intent(getApplicationContext(), LogIn.class));
//        finish();
//    }
