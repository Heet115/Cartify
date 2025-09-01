package com.cartify.app.activities.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cartify.app.R;
import com.cartify.app.activities.auth.LoginActivity;
import com.cartify.app.activities.cart.CartActivity;
import com.cartify.app.activities.main.MainActivity;
import com.cartify.app.activities.order.OrdersActivity;
import com.cartify.app.models.UserProfile;
import com.cartify.app.utils.FirebaseHelper;
import com.cartify.app.utils.InputValidator;
import com.cartify.app.utils.FormValidationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;

/**
 * Profile Activity for managing user profile data in Firestore
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView tvEmail, tvMemberSince, tvLastLogin;
    private TextView tvNameDisplay, tvPhoneDisplay, tvAddressDisplay;
    private EditText etName, etPhone, etAddress;
    private Button btnEditProfile, btnSaveProfile, btnCancelEdit;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    
    private UserProfile currentProfile;
    private boolean isEditMode = false;
    private FormValidationHelper formValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupValidation();
        setupToolbar();
        setupBottomNavigation();
        loadUserProfile();
    }

    private void initViews() {
        // Display TextViews
        tvEmail = findViewById(R.id.tvEmail);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvLastLogin = findViewById(R.id.tvLastLogin);
        tvNameDisplay = findViewById(R.id.tvNameDisplay);
        tvPhoneDisplay = findViewById(R.id.tvPhoneDisplay);
        tvAddressDisplay = findViewById(R.id.tvAddressDisplay);
        
        // Edit TextInputs
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        
        // Buttons
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Set click listeners
        btnEditProfile.setOnClickListener(v -> enableEditMode());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnCancelEdit.setOnClickListener(v -> cancelEdit());
        
        // Initially show display mode
        setDisplayMode();
    }

    private void setupValidation() {
        formValidator = new FormValidationHelper();
        
        // Add validation for profile fields (all optional but must be valid if provided)
        formValidator.addOptionalField(etName, InputValidator::validateName)
                   .addOptionalField(etPhone, InputValidator::validatePhone)
                   .addOptionalField(etAddress, InputValidator::validateAddress);

        // Set validation listener to enable/disable save button
        formValidator.setValidationListener(new FormValidationHelper.ValidationListener() {
            @Override
            public void onValidationStateChanged(boolean isFormValid) {
                if (isEditMode) {
                    btnSaveProfile.setEnabled(isFormValid && !isLoading());
                }
            }

            @Override
            public void onFieldValidated(EditText field, boolean isValid, String errorMessage) {
                // Optional: Handle individual field validation
            }
        });
        
        // Disable real-time validation initially (enable only in edit mode)
        formValidator.setRealTimeValidationEnabled(false);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Profile");
        
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        updateMenuVisibility(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.action_edit_profile) {
            enableEditMode();
            invalidateOptionsMenu(); // Refresh menu
            return true;
        } else if (itemId == R.id.action_save_profile) {
            saveProfile();
            return true;
        } else if (itemId == R.id.action_change_password) {
            Toast.makeText(this, "Change password feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_privacy_settings) {
            Toast.makeText(this, "Privacy settings feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_notification_settings) {
            Toast.makeText(this, "Notification settings feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_delete_account) {
            Toast.makeText(this, "Delete account feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
        return true;
    }

    private void updateMenuVisibility(android.view.Menu menu) {
        android.view.MenuItem editItem = menu.findItem(R.id.action_edit_profile);
        android.view.MenuItem saveItem = menu.findItem(R.id.action_save_profile);
        
        if (editItem != null && saveItem != null) {
            editItem.setVisible(!isEditMode);
            saveItem.setVisible(isEditMode);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(ProfileActivity.this, CartActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(ProfileActivity.this, OrdersActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true; // Already on profile
            }
            return false;
        });
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
            // Display user information
            tvEmail.setText(currentProfile.getEmail() != null ? currentProfile.getEmail() : "No email");
            tvMemberSince.setText("Member since: " + (currentProfile.getCreatedAt() != null ? 
                currentProfile.getCreatedAt().substring(0, 10) : "Unknown"));
            tvLastLogin.setText("Last login: " + (currentProfile.getLastLoginAt() != null ? 
                currentProfile.getLastLoginAt() : "Unknown"));
            
            // Display profile details
            String name = currentProfile.getName() != null && !currentProfile.getName().isEmpty() ? 
                currentProfile.getName() : "Not provided";
            String phone = currentProfile.getPhone() != null && !currentProfile.getPhone().isEmpty() ? 
                currentProfile.getPhone() : "Not provided";
            String address = currentProfile.getAddress() != null && !currentProfile.getAddress().isEmpty() ? 
                currentProfile.getAddress() : "Not provided";
                
            tvNameDisplay.setText(name);
            tvPhoneDisplay.setText(phone);
            tvAddressDisplay.setText(address);
            
            // Set edit text values
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

        // Validate all fields before saving
        if (!formValidator.validateAllFields()) {
            formValidator.focusFirstInvalidField();
            return;
        }

        // Sanitize and get input values
        String name = InputValidator.sanitizeInput(etName.getText().toString().trim());
        String phone = InputValidator.sanitizeInput(etPhone.getText().toString().trim());
        String address = InputValidator.sanitizeInput(etAddress.getText().toString().trim());

        // Additional validation for non-empty fields
        if (!name.isEmpty()) {
            InputValidator.ValidationResult nameValidation = InputValidator.validateName(name);
            if (!nameValidation.isValid()) {
                etName.setError(nameValidation.getErrorMessage());
                etName.requestFocus();
                return;
            }
        }

        if (!phone.isEmpty()) {
            InputValidator.ValidationResult phoneValidation = InputValidator.validatePhone(phone);
            if (!phoneValidation.isValid()) {
                etPhone.setError(phoneValidation.getErrorMessage());
                etPhone.requestFocus();
                return;
            }
        }

        if (!address.isEmpty()) {
            InputValidator.ValidationResult addressValidation = InputValidator.validateAddress(address);
            if (!addressValidation.isValid()) {
                etAddress.setError(addressValidation.getErrorMessage());
                etAddress.requestFocus();
                return;
            }
        }

        if (currentProfile == null) {
            createNewProfile(userId);
        }

        // Update profile data
        currentProfile.setName(name);
        currentProfile.setPhone(phone);
        currentProfile.setAddress(address);

        setLoading(true);

        FirebaseHelper.getUserProfileRef(userId)
            .set(currentProfile)
            .addOnSuccessListener(aVoid -> {
                setLoading(false);
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                displayProfile();
                setDisplayMode();
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                String errorMessage = "Failed to save profile";
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("network")) {
                        errorMessage = "Network error. Please check your connection.";
                    } else if (e.getMessage().contains("permission")) {
                        errorMessage = "Permission denied. Please try logging in again.";
                    }
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            });
    }
    
    private void enableEditMode() {
        isEditMode = true;
        formValidator.setRealTimeValidationEnabled(true);
        formValidator.clearErrors();
        setEditMode();
    }
    
    private void cancelEdit() {
        isEditMode = false;
        formValidator.setRealTimeValidationEnabled(false);
        formValidator.clearErrors();
        displayProfile(); // Reset to original values
        setDisplayMode();
    }
    
    private void setDisplayMode() {
        isEditMode = false;
        // Show display views
        findViewById(R.id.llDisplayMode).setVisibility(View.VISIBLE);
        findViewById(R.id.llEditMode).setVisibility(View.GONE);
        
        // Show edit button, hide save/cancel buttons
        btnEditProfile.setVisibility(View.VISIBLE);
        btnSaveProfile.setVisibility(View.GONE);
        btnCancelEdit.setVisibility(View.GONE);
        
        // Refresh menu
        invalidateOptionsMenu();
    }
    
    private void setEditMode() {
        isEditMode = true;
        // Show edit views
        findViewById(R.id.llDisplayMode).setVisibility(View.GONE);
        findViewById(R.id.llEditMode).setVisibility(View.VISIBLE);
        
        // Hide edit button, show save/cancel buttons
        btnEditProfile.setVisibility(View.GONE);
        btnSaveProfile.setVisibility(View.VISIBLE);
        btnCancelEdit.setVisibility(View.VISIBLE);
        
        // Refresh menu
        invalidateOptionsMenu();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!loading && formValidator.isFormValid());
        btnCancelEdit.setEnabled(!loading);
        etName.setEnabled(!loading);
        etPhone.setEnabled(!loading);
        etAddress.setEnabled(!loading);
    }

    private boolean isLoading() {
        return progressBar.getVisibility() == View.VISIBLE;
    }
}