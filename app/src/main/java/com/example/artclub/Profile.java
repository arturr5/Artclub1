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

import java.util.HashMap;
import java.util.Map;

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
    private static final int REQUEST_IMAGE_SELECT = 1;
    private static final int IMAGE_UPLOAD_REQUEST_CODE = 2;
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

                    String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                    if (profilePictureUrl != null) {
                        Picasso.get().load(profilePictureUrl).into(profileImageView);
                    }
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
        if(item.getItemId() == R.id.main) {
            Intent intent = new Intent(Profile.this, Profile.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.logoutm) {
            fAuth.signOut();
            Intent intent = new Intent(Profile.this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            startUploadImage();
        }
        if (requestCode == IMAGE_UPLOAD_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri downloadUrl = data.getData();
            uploadImageToFirebaseStorage(downloadUrl);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECT);
    }

    private void startUploadImage() {
        StorageReference profilePicsRef = storageReference.child("profile_pictures").child(CurrentUserUid);
        if (imageUri != null) {
            UploadTask uploadTask = profilePicsRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Intent intent = new Intent(Profile.this, Profile.class);
                    startActivityForResult(intent, IMAGE_UPLOAD_REQUEST_CODE);
                }
            });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebaseStorage(Uri downloadUrl) {
        // Save the profile picture URL to Firestore
        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DocumentReference documentReference = fstore.collection("users").document(uid);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("profilePictureUrl", downloadUrl.toString());
            documentReference.update(userMap);
        }
    }
}