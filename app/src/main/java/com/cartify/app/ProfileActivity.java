package com.cartify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cartify.app.models.UserProfile;
import com.cartify.app.utils.FirebaseHelper;

/**
 * Profile Activity for managing user profile data in Firestore
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private Button btnSaveProfile;
    private ProgressBar progressBar;
    
    private UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupToolbar();
        loadUserProfile();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressBar);

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserProfile() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getUserProfileRef(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentProfile = documentSnapshot.toObject(UserProfile.class);
                    if (currentProfile != null) {
                        displayProfile();
                    }
                } else {
                    // Create new profile if doesn't exist
                    createNewProfile(userId);
                }
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load profile: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void displayProfile() {
        if (currentProfile != null) {
            etName.setText(currentProfile.getName() != null ? currentProfile.getName() : "");
            etPhone.setText(currentProfile.getPhone() != null ? currentProfile.getPhone() : "");
            etAddress.setText(currentProfile.getAddress() != null ? currentProfile.getAddress() : "");
        }
    }

    private void createNewProfile(String userId) {
        String email = FirebaseHelper.getCurrentUser().getEmail();
        String currentTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
            java.util.Locale.getDefault()).format(new java.util.Date());
        
        currentProfile = new UserProfile(userId, email, "", "", "", currentTime, currentTime);
    }

    private void saveProfile() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) return;

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (currentProfile == null) {
            createNewProfile(userId);
        }

        // Update profile data
        currentProfile.setName(name);
        currentProfile.setPhone(phone);
        currentProfile.setAddress(address);

        progressBar.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        FirebaseHelper.getUserProfileRef(userId)
            .set(currentProfile)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                Toast.makeText(this, "Failed to save profile: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}