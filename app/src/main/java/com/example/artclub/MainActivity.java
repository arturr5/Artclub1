package com.example.artclub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    UsersAdapter usersAdapter;
    String yourName;
    FirebaseFirestore db;
    CollectionReference usersCollection;
    SearchView searchEditText;
    private List<UserModel> originalUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        String userName = getIntent().getStringExtra("user");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(userName);

        usersAdapter = new UsersAdapter(this);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setAdapter(usersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users");

        fetchUsersFromFirestore(); // Fetch users from Firestore

        // Initialize SearchView
        searchEditText = findViewById(R.id.search_view);
        searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onOptionsItemSelected(item);
                return false;
            }
        });
    }

    // Method for filtering users
    private void filterUsers(String query) {
        List<UserModel> filteredList = new ArrayList<>();
        if (!TextUtils.isEmpty(query)) {
            for (UserModel userModel : originalUserList) {
                if (userModel.getUserName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(userModel);
                }
            }
            usersAdapter.setUserModelList(filteredList);
        } else {
            usersAdapter.setUserModelList(originalUserList);
        }
        usersAdapter.notifyDataSetChanged();
    }

    // Update fetchUsersFromFirestore method to store the original list of users
    private void fetchUsersFromFirestore() {
        usersCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserModel> userModelList = new ArrayList<>();
                    originalUserList = new ArrayList<>(); // Initialize the originalUserList
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        if (userModel != null && !userModel.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                            userModelList.add(userModel);
                            originalUserList.add(userModel); // Add users to the originalUserList
                        }
                    }
                    usersAdapter.setUserModelList(userModelList);
                    usersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error fetching users from Firestore: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Failed to fetch users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_manu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_view); // Ensure the ID matches your menu XML
        if (searchItem != null) {
            searchEditText = (SearchView) searchItem.getActionView();
            searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterUsers(newText);
                    return true;
                }
            });
        } else {
            Log.e("MainActivity", "Search item not found in menu");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logoutm) { // Change to R.id.logoutm
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, SingInActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        if (item.getItemId() == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
