package com.example.artclub;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    TextView FullName, Email;

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    String userid, UserName, UserEmail;
    // Button buttonV, rePassword, ChangeProfile;
    ImageView ProfileImage;
    private CircleImageView profileImageView;
    StorageReference storageReference;
    String CurrentUserUid;
    private static final int REQUEST_IMAGE_SELECT = 1000;
    // private ImageView profileImageView;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FullName = findViewById(R.id.nametextt);
        Email = findViewById(R.id.emailTextt);
        //ProfileImage = findViewById(R.id.ProfileImage);

        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        userid = fAuth.getCurrentUser().getUid();
        final FirebaseUser user = fAuth.getCurrentUser();
        CurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        FirebaseStorage storage = FirebaseStorage.getInstance();



        profileImageView = findViewById(R.id.ProfileImage);
        profileImageView.setClickable(true);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        DocumentReference documentReference = fstore.collection("users").document(userid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserName = documentSnapshot.getString("userName");
                    UserEmail = documentSnapshot.getString("userEmail");

                    FullName.setText(UserName);
                    Email.setText(UserEmail);
                } else {
                    Toast.makeText(Profile.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Error getting document", Toast.LENGTH_SHORT).show();
            }
        });

        // ProfileImage.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         Intent OpengalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //         startActivityForResult(OpengalleryIntent, 1000);
        //     }
        // });

        // toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        //     @Override
        //     public boolean onMenuItemClick(MenuItem item) {
        //         onOptionsItemSelected(item);
        //         return false;
        //     }
        // });
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_manu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logoutm){ // Change to R.id.logoutm
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Profile.this, SingInActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        if(item.getItemId()==R.id.main){
            Intent intent = new Intent(Profile.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void openAlert() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        final View view = getLayoutInflater().inflate(R.layout.alert_dialog, null);
//        builder.setView(view);
//        builder.setCancelable(false);

//        Button buttonV = view.findViewById(R.id.btnV);
//        final EditText rePassword = view.findViewById(R.id.edtPass);
//        Button ChangeProfile = view.findViewById(R.id.btnChProf);

//        builder.setTitle("Change Password");
//        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String pass = rePassword.getText().toString();
//                if (pass.isEmpty()) {
//                    rePassword.setError("Password can't be empty");
//                } else {
//                    fAuth.getCurrentUser().updatePassword(pass).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(Profile.this, "Password Updated", Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(Profile.this, "Error in updating password", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.show();
//    }
private void openGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, REQUEST_IMAGE_SELECT);
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
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
                        // Update the Firestore document with the user's profile picture URL.
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(fAuth.getCurrentUser().getUid()).update("profilePictureUrl", uri.toString());

                        Picasso.get().load(uri).into(profileImageView);
                        Toast.makeText(Profile.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}